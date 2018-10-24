package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.common.AddressWithWeight;
import com.google.common.net.HostAndPort;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.asm.Register;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZkRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

    private CuratorFramework client;
    private ConcurrentMap<String, PathChildrenCache> watcherMap;

    public void init(String serverList, String group, String app, AddressWithWeight addressWithWeight) {
        watcherMap = new ConcurrentHashMap<>();
        RetryPolicy retryPolicy = new ForeverRetryPolicy(1000, 60 * 1000);
        client = CuratorFrameworkFactory.newClient(serverList, 1000 * 10, 1000 * 3, retryPolicy);
        client.start();

        register(group, app, addressWithWeight);
    }

    private void register(String group, String app, AddressWithWeight addressWithWeight) {
        final String path = "/zoo/" + group + "/" + app + "/"
                + byte2HexStr(addressWithWeight.getServerAddr().getBytes(StandardCharsets.UTF_8));

        byte[] data = addressWithWeight.toBytes();

        try {
            if (client.checkExists().forPath(path) != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("删除zk已存在节点: {},{}", path, addressWithWeight.getServerAddr());
                }

                client.delete().forPath(path);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk已存在节点删除失败: {},{}", path, addressWithWeight.getServerAddr(), e);
            }
        }

        try {
            client//
                    .create()//
                    .creatingParentsIfNeeded()//
                    .withMode(CreateMode.EPHEMERAL)//
                    .forPath(path, data);

            if (logger.isInfoEnabled()) {
                logger.info("zk注册成功, {},{}", path, addressWithWeight);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注册失败, {},{}", path, addressWithWeight, e);
            }
        }

        if (!watcherMap.containsKey(path)) {
            addRegisterWatcher(group, app, addressWithWeight);
        }
    }

    private void addRegisterWatcher(String group, String app, AddressWithWeight addressWithWeight) {
        final String path = "/zoo/" + group + "/" + app + "/"
                + byte2HexStr(addressWithWeight.getServerAddr().getBytes(StandardCharsets.UTF_8));

        if (watcherMap.containsKey(path)) {
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
                            logger.info("获得zk连接尝试重新注册, {},{}", path, addressWithWeight);
                        }

                        ZkRegister.this.register(group, app, addressWithWeight);
                        break;

                    default:
                        break;
                }
            }
        });

        try {
            watcher.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            watcherMap.put(path, watcher);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk监听失败, {}", path, e);
            }
        }
    }


    public void unregister(String group, String app, AddressWithWeight addressWithWeight) {
        String path = "/zoo/" + group + "/" + app + "/"
                + byte2HexStr(addressWithWeight.getServerAddr().getBytes(StandardCharsets.UTF_8));

        try {
            PathChildrenCache watcher = watcherMap.remove(path);
            if (watcher != null) {
                watcher.close();
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("warcher关闭失败, {},{}", path, addressWithWeight.getServerAddr(), e);
            }
        }

        try {
            if (client.checkExists().forPath(path) != null) {
                client.delete().forPath(path);
            }

            if (logger.isInfoEnabled()) {
                logger.info("zk注销成功, {},{}", path, addressWithWeight.getServerAddr());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("zk注销失败, {},{}", path, addressWithWeight.getServerAddr(), e);
            }
        }
    }

    public void close() throws IOException {
        watcherMap.forEach((path, watcher) -> {
            try {
                watcher.close();
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("warcher关闭失败, " + path);
                }
            }
        });

        watcherMap = null;

        client.close();
        client = null;
    }


    /**
     * bytes转换成十六进制字符串
     *
     * @param  b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    private static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//			sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

}