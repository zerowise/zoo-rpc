package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.common.LoadBalancer;

import java.io.IOException;
import java.util.Map;

/**
 ** @createtime : 2018/10/23 11:36 AM
 **/
public class ZooKeeperRemoteClient implements IRemoteClient {

    private Map<String, IRemoteClient> iRemoteClients;

    private LoadBalancer<IRemoteClient> loadBalancer;

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
    }

    @Override
    public int weight() {
        return 0;
    }
}
