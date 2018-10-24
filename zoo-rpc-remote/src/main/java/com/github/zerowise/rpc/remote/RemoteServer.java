package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.common.AddressWithWeight;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 ** @createtime : 2018/10/23 11:21 AM
 **/
public class RemoteServer implements Closeable {
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public RemoteServer(int bossThreadNum, int workerThreadNum, Supplier<ChannelHandler> channelHandlerSupplier, SocketAddress socketAddress, Consumer<SocketAddress> consumer) {
        try {
            boss = Epoll.isAvailable() ? new EpollEventLoopGroup(bossThreadNum) : new NioEventLoopGroup(bossThreadNum);
            worker = Epoll.isAvailable() ? new EpollEventLoopGroup(workerThreadNum) : new NioEventLoopGroup(workerThreadNum);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channelFactory(Epoll.isAvailable() ? EpollServerDomainSocketChannel::new : NioServerSocketChannel::new)
                    .childHandler(channelHandlerSupplier.get())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(socketAddress).sync();
            if (future.isSuccess()) {
                consumer.accept(socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void close() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
    }

}
