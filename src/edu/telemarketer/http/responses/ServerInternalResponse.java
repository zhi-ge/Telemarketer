package edu.telemarketer.http.responses;

import edu.telemarketer.http.Status;

/**
 * 500服务器内部错误响应
 */
public class ServerInternalResponse extends Response {

    public ServerInternalResponse() {
        super(Status.INTERNAL_SERVER_ERROR_500);

    }
}
