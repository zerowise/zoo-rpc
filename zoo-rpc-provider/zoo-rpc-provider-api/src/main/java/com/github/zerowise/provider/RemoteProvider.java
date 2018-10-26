package com.github.zerowise.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 ** @createtime : 2018/10/23 11:21 AM
 **/
public class RemoteProvider implements IRemoteProvider {

    private EventLoopGroup boss;
    private EventLoopGroup worker;

    private Runnable closeListener;

    public RemoteProvider(Consumer<Channel> channelConsumer, SocketAddress socketAddress, Consumer<SocketAddress> startListener) {
        this(channelConsumer, socketAddress, startListener, null);
    }

    public RemoteProvider(Consumer<Channel> channelConsumer, SocketAddress socketAddress, Consumer<SocketAddress> startListener, Runnable closeListener) {
        this(1, Runtime.getRuntime().availableProcessors() * 2, channelConsumer, socketAddress, startListener, closeListener);
    }

    public RemoteProvider(int bossThreadNum, int workerThreadNum, Consumer<Channel> channelConsumer, SocketAddress socketAddress, Consumer<SocketAddress> startListener, Runnable closeListener) {
        try {
            boss = Epoll.isAvailable() ? new EpollEventLoopGroup(bossThreadNum) : new NioEventLoopGroup(bossThreadNum);
            worker = Epoll.isAvailable() ? new EpollEventLoopGroup(workerThreadNum) : new NioEventLoopGroup(workerThreadNum);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channelFactory(Epoll.isAvailable() ? EpollServerSocketChannel::new : NioServerSocketChannel::new)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channelConsumer.accept(channel);
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(socketAddress).sync();
            if (future.isSuccess()) {
                startListener.accept(socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.closeListener = closeListener;
    }


    @Override
    public void close() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
        if (closeListener != null) {
            closeListener.run();
        }
    }

}
