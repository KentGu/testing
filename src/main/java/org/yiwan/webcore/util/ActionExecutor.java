package org.yiwan.webcore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Kenny Wang on 8/22/2016.
 */
public class ActionExecutor {
    private final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    private int max_retry_times;
    private int max_session_retry_times;

    public ActionExecutor() {
        this(2);
    }

    public ActionExecutor(int max_retry_times) {
        this.max_retry_times = max_retry_times;
    }

    /**
     * @param action
     * @param exceptionType any exception such as UnreachableBrowserException, TimeoutException, WebDriverException
     * @param <T>
     */
    public <T extends Exception> void execute(IAction action, Class<T> exceptionType) {
        execute(action, 0, exceptionType);
    }

    private <T extends Exception> void execute(IAction action, int retries, Class<T> exceptionType) {
        if (retries > max_retry_times) {
            throw new RuntimeException("exceed retry times, max retry times is " + max_retry_times);
        } else {
            try {
                action.execute();
            }catch (Exception e) {
                if (exceptionType.isInstance(e)) {
                    logger.warn(String.format("retry %d time(s) due to %s:\n%s", ++retries, exceptionType.getName(), e.getMessage()), e);
                    try {
                        Thread.sleep(PropHelper.ACTION_EXECUTE_INTERVAL);
                        execute(action, retries, exceptionType);
                    } catch (InterruptedException ie) {
                        logger.error(ie.getMessage(), ie);
                    }
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
