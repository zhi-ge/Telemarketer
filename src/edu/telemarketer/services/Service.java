package edu.telemarketer.services;

import edu.telemarketer.http.requests.Request;
import edu.telemarketer.http.responses.Response;

/**
 * 服务接口
 */
public interface Service {
    Response service(Request request);
}
