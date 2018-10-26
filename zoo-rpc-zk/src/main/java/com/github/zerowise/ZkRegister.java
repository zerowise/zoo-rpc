package com.github.zerowise;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.Objects;
import java.util.function.Supplier;

/**
 ** @createtime : 2018/10/25 12:42 PM
 **/
public class ZkRegister extends ZkClient {


    public ZkRegister(String zkClusterAddrs) {
        super(zkClusterAddrs);
    }


    public void register(Supplier<String> pathSupplier, Supplier<byte[]> dataSupplier) {
        register0(Objects.requireNonNull(pathSupplier.get()), Objects.requireNonNull(dataSupplier.get()));
    }


    private void register0(String path, byte[] data) {
        try {
            if (client.checkExists().forPath(path) != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("删除zk已存在节点: {}", path);
                }

                client.delete().forPath(path);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk已存在节点删除失败: {}", path, e);
            }
        }

        try {
            client//
                    .create()//
                    .creatingParentsIfNeeded()//
                    .withMode(CreateMode.EPHEMERAL)//
                    .forPath(path, data);

            if (logger.isInfoEnabled()) {
                logger.info("zk注册成功, {}", path);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注册失败, {}", path, e);
            }
        }

        if (!pathChildrenCaches.containsKey(path)) {
            addRegisterWatcher(path, data);
        }
    }


    private void addRegisterWatcher(String path, byte[] data) {
        if (pathChildrenCaches.containsKey(path)) {
            return;
        }

        PathChildrenCache watcher = new PathChildrenCache(client, path, false);

        watcher.getListenable().addListener(new PathChildrenCacheListener() {
            private volatile boolean waitForInitializedEvent = true;

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {

                    case INITIALIZED:
                        waitForInitializedEvent = false;
                        break;

                    case CONNECTION_RECONNECTED:
                        if (waitForInitializedEvent) {
                            return;
                        }

                        if (logger.isInfoEnabled()) {
                            logger.info("获得zk连接尝试重新注册, {}", path);
                        }

                        register0(path, data);
                        break;

                    default:
                        break;
                }
            }
        });

        try {
            watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            pathChildrenCaches.put(path, watcher);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk监听失败, {}", path, e);
            }
        }
    }

    public void unregister(Supplier<String> pathSupplier) {
        String path = Objects.requireNonNull(pathSupplier.get());

        try {
            PathChildrenCache watcher = pathChildrenCaches.remove(path);
            if (watcher != null) {
                watcher.close();
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("warcher关闭失败, {}", path, e);
            }
        }

        try {
            if (client.checkExists().forPath(path) != null) {
                client.delete().forPath(path);
            }

            if (logger.isInfoEnabled()) {
                logger.info("zk注销成功, {}", path);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注销失败, {}", path, e);
            }
        }
    }

}
