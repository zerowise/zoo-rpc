package com.github.zerowise.rpc.handler;

import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 ** @createtime : 2018/10/23 1:08 PM
 **/
@ChannelHandler.Sharable
public class RpcHander extends SimpleChannelInboundHandler<RpcRequest> {
    private ExecutorService executorService;
    private Map<String, Object> rpcService;

    public RpcHander() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        rpcService = new HashMap<>();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        execute(ctx, msg);
    }

    public void register(Object obj, Predicate<Class> p) {
        if (obj.getClass().isAnnotationPresent(RpcService.class)) {
            Class clazz = obj.getClass();
            while (clazz != Object.class) {
                Stream.of(clazz.getInterfaces()).filter(p).forEach(clazz0 -> rpcService.putIfAbsent(clazz0.getName(), obj));
                clazz = clazz.getSuperclass();
            }
        }
    }

    public void execute(ChannelHandlerContext ctx, RpcRequest msg) {

        executorService.execute(() -> {
            RpcResponse rpcResponse;
            try {
                Object obj = rpcService.get(msg.getServiceName());
                FastClass fastClass = FastClass.create(obj.getClass());
                FastMethod fastMethod = fastClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
                Object value = fastMethod.invoke(obj, msg.getArgumemts());

                if (value instanceof CompletableFuture) {
                    rpcResponse = wrapAsynRespone(msg.getMessageId(), (CompletableFuture) value);
                } else {
                    rpcResponse = wrapSynRespone(msg.getMessageId(), value);
                }
            } catch (Exception e) {
                rpcResponse = new RpcResponse(msg.getMessageId(), e);
                e.printStackTrace();
            }
            ctx.writeAndFlush(rpcResponse);
        });
    }

    private RpcResponse wrapSynRespone(String messageId, Object value) {
        return new RpcResponse(messageId, value);
    }

    private RpcResponse wrapAsynRespone(String messageId, CompletableFuture value) {
        try {
            return new RpcResponse(messageId, value.get());
        } catch (InterruptedException | ExecutionException e) {
            return new RpcResponse(messageId, e.getCause() != null ? e.getCause() : e);
        }

    }

    public void shutdown() {
        executorService.shutdown();
    }
}
