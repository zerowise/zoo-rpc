import com.alibaba.fastjson.JSON;
import com.github.zerowise.rpc.codec.RpcDecoder;
import com.github.zerowise.rpc.codec.RpcEncoder;
import com.github.zerowise.rpc.common.AddressWithWeight;
import com.github.zerowise.rpc.common.RpcRequest;
import com.github.zerowise.rpc.common.RpcResponse;
import com.github.zerowise.rpc.handler.RpcHander;
import com.github.zerowise.rpc.remote.RemoteServer;
import com.github.zerowise.rpc.remote.ZkRegister;
import com.github.zerowise.rpc.remote.ZooKeeperRemoteClient;
import com.google.common.net.HostAndPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 ** @createtime : 2018/10/24 2:33 PM
 **/
public class ZooKeeperConsumerStartup {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperConsumerStartup.class);

    public static void main(String[] args) {

        RpcHander rpcHander = new RpcHander();


        rpcHander.register(new CalServiceImpl(), cls -> true);
        rpcHander.register(new AsycCalServiceImpl(), cls -> true);

        List<ServerConf> serverConfs;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ZooKeeperRemoteClient.class.getClassLoader().getResourceAsStream("zoo-rpc-consumer-zk.json")))) {
            StringBuilder lines = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
            serverConfs = JSON.parseArray(lines.toString(), ServerConf.class);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

        if (serverConfs == null || serverConfs.isEmpty()) {
            log.error("{} read config empty", "zoo-rpc-consumer-zk.json");
            System.exit(0);
        }

        ServerConf serverConf = serverConfs.get(0);

        ZkRegister zkRegister = new ZkRegister();

        AddressWithWeight addressWithWeight = new AddressWithWeight(HostAndPort.fromString(serverConf.getIpAddr()), serverConf.getServerWeight());
        RemoteServer zooRpcServer = new RemoteServer(1, 4, () ->
                new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new RpcDecoder(RpcRequest.class), new RpcEncoder(RpcResponse.class), rpcHander);
                    }
                }, addressWithWeight.toSocketAddr(), addr -> {
            zkRegister.init(serverConf.getZooKeeperAddrs(), serverConf.getGroup(), serverConf.getApp(), addressWithWeight);
            log.info("{} connect zookeeper success..", addressWithWeight.getServerAddr());
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                zooRpcServer.close();
                zkRegister.unregister(serverConf.getGroup(), serverConf.getApp(), addressWithWeight);
                zkRegister.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
