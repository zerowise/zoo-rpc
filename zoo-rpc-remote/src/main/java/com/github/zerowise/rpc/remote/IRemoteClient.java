package com.github.zerowise.rpc.remote;

import com.github.zerowise.rpc.common.Weightable;

import java.io.Closeable;

/**
 ** @createtime : 2018/10/23 11:27 AM
 **/
public interface IRemoteClient extends Closeable, Weightable {

    void write(Object request) throws Exception;
}
