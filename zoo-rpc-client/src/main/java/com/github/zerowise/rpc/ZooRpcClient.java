package com.github.zerowise.rpc;

import com.github.zerowise.rpc.codec.RpcDecoder;
import com.github.zerowise.rpc.codec.RpcEncoder;
import com.github.zerowise.rpc.common.ClazzUtil;
import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.common.RpcResult;
import com.github.zerowise.rpc.handler.RpcClientHandler;
import com.github.zerowise.rpc.remote.IRemoteClient;
import com.github.zerowise.rpc.remote.RemoteClient;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 ** @createtime : 2018/10/23 2:34 PM
 **/
public class ZooRpcClient {

    public static void main(String[] args) {
        RpcClientHandler rpcClientHandler = new RpcClientHandler();

        IRemoteClient remoteClient = new RemoteClient(new InetSocketAddress("localhost", 8888), ch -> ch.pipeline().addLast(
                new RpcDecoder(RpcResponse.class), new RpcEncoder(RpcRequest.class), rpcClientHandler));

        newProxyInstance(ZooRpcClient.class, rpcClientHandler, remoteClient);
    }


    static <T> T newProxyInstance(Class<T> clazz, SyncResultListener syncResultListener, IRemoteClient remoteClient) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method, args);
            RpcResult rpcResult = syncResultListener.onMessageWrite(rpcRequest.getMessageId());
            remoteClient.write(rpcRequest);
            return rpcResult.getResult();
        });
    }


    static Map<Class, Object> makeProxy(String pack, SyncResultListener syncResultListener, IRemoteClient remoteClient) throws IOException {
        Set<Class<?>> clazzes = ClazzUtil.findCandidateComponents(pack);
        return clazzes.stream().filter(c -> c.isInterface()).collect(Collectors.toMap(c -> c, c -> newProxyInstance(c, syncResultListener, remoteClient)));
    }


}
