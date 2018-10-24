package com.github.zerowise.rpc;

import com.github.zerowise.rpc.codec.RpcDecoder;
import com.github.zerowise.rpc.codec.RpcEncoder;
import com.github.zerowise.rpc.common.ClazzUtil;
import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.common.RpcResult;
import com.github.zerowise.rpc.handler.RpcClientHandler;
import com.github.zerowise.rpc.remote.FixedClusterRemoteClient;
import com.github.zerowise.rpc.remote.IRemoteClient;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 ** @createtime : 2018/10/23 2:34 PM
 **/
public class ZooRpcClient {

//    private static Logger logger = LoggerFactory.getLogger(ZooRpcClient.class);

    public static <T> T newProxyInstance(Class<T> clazz, ResultListener resultListener, IRemoteClient remoteClient) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            //logger.info("start exec:{}", method.getName());
            RpcRequest rpcRequest = new RpcRequest(UUID.randomUUID().toString(), method, args);
            RpcResult rpcResult = resultListener.onMessageWrite(rpcRequest.getMessageId(), method.getReturnType() == CompletableFuture.class);
            remoteClient.write(rpcRequest);
//            logger.info("end exec:{}", rpcRequest);
            return rpcResult.getResult();
        });
    }


    public static Map<Class, Object> makeProxy(String pack, ResultListener resultListener, IRemoteClient remoteClient) throws IOException {
        Set<Class<?>> clazzes = ClazzUtil.getClzFromPkg(pack);
        return clazzes.stream().filter(c -> c.isInterface()).collect(Collectors.toMap(c -> c, c -> newProxyInstance(c, resultListener, remoteClient)));
    }


    public static Map<Class, Object> startFixed(String clusterStrs, String loadBalancerName, String pack) throws Exception {
        RpcClientHandler rpcClientHandler = new RpcClientHandler();

        IRemoteClient remoteClient = new FixedClusterRemoteClient(clusterStrs, loadBalancerName, ch -> ch.pipeline().addLast(
                new RpcDecoder(RpcResponse.class), new RpcEncoder(RpcRequest.class), rpcClientHandler));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                remoteClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        return makeProxy(pack, rpcClientHandler, remoteClient);
    }

}
