/**
 ** @createtime : 2018/10/24 2:49 PM
 **/
public class ServerConf {
    private String zooKeeperAddrs;
    private String group;
    private String app;
    private String ipAddr;
    private int serverWeight;

    public String getZooKeeperAddrs() {
        return zooKeeperAddrs;
    }

    public void setZooKeeperAddrs(String zooKeeperAddrs) {
        this.zooKeeperAddrs = zooKeeperAddrs;
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

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getServerWeight() {
        return serverWeight;
    }

    public void setServerWeight(int serverWeight) {
        this.serverWeight = serverWeight;
    }
}
