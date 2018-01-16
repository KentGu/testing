package org.yiwan.webcore.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.yiwan.webcore.util.PropHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Kenny Wang
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private final static Logger logger = LoggerFactory
            .getLogger(RetryAnalyzer.class);

    private static final int MAX_RETRY_COUNT = PropHelper.TEST_RETRY_COUNT;
    private int retryCount = 0;

    @Override
    public synchronized boolean retry(ITestResult testResult) {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            return true;
        }

        // set the skip test to true on such test case instance
        try {
            Method method = testResult.getInstance().getClass().getMethod("setSkipTest", boolean.class);
            method.invoke(testResult.getInstance(), true);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }
}
