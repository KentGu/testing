package org.yiwan.webcore.bmproxy.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.test.ITestBase;

import java.io.IOException;

/**
 * Created by Kenny Wang on 4/5/2016.
 */
public class ScreenshotObserver extends SampleObserver {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotObserver.class);
    private ITestBase testCase;

    public ScreenshotObserver(ITestBase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        try {
            testCase.embedScreenshot();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
