package com.github.zerowise.rpc.lb;

import com.github.zerowise.rpc.common.Weightable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 ** @createtime : 2018/10/23 4:10 PM
 **/
public final class RandomLoadBalancer<T extends Weightable> implements LoadBalancer<T> {

    private T[] weightables;
    /**
     * [4,5,8,10]
     * seed->3 index->0
     * seed->4 index->1
     * seed->7 index->2
     *
     */
    private int[] weights;//权重区间 [0,weight)

    @Override
    public void updateWeightable(List<T> weigthableList) {
        weightables = (T[]) weigthableList.toArray();
        Arrays.sort(weightables, Comparator.comparingInt(Weightable::weight));
        weights = new int[weigthableList.size()];
        int temp = 0;
        for (int i = 0; i < weights.length; i++) {
            temp += weightables[i].weight();
            weights[i] = temp;
        }
    }

    @Override
    public T select() {
        if (weights.length == 0) {
            return null;
        }

        if (weights.length == 1) {
            return weightables[0];
        }

        int seed = ThreadLocalRandom.current().nextInt(weights[weights.length - 1]);
        for (int i = 0; i < weights.length; i++) {
            if (seed < weights[i]) {
                return weightables[i];
            }
        }

        return null;
    }

    @Override
    public List<T> getWeigthables() {
        return Arrays.asList(weightables);
    }
}
