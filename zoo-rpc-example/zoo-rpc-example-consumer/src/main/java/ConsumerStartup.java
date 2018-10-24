import com.github.zerowise.rpc.codec.RpcDecoder;
import com.github.zerowise.rpc.codec.RpcEncoder;
import com.github.zerowise.rpc.common.AddressWithWeight;
import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.handler.RpcHander;
import com.github.zerowise.rpc.remote.RemoteServer;
import com.google.common.net.HostAndPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 ** @createtime : 2018/10/23 4:49 PM
 **/
public class ConsumerStartup {
    public static void main(String[] args) {
        RpcHander rpcHander = new RpcHander();

        rpcHander.register(new CalServiceImpl(), cls -> true);
        rpcHander.register(new AsycCalServiceImpl(), cls -> true);


        RemoteServer remoteServer = new RemoteServer(1, 4, () ->
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new RpcDecoder(RpcRequest.class), new RpcEncoder(RpcResponse.class), rpcHander);
                    }
                }, new AddressWithWeight(HostAndPort.fromString("localhost:8888"), 100).toSocketAddr(), System.out::print);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> remoteServer.close()));
    }
}
