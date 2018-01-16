package org.yiwan.webcore.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Hook implements IHookable {

    private final static Logger logger = LoggerFactory.getLogger(Hook.class);

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        // Preferably initialized in a @Configuration method

        // get the skip test value to determine whether to skip next methods on
        // such test case
        Method method;
        boolean skipTest = false;
        try {
            method = testResult.getInstance().getClass().getMethod("isSkipTest");
            skipTest = (boolean) method.invoke(testResult.getInstance());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        if (skipTest)
            throw new SkipException("skip method " + testResult.getTestClass().getName() + "." + testResult.getName());
        callBack.runTestMethod(testResult);
    }

}
