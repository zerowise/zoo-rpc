package com.github.zerowise.zk;

import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryForever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 ** @createtime : 2018/10/25 1:18 PM
 **/
public class ZkClient implements Closeable {

    protected static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);
    protected CuratorFramework client;
    protected ConcurrentMap<String, PathChildrenCache> pathChildrenCaches;

    public ZkClient(String zkClusterAddrs) {
        client = CuratorFrameworkFactory.newClient(zkClusterAddrs, 1000 * 10, 1000 * 3, new RetryForever(2000));
        client.start();
        pathChildrenCaches = Maps.newConcurrentMap();
    }

    @Override
    public void close() throws IOException {
        pathChildrenCaches.forEach((path, w) -> {
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pathChildrenCaches.clear();
        client.close();
    }
}
