package com.github.zerowise.provider.zk;

import com.github.zerowise.provider.IRemoteProvider;
import com.github.zerowise.zk.ZkRegister;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 ** @createtime : 2018/10/26 2:08 PM
 **/
public class ZookeeperRpcProvider implements IRemoteProvider {

    private List<IRemoteProvider> remoteProviders;

    private ZkRegister zkRegister;

    public ZookeeperRpcProvider(String registyAddrs,List<String> addresses){
        zkRegister = new ZkRegister(registyAddrs);

//        addresses.stream().map(s-> {
//            String[] tmp = s.split("@");
//
//
//
//            zkRegister.register();
//        });
    }

    @Override
    public void close() throws IOException {
        remoteProviders.forEach(iRemoteProvider -> {
            try {
                iRemoteProvider.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        remoteProviders.clear();
        zkRegister.close();
    }
}
