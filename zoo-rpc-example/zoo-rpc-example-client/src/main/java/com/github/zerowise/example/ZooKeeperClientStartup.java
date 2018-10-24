package com.github.zerowise.example;

import com.alibaba.fastjson.JSON;
import com.github.zerowise.example.api.AsycCalService;
import com.github.zerowise.rpc.ClientConf;
import com.github.zerowise.rpc.ZooRpcClient;
import com.github.zerowise.rpc.remote.ZooKeeperRemoteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 ** @createtime : 2018/10/24 3:34 PM
 **/
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
