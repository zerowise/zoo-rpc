package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.common.AddressWithWeight;
import com.github.zerowise.rpc.lb.LoadBalancer;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 ** @createtime : 2018/10/23 11:36 AM
 **/
public class ZooKeeperRemoteClient implements IRemoteClient {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRemoteClient.class);

    private Map<String, IRemoteClient> iRemoteClients;

    private LoadBalancer<IRemoteClient> loadBalancer;

    private Consumer<Channel> consumer;

    private CuratorFramework client;
    private List<PathChildrenCache> watchers;

    private CountDownLatch initSuccess = null;

    public ZooKeeperRemoteClient(String zooKeeperAddrs, String loadBalanceClazzName, String group, String app, Consumer<Channel> consumer) throws Exception {
        watchers = Collections.synchronizedList(new ArrayList<>());
        RetryPolicy retryPolicy = new ForeverRetryPolicy(1000, 60 * 1000);
        client = CuratorFrameworkFactory.newClient(zooKeeperAddrs, 1000 * 10, 1000 * 3, retryPolicy);
        client.start();
        iRemoteClients = new ConcurrentHashMap<>();
        loadBalancer = (LoadBalancer<IRemoteClient>) Class.forName(loadBalanceClazzName).newInstance();

        this.consumer = consumer;

        final String path = "/zoo/" + group + "/" + app;
        final PathChildrenCache watcher = new PathChildrenCache(client, path, true);

        initSuccess = new CountDownLatch(1);

        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            private final ConcurrentMap<String, Integer> serverWithWeight = new ConcurrentHashMap<>();
            private volatile boolean waitForInitializedEvent = true;

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                if (logger.isInfoEnabled()) {
                    logger.info("zk监控列表发生变化, " + path + ", " + event.getType());
                }

                boolean isChanged = true;

                switch (event.getType()) {
                    case INITIALIZED:
                        waitForInitializedEvent = false;

                        if (logger.isInfoEnabled()) {
                            logger.info("完成初始化: " + path);
                        }

                        break;

                    case CHILD_ADDED: {
                        AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
                        serverWithWeight.put(kv.getServerAddr(), kv.weight());

                        if (logger.isInfoEnabled()) {
                            logger.info("新增节点: " + kv);
                        }

                        break;
                    }

                    case CHILD_REMOVED: {
                        AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
                        serverWithWeight.remove(kv.getServerAddr());

                        if (logger.isInfoEnabled()) {
                            logger.info("删除节点: " + kv);
                        }

                        break;
                    }

                    case CHILD_UPDATED: {
                        AddressWithWeight kv = new AddressWithWeight(event.getData().getData());
                        serverWithWeight.put(kv.getServerAddr(), kv.weight());

                        if (logger.isInfoEnabled()) {
                            logger.info("更新节点: " + kv);
                        }

                        break;
                    }

                    default:
                        isChanged = false;

                        if (logger.isInfoEnabled()) {
                            logger.info("忽略, " + path + ", " + event.getType());
                        }
                }

                if (!waitForInitializedEvent && isChanged) {
                    try {
                        onChange(serverWithWeight);
                    } catch (Throwable t) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Discover监听处理失败", t);
                        }
                    }
                }
            }
        };

        watcher.getListenable().addListener(pathChildrenCacheListener);
        try {
            watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            watchers.add(watcher);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk监听失败, " + path, e);
            }
        }

        initSuccess.await(2000, TimeUnit.SECONDS);
        initSuccess = null;
    }

    private void onChange(ConcurrentMap<String, Integer> serverWithWeight) {
        if (!iRemoteClients.isEmpty()) {
            Lists.newArrayList(iRemoteClients.keySet()).forEach(s -> {
                if (!serverWithWeight.containsKey(s)) {
                    IRemoteClient iRemoteClient = iRemoteClients.remove(s);
                    try {
                        if (iRemoteClient != null)
                            iRemoteClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        serverWithWeight.forEach((key, value) -> iRemoteClients.computeIfAbsent(key, t -> RemoteClient.build(key + "@" + value, consumer)));
        loadBalancer.updateWeightable(Lists.newArrayList(iRemoteClients.values()));

        if (initSuccess != null) {
            initSuccess.countDown();
        }

    }

    @Override
    public void write(Object request) throws Exception {
        loadBalancer.select().write(request);
    }

    @Override
    public void close() {
        iRemoteClients.values().forEach(c -> {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        iRemoteClients.clear();

        for (int i = 0; i < watchers.size(); i++) {
            PathChildrenCache watcher = watchers.get(i);

            try {
                watcher.close();
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("watcher关闭失败 ", e);
                }
            }
        }

        watchers = null;

        client.close();
        client = null;
    }

    @Override
    public int weight() {
        return 0;
    }
}
