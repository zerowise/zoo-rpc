package com.github.zerowise.rpc.lb;

import com.github.zerowise.rpc.common.Weightable;

import java.util.List;

/**
 ** @createtime : 2018/10/23 11:38 AM
 **/
public interface LoadBalancer<T extends Weightable> {

    void updateWeightable(List<T> weigthables);

    T select();

    List<T> getWeigthables();
}
