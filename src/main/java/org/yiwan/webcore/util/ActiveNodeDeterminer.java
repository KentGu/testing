package org.yiwan.webcore.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ActiveNodeDeterminer {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ActiveNodeDeterminer.class.getCanonicalName());

    /**
     * @param sessionId - A {@link SessionId} object that represents a valid session.
     * @param remoteAddress - A {@link String} The grid hub server information.
     */
    public synchronized static void getNodeInfoForSession(String remoteAddress, SessionId sessionId) {
        String[] serverInfo = remoteAddress.replace("http://","").replace("/wd/hub","").replace("/","").split(":");
        String gridHostName = serverInfo[0];
        int gridPort = Integer.valueOf(serverInfo[1]);

//        GridNode node = null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        try {
            URL url = new URL("http://" + gridHostName + ":" + gridPort + "/grid/api/testsession?session=" + sessionId);
            BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", url.toExternalForm());
            response = client.execute(new HttpHost(gridHostName, gridPort), r);
            JsonObject object = extractJson(response.getEntity());
            URL tempUrl = new URL(object.get("proxyId").getAsString());
//            node = new GridNode(tempUrl.getHost(), tempUrl.getPort());
            logger.info("Session " + sessionId + " was routed to " + tempUrl.getHost());
        } catch (Exception e) {
            String errorMsg = "Failed to acquire remote webdriver node and port info. Root cause: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.warn(e.getMessage(),e);
            }
        }
//        return node;
    }

    private synchronized static JsonObject extractJson(HttpEntity entity) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }
            return new JsonParser().parse(builder.toString()).getAsJsonObject();
        }
    }
}
