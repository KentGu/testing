package org.yiwan.webcore.test;

import org.assertj.core.api.SoftAssertions;
import org.testng.ITestResult;
import org.yiwan.webcore.bmproxy.ProxyWrapper;
import org.yiwan.webcore.bmproxy.TimestampWriter;
import org.yiwan.webcore.database.DatabaseWrapper;
import org.yiwan.webcore.test.pojo.TestCapability;
import org.yiwan.webcore.test.pojo.TestEnvironment;
import org.yiwan.webcore.web.IPageManager;
import org.yiwan.webcore.web.IWebDriverWrapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Map;

public interface ITestBase {

    String getDownloadFile();

    String getDefaultDownloadFileName();

    String getTransactionName();

    String getSuiteName();

    String getTestName();

    String getScenarioId();

    String getFeatureId();

    String getSuiteTestSeparator();

    boolean isSkipTest();

    boolean isPrepareToDownload();

    Map<String, String> getTestMap();

    IWebDriverWrapper getWebDriverWrapper();

    DatabaseWrapper getDatabaseWrapper();

    ITestDataManager getTestDataManager();

    IPageManager getPageManager();

    ProxyWrapper getProxyWrapper();

    TestCapability getTestCapability();

    TestEnvironment getTestEnvironment();

    TimestampWriter getTimestampWriter();

    SoftAssertions getSoftAssertions();

    void setDownloadFile(String downloadFile);

    void setDefaultDownloadFileName(String defaultDownloadFileName);

    void setScenarioId(String scenarioId);

    void setFeatureId(String featureId);

    void setSkipTest(boolean skipTest);

    void setPrepareToDownload(boolean prepareToDownload);

    void setTestDataManager(ITestDataManager testDataManager);

    void setPageManager(IPageManager pageManager);

    /**
     * log the content into the report
     *
     * @param s
     */
    void report(String s);

    /**
     * Invoked each time before a test will be invoked. The
     * <code>ITestResult</code> is only partially filled with the references to
     * class, method, start millis and status.
     *
     * @param result the partially filled <code>ITestResult</code>
     * @see ITestResult#STARTED
     */
    void onTestStart(ITestResult result);

    /**
     * Invoked each time a test succeeds.
     *
     * @param result <code>ITestResult</code> containing information about the run
     *               test
     * @see ITestResult#SUCCESS
     */
    void onTestSuccess(ITestResult result);

    /**
     * Invoked each time a test fails.
     *
     * @param result <code>ITestResult</code> containing information about the run
     *               test
     * @see ITestResult#FAILURE
     */
    void onTestFailure(ITestResult result) throws IOException;

    /**
     * Invoked each time a test is skipped.
     *
     * @param result <code>ITestResult</code> containing information about the run
     *               test
     * @see ITestResult#SKIP
     */
    void onTestSkipped(ITestResult result);

    void embedScreenshot() throws IOException;

    void embedTestLog() throws IOException;

    void embedTestData(Object o) throws Exception;

    void startTransaction(String transactionName);

    void stopTransaction();

    void createProxyWrapper();

    void createWebDriverWrapper() throws MalformedURLException;

    void setUpTest() throws Exception;

    void setUpTest(boolean proxied) throws Exception;

    void tearDownTest() throws Exception;

    Charset getDownloadFileCharset();

    void setDownloadFileCharset(Charset downloadFileCharset);

    FileFormat getDownloadFileFormat();

    void setDownloadFileFormat(FileFormat downloadFileFormat);

    void prepareToDownloadTextFile();

    void prepareToDownloadBinaryFile();
}