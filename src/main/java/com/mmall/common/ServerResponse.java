package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @auther earlman
 * @create 9/12/17
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;


    private ServerResponse() {

    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }


    public static <T> ServerResponse<T> createSuccessResponse(T data) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.data = data;
        serverResponse.status = ResponseCode.SUCCESS.getCode();
        return serverResponse;
    }

    public static <T> ServerResponse<T> createSuccessResponse(String msg, T data) {
        ServerResponse serverResponse = createSuccessResponse(data);
        serverResponse.msg = msg;
        return serverResponse;
    }

    public static ServerResponse createErrorResponse(int status, String msg) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.status = status;
        serverResponse.msg = msg;
        return serverResponse;
    }

    public static ServerResponse createErrorResponse(String msg) {
        return createErrorResponse(ResponseCode.ERROR.getCode(), msg);
    }
}
