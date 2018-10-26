package com.github.zerowise.rpc.lb;

import com.github.zerowise.rpc.common.Weightable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 ** @createtime : 2018/10/23 11:38 AM
 **/
public interface LoadBalancer<T extends Weightable> {

    Logger logger = LoggerFactory.getLogger(LoadBalancer.class);

    void updateWeightable(List<T> weigthables);

    T select();

    List<T> getWeigthables();

    static <T extends Weightable> LoadBalancer<T> make(String loadBalancerName) {
        if (StringUtils.isEmpty(loadBalancerName)) {
            return new RandomLoadBalancer<>();
        }
        try {
            return (LoadBalancer<T>) Class.forName(loadBalancerName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            logger.error("loadBalancerName:{} not exist.. use :{}", loadBalancerName, RandomLoadBalancer.class.getName(), e);
            return new RandomLoadBalancer<>();
        }
    }
}
