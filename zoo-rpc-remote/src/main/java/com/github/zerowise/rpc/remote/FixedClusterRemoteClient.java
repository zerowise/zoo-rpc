package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.lb.LoadBalancer;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 ** @createtime : 2018/10/23 3:47 PM
 **/
public class FixedClusterRemoteClient implements IRemoteClient {

    private LoadBalancer<IRemoteClient> loadBalancer;

    public FixedClusterRemoteClient(String clusterStr, String loadBalancerName, Consumer<Channel> consumer) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<IRemoteClient> iRemoteClients = Stream.of(clusterStr.split(";")).map(s -> RemoteClient.build(s, consumer)).collect(Collectors.toList());
        loadBalancer = (LoadBalancer<IRemoteClient>) Class.forName(loadBalancerName).newInstance();
        loadBalancer.updateWeightable(iRemoteClients);
    }

    @Override
    public void write(Object request) throws Exception {
        loadBalancer.select().write(request);
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        loadBalancer.getWeigthables().forEach(iRemoteClient -> {
            try {
                iRemoteClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
