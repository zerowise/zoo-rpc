package com.github.zerowise.rpc.common;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

/**
 ** @createtime : 2018/10/23 11:43 AM
 **/
public class AddressWithWeight implements Weightable {

    private String serverAddress;
    private int weight;

    public AddressWithWeight(byte[] bytes) {
        this(new String(bytes, Charset.defaultCharset()));
    }

    public AddressWithWeight(String addrInfos) {
        String[] ts = addrInfos.split("@");
        serverAddress = ts[0];
        weight = Integer.parseInt(ts[1]);
    }

    public AddressWithWeight(String serverAddress, int serverWeight) {
        this.serverAddress = serverAddress;
        this.weight = serverWeight;
    }

    @Override
    public int weight() {
        return weight;
    }

    public String getServerAddr() {
        return serverAddress;
    }

    public byte[] toBytes() {
        return (serverAddress + "@" + weight).getBytes(Charset.defaultCharset());
    }

    public SocketAddress toSocketAddr() {
        String[] tmps = serverAddress.split(":");
        return new InetSocketAddress(tmps[0], Integer.parseInt(tmps[1]));
    }

    @Override
    public String toString() {
        return serverAddress + "@" + weight;
    }
}
