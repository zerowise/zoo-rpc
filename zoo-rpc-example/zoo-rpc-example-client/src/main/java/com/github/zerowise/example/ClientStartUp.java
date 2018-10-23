package com.github.zerowise.example;

import com.github.zerowise.example.api.CalService;
import com.github.zerowise.rpc.ZooRpcClient;

import java.io.IOException;
import java.util.Map;

/**
 ** @createtime : 2018/10/23 4:08 PM
 **/
public class ClientStartUp {

    public static void main(String[] args) {

        try {
            Map<Class, Object> beans = ZooRpcClient.startFixed("localhost:8888@100", "com.github.zerowise.rpc.lb.RandomLoadBalancer", "com.github.zerowise.example.api");
            System.out.println(((CalService) beans.get(CalService.class)).add(1000, 1000));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
