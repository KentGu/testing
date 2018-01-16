package org.yiwan.webcore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by Kenny Wang on 5/18/2016.
 */
public class PropHelperTest {
    private static final Logger logger = LoggerFactory.getLogger(PropHelperTest.class);

    @Test
    public void testServerInfoProperty() throws Exception {
        logger.info("\n" + PropHelper.SERVER_INFO);
    }

    @Test
    public void testGetServerInfo() {
//        System.setProperty("server.url", "http://192.168.1.1/,http://192.168.1.2/");
        logger.info(PropHelper.SERVER_URL);
        logger.info(PropHelper.SERVER_INFO);
    }
}
