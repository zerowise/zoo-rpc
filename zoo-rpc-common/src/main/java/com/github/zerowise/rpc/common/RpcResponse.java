package com.github.zerowise.rpc.common;

/**
 ** @createtime : 2018/10/227:02 PM
 **/
public class RpcResponse {
    private String messageId;
    private Object result;
    private Throwable error;

    public RpcResponse() {
    }

    public RpcResponse(String messageId, Object result) {
        this.messageId = messageId;
        this.result = result;
    }

    public RpcResponse(String messageId, Throwable error) {
        this.messageId = messageId;
        this.error = error;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
