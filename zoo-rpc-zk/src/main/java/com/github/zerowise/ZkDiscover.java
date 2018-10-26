package com.github.zerowise;

import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 ** @createtime : 2018/10/25 11:29 AM
 **/
public class ZkDiscover extends ZkClient {

    public ZkDiscover(String zkClusterAddrs) {
        super(zkClusterAddrs);
    }

    public <T extends ZkNode> void discover(Supplier<String> discover, Function<byte[], T> dataConvertor, Consumer<ConcurrentMap<String, T>> changeListener) {

        String path = Objects.requireNonNull(discover.get());
        PathChildrenCache watcher = new PathChildrenCache(client, path, true);

        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            private ConcurrentMap<String, T> results = Maps.newConcurrentMap();
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

                    case CHILD_ADDED:
                    case CHILD_UPDATED: {
                        T result = dataConvertor.apply(event.getData().getData());
                        results.put(result.node(), result);
                        if (logger.isInfoEnabled()) {
                            logger.info("新增节点: " + result);
                        }
                        break;
                    }

                    case CHILD_REMOVED: {
                        T result = dataConvertor.apply(event.getData().getData());
                        results.remove(result.node());

                        if (logger.isInfoEnabled()) {
                            logger.info("删除节点: " + result);
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
                        changeListener.accept(results);
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
            pathChildrenCaches.put(path, watcher);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk监听失败, " + path, e);
            }
        }
    }

}
