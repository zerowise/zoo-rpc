package com.github.zerowise.rpc.common;

/**
 ** @createtime : 2018/10/23 11:43 AM
 **/
public class IpPortWithWeight implements Weightable {

    private String ipWithPort;
    private int weight;

    public IpPortWithWeight(String addrInfos) {
        String[] ts = addrInfos.split("@");
        ipWithPort = ts[0];
        weight = Integer.parseInt(ts[1]);
    }

    @Override
    public int weight() {
        return weight;
    }

    public String ipAndPort() {
        return ipWithPort;
    }
}
