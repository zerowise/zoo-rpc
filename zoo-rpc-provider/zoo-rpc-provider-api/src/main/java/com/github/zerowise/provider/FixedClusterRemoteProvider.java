package com.github.zerowise.provider;

import io.netty.channel.Channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 ** @createtime : 2018/10/26 11:21 AM
 **/
public class FixedClusterRemoteProvider implements IRemoteProvider {

    private List<IRemoteProvider> remoteProviders;

    public FixedClusterRemoteProvider(String addrClusters, Consumer<Channel> channelConsumer, Consumer<SocketAddress> consumer) {
        remoteProviders = Stream.of(addrClusters.split(";")).map(s -> {
            String[] tmp = s.split(":");
            return new RemoteProvider(channelConsumer, new InetSocketAddress(tmp[0], Integer.parseInt(tmp[1])), consumer);
        }).collect(Collectors.toList());
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
    }
}
