package com.github.zerowise.rpc;

/**
 ** @createtime : 2018/10/24 3:36 PM
 **/
public class ClientConf {
    private String zooKeeperAddrs;
    private String loadBalanceClazzName;
    private String group;
    private String app;

    public String getZooKeeperAddrs() {
        return zooKeeperAddrs;
    }

    public void setZooKeeperAddrs(String zooKeeperAddrs) {
        this.zooKeeperAddrs = zooKeeperAddrs;
    }

    public String getLoadBalanceClazzName() {
        return loadBalanceClazzName;
    }

    public void setLoadBalanceClazzName(String loadBalanceClazzName) {
        this.loadBalanceClazzName = loadBalanceClazzName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
