package com.github.zerowise.rpc.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 ** @createtime : 2018/10/2310:51 AM
 **/
public class RpcResult {

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    private Object result;

    private Throwable error;

    public Object getResult() throws Throwable {
        try {
            lock.lock();
            condition.await();
            if (error != null) {
                throw error;
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void onResult(RpcResponse rpcResponse) {
        try {
            lock.lock();
            this.result = rpcResponse.getResult();
            this.error = rpcResponse.getError();
        } catch (Exception e) {
            this.error = e;
        } finally {
            condition.signal();
            lock.unlock();
        }
    }

}
