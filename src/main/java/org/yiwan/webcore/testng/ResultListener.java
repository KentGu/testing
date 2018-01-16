package org.yiwan.webcore.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.lang.reflect.Method;

/**
 * @author Kenny Wang
 */
public class ResultListener extends TestListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(ResultListener.class);

    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        try {
            Method method = result.getInstance().getClass().getMethod("onTestFailure", ITestResult.class);
            method.invoke(result.getInstance(), result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        super.onTestSkipped(result);

        try {
            Method method = result.getInstance().getClass().getMethod("onTestSkipped", ITestResult.class);
            method.invoke(result.getInstance(), result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result);

        try {
            Method method = result.getInstance().getClass().getMethod("onTestSuccess", ITestResult.class);
            method.invoke(result.getInstance(), result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        super.onTestStart(result);

        try {
            Method method = result.getInstance().getClass().getMethod("onTestStart", ITestResult.class);
            method.invoke(result.getInstance(), result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
