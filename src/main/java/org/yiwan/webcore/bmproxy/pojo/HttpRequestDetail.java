package org.yiwan.webcore.bmproxy.pojo;

import io.netty.handler.codec.http.HttpRequest;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

/**
 * Created by Kenny Wang on 4/2/2016.
 */
public class HttpRequestDetail {
    private long requestTimestamp;
    private HttpRequest httpRequest;
    private HttpMessageContents httpMessageContents;
    private HttpMessageInfo httpMessageInfo;

    public HttpRequestDetail(long requestTimestamp, HttpRequest httpRequest, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
        this.requestTimestamp = requestTimestamp;
        this.httpRequest = httpRequest;
        this.httpMessageContents = httpMessageContents;
        this.httpMessageInfo = httpMessageInfo;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpMessageContents getHttpMessageContents() {
        return httpMessageContents;
    }

    public void setHttpMessageContents(HttpMessageContents httpMessageContents) {
        this.httpMessageContents = httpMessageContents;
    }

    public HttpMessageInfo getHttpMessageInfo() {
        return httpMessageInfo;
    }

    public void setHttpMessageInfo(HttpMessageInfo httpMessageInfo) {
        this.httpMessageInfo = httpMessageInfo;
    }
}