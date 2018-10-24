package com.github.zerowise.rpc.common;

import com.google.common.net.HostAndPort;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

/**
 ** @createtime : 2018/10/23 11:43 AM
 **/
public class AddressWithWeight implements Weightable {

    private HostAndPort hostAndPort;
    private int weight;

    public AddressWithWeight(byte[] bytes) {
        this(new String(bytes, Charset.defaultCharset()));
    }

    public AddressWithWeight(String addrInfos) {
        String[] ts = addrInfos.split("@");
        hostAndPort = HostAndPort.fromString(ts[0]);
        weight = Integer.parseInt(ts[1]);
    }

    public AddressWithWeight(HostAndPort hostAndPort, int serverWeight) {
        this.hostAndPort = hostAndPort;
        this.weight = serverWeight;
    }

    @Override
    public int weight() {
        return weight;
    }

    public String getServerAddr() {
        return hostAndPort.toString();
    }

    public byte[] toBytes() {
        return toString().getBytes(Charset.defaultCharset());
    }

    public SocketAddress toSocketAddr() {
        return new InetSocketAddress(hostAndPort.getHostText(), hostAndPort.getPort());
    }

    @Override
    public String toString() {
        return hostAndPort.toString() + "@" + weight;
    }
}
