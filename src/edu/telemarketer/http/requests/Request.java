package edu.telemarketer.http.requests;

import edu.telemarketer.http.exceptions.IllegalRequestException;
import edu.telemarketer.http.requests.mime.MIMEData;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Map;

/**
 * Http请求
 */
public class Request {

    private final RequestHeader header;
    private final RequestBody body;


    private Request(RequestHeader header, RequestBody body) {
        this.header = header;
        this.body = body;
    }


    public static Request parseRequest(SocketChannel channel) throws IllegalRequestException, IOException {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer); //IOException
        buffer.flip();
        int remaining = buffer.remaining();
        byte[] bytes = new byte[remaining];
        buffer.get(bytes);
        int position = 0;
        for (int i = 0; i < remaining; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                position = i;
                i += 2;
            }
            if (i + 1 < remaining && bytes[i] == '\r' && bytes[i + 1] == '\n') {
                break;
            }
        }
        byte[] head = Arrays.copyOf(bytes, position);
        RequestHeader requestHeader = RequestHeader.parseHeader(head); //IOException
        int contentLength = requestHeader.getContentLength();
        buffer.position(position + 4);
        ByteBuffer bodyBuffer = ByteBuffer.allocate(contentLength);
        bodyBuffer.put(buffer);
        while (bodyBuffer.hasRemaining()) {
            channel.read(bodyBuffer); //IOException
        }
        byte[] body = bodyBuffer.array();
        RequestBody requestBody = new RequestBody();
        if (body.length != 0) {
            RequestBody.parseBody(body, requestHeader);
        }
        return new Request(requestHeader, requestBody);
    }

    public boolean containKey(String key) {
        return header.containKey(key);
    }

    public Map<String, String> getQueryMap() {
        return header.getQueryMap();
    }

    public String queryValue(String key) {
        return header.queryValue(key);
    }

    public boolean formContainKey(String key) {
        return body.formContainKey(key);
    }

    public boolean mimeContainKey(String key) {
        return body.mimeContainKey(key);
    }


    public Map<String, String> getFormMap() {
        return body.getFormMap();
    }

    public Map<String, MIMEData> getMimeMap() {
        return body.getMimeMap();
    }

    public String formValue(String key) {
        return body.formValue(key);
    }

    public MIMEData mimeValue(String key) {
        return body.mimeValue(key);
    }

    public String getURI() {
        return header.getURI();
    }


    public String getMethod() {
        return header.getMethod();
    }
}
