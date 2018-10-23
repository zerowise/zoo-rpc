import com.github.zerowise.rpc.codec.RpcDecoder;
import com.github.zerowise.rpc.codec.RpcEncoder;
import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.handler.RpcHander;
import com.github.zerowise.rpc.remote.RemoteServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;

/**
 ** @createtime : 2018/10/23 4:49 PM
 **/
public class ConsumerStartup {
    public static void main(String[] args) {
        RpcHander rpcHander = new RpcHander();

        rpcHander.register(new CalServiceImpl(), cls -> true);


        RemoteServer zooRpcServer = new RemoteServer(1, 4, () ->
                new ChannelInitializer<ServerSocketChannel>() {
                    @Override
                    protected void initChannel(ServerSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcDecoder(RpcRequest.class), new RpcEncoder(RpcResponse.class), rpcHander);
                    }
                }, 8888, System.out::print);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> zooRpcServer.close()));
    }
}
