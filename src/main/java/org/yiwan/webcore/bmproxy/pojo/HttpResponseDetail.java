package org.yiwan.webcore.bmproxy.pojo;

import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

/**
 * Created by Kenny Wang on 4/2/2016.
 */
public class HttpResponseDetail {
    private long responseTimestamp;
    private HttpResponse httpResponse;
    private HttpMessageContents httpMessageContents;
    private HttpMessageInfo httpMessageInfo;

    public HttpResponseDetail(long responseTimestamp, HttpResponse httpResponse, HttpMessageContents httpMessageContents, HttpMessageInfo httpMessageInfo) {
        this.responseTimestamp = responseTimestamp;
        this.httpResponse = httpResponse;
        this.httpMessageContents = httpMessageContents;
        this.httpMessageInfo = httpMessageInfo;
    }

    public long getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(long responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
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