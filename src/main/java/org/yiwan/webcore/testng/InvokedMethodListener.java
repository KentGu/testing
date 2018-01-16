package org.yiwan.webcore.testng;

//import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class InvokedMethodListener implements IInvokedMethodListener {

    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory
            .getLogger(InvokedMethodListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // TODO
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // TODO
    }
}
