package com.github.zerowise.example.api;

import java.util.concurrent.CompletableFuture;

/**
 ** @createtime : 2018/10/24 11:03 AM
 **/
public interface AsycCalService {

    CompletableFuture<Integer> add(int ma, int mb);
}
