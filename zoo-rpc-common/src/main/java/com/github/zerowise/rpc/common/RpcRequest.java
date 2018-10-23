package com.github.zerowise.rpc.common;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Method;

/**
 ** @createtime : 2018/10/226:57 PM
 **/
public class RpcRequest {
    private String messageId;
    private String serviceName;
    private String methodName;
    private Class[] parameterTypes;
    private Object[] argumemts;

    public RpcRequest() {
    }

    public RpcRequest(String messageId, Method method, Object[] argumemts) {
        this.messageId = messageId;
        this.serviceName = method.getDeclaringClass().getName();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.argumemts = argumemts;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgumemts() {
        return argumemts;
    }

    public void setArgumemts(Object[] argumemts) {
        this.argumemts = argumemts;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageId", messageId)
                .append("serviceName", serviceName)
                .append("methodName", methodName)
                .toString();
    }
}
