package com.github.zerowise.example;

import com.github.zerowise.example.api.AsycCalService;
import com.github.zerowise.example.api.CalService;
import com.github.zerowise.rpc.ZooRpcClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 ** @createtime : 2018/10/23 4:08 PM
 **/
public class ClientStartUp {

    public static void main(String[] args) {

        try {
            Map<Class, Object> beans = ZooRpcClient.startFixed("localhost:8888@100", "com.github.zerowise.rpc.lb.RandomLoadBalancer", "com.github.zerowise.example.api");
            //System.out.println(((CalService) beans.get(CalService.class)).add(1000, 1000));
//            CompletableFuture[] completableFutures = IntStream.of(5)
//                    .mapToObj(i -> ((AsycCalService) beans.get(AsycCalService.class)).add(1000, 1000))
//                    .collect(Collectors.toList())
//                    .toArray(new CompletableFuture[0]);
//            CompletableFuture.allOf(completableFutures).join();
            System.out.println( ((AsycCalService) beans.get(AsycCalService.class)).add(1000, 1000).get());
            System.out.println("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
