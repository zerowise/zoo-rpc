# zoo-rpc

### 主要是实现JAVA 异步和同步RPC
#### 主要技术
jdk1.8+,netty 4, zookeeper , guava，cglib；

### 实战

#### 单机直连
server
```java

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


```

调用端 client
```java
public class ClientStartUp {

    public static void main(String[] args) {

        try {
            Map<Class, Object> beans = ZooRpcClient.startSingal("localhost:8888@100", "com.github.zerowise.example.api");
            System.out.println(((CalService) beans.get(CalService.class)).add(1000, 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```
弊端：1.单点的负载压力有上限，单点故障，无法动态扩展

#### client集群 直连 server集群
server的如上

调用端 client
```java
public class ClientStartUp {

    public static void main(String[] args) {

        try {
            Map<Class, Object> beans = ZooRpcClient.startFixed("localhost:8888@100;localhost:8889@100;localhost:8890@100", "com.github.zerowise.example.api");
            System.out.println(((CalService) beans.get(CalService.class)).add(1000, 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
该方法实现负载均衡（随机的负载均衡算法，也可以自己实现），提高了性能，可以支持动态选择服务器，还是无法动态增删服务器，


#### 通过zookeeper来实现服务器端的动态增删
```java
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

        com.github.zerowise.ZkRegister zkRegister = new com.github.zerowise.ZkRegister();

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

```
实例只是完善了一个服务器端 通过修改配置文件（zoo-rpc-consumer-zk.json 可以自己实现）实现集群的控制 

client端的实现
```java
public class ZooKeeperClientStartup {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperClientStartup.class);

    public static void main(String[] args) {

        List<ClientConf> clientConfs;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ZooKeeperRemoteClient.class.getClassLoader().getResourceAsStream("zoo-rpc-client-zk.json")))) {
            StringBuilder lines = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
            clientConfs = JSON.parseArray(lines.toString(), ClientConf.class);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

        if (clientConfs == null || clientConfs.isEmpty()) {
            log.error("{} read config empty", "zoo-rpc-client-zk.json");
            System.exit(0);
        }
        try {

            Map<Class, Object> beans = ZooRpcClient.startZooClient("com.github.zerowise.example.api", clientConfs.get(0));
            //System.out.println(((CalService) beans.get(CalService.class)).add(1000, 1000));
//            CompletableFuture[] completableFutures = IntStream.of(5)
//                    .mapToObj(i -> ((AsycCalService) beans.get(AsycCalService.class)).add(1000, 1000))
//                    .collect(Collectors.toList())
//                    .toArray(new CompletableFuture[0]);
//            CompletableFuture.allOf(completableFutures).join();
            System.out.println(((AsycCalService) beans.get(AsycCalService.class)).add(1000, 1000).get());
            System.out.println("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

### 异步与同步的支持

异步的支持 只需要将方法的返回声明成 CompletableFuture

### 以后扩展 支持spring
### 提供更多的参数控制 比如支持线程数/连接数 修改
### 加入心跳检查
### 支持更多的负载均衡策略