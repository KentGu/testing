package org.yiwan.webcore.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.yiwan.webcore.test.TestCaseManager;
import org.yiwan.webcore.util.PropHelper;

public class TestCaseManagerTest {
	private static final Logger logger = LoggerFactory.getLogger(TestEnvironmentTest.class);
	
    @Test
    public void takeTakeTestEnvironment() throws Exception {
    	logger.info(PropHelper.SERVER_INFO);
        assertThat(TestCaseManager.takeTestEnvironment().getApplicationServers().get(0).getUrl()).isNull();
    }
}
