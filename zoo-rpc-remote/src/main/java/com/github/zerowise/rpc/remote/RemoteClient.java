package com.github.zerowise.rpc.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Consumer;

public class RemoteClient implements IRemoteClient {

    private EventLoopGroup worker;
    private GenericObjectPool<Channel> channelPool;
    private int weight;

    public static RemoteClient build(String serverInfo, Consumer<Channel> consumer) {
        String[] str = serverInfo.split("@");
        int tempWeight = str.length < 2 ? 100 : Integer.parseInt(str[1]);
        String[] hostAndPort = str[0].split(":");
        return new RemoteClient(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])), consumer, tempWeight);
    }

    public RemoteClient(SocketAddress socketAddress, Consumer<Channel> consumer) {
        this(1, socketAddress, consumer, new GenericObjectPoolConfig<>(), 100);
    }

    public RemoteClient(SocketAddress socketAddress, Consumer<Channel> consumer, int weight) {
        this(1, socketAddress, consumer, new GenericObjectPoolConfig<>(), weight);
    }

    public RemoteClient(int workThreadNum, SocketAddress socketAddress, Consumer<Channel> consumer,
                        GenericObjectPoolConfig<Channel> poolConfig, int weight) {
        this.weight = weight;
        this.worker = new NioEventLoopGroup(workThreadNum);
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(worker).channel(NioSocketChannel.class).handler(
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        consumer.accept(ch);
                    }
                }).option(ChannelOption.TCP_NODELAY, true);


        PooledObjectFactory channelFactory = new PooledObjectFactory<Channel>() {
            public PooledObject<Channel> makeObject() throws Exception {
                return new DefaultPooledObject<>(bootstrap.connect(socketAddress).channel());
            }

            public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {

            }

            /**
             * 功能描述：判断资源对象是否有效，有效返回 true，无效返回 false
             *
             * 什么时候会调用此方法
             *
             * 1：从资源池中获取资源的时候，参数 testOnBorrow 或者 testOnCreate 中有一个 配置 为 true 时，
             *
             * 则调用 channelFactory.validateObject() 方法.
             *
             * 2：将资源返还给资源池的时候，参数 testOnReturn，配置为 true 时，调用此方法.
             *
             * 3：资源回收线程，回收资源的时候，参数 testWhileIdle，配置为 true 时，调用此方法.
             */
            public boolean validateObject(PooledObject<Channel> pooledObject) {
                return pooledObject.getObject().isActive();
            }

            public void activateObject(PooledObject<Channel> pooledObject) throws Exception {

            }

            public void passivateObject(PooledObject<Channel> pooledObject) throws Exception {

            }
        };
        channelPool = new GenericObjectPool<Channel>(channelFactory, poolConfig);
    }

    @Override
    public void write(Object request) throws Exception {
        Channel channel = null;
        try {
            channel = channelPool.borrowObject();
            channel.writeAndFlush(request);
        } finally {
            channelPool.returnObject(channel);
        }
    }


    @Override
    public void close() {
        channelPool.clear();
        channelPool.close();
        worker.shutdownGracefully();
    }

    @Override
    public int weight() {
        return weight;
    }
}