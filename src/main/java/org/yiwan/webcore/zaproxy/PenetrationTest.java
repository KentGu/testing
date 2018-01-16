package org.yiwan.webcore.zaproxy;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.test.TestCaseManager;
import org.yiwan.webcore.test.pojo.ApplicationServer;
import org.yiwan.webcore.test.pojo.TestEnvironment;
import org.yiwan.webcore.util.PropHelper;
import org.yiwan.webcore.zaproxy.model.ScanInfo;
import org.yiwan.webcore.zaproxy.model.ScanResponse;
import org.zaproxy.clientapi.core.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PenetrationTest {
    private static final Logger logger = LoggerFactory.getLogger(PenetrationTest.class);
    private static final String API_KEY = PropHelper.ZAP_API_KEY;
    private static final String MEDIUM = "MEDIUM";
    private static final String HIGH = "HIGH";
    private static final String[] policyNames = {"directory-browsing", "cross-site-scripting", "sql-injection", "path-traversal", "remote-file-inclusion", "server-side-include", "script-active-scan-rules", "server-side-code-injection", "external-redirect", "crlf-injection"};
    private static ClientApi clientApi;

    static {
        if (PropHelper.ENABLE_PENETRATION_TEST) {
            try {
                doPreActions();
//                add shutdown hook for crawling, scanning and get penetration test report in the end
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        doPostActions();
                    }
                });
            } catch (ClientApiException e) {
                logger.error("skip penetration testing due to following error", e);
            }
        }
    }

    static void doPreActions() throws ClientApiException {
        clientApi = new ClientApi(PropHelper.ZAP_SERVER_HOST, PropHelper.ZAP_SERVER_PORT);
        ActiveScanner.config();
        Core.config();
        Spider.config();
    }

    static void doPostActions() {
        try {
            crawl();
            scan();
            generateReport();
            Core.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static void crawl() {
        for (TestEnvironment testEnvironment : TestCaseManager.getTestEnvironments()) {
            for (ApplicationServer applicationServer : testEnvironment.getApplicationServers()) {
                try {
                    String url = applicationServer.getUrl();
                    Spider.scan(url);
                    Spider.waitScanningDone(Spider.getLastScannerId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    static void scan() {
        for (TestEnvironment testEnvironment : TestCaseManager.getTestEnvironments()) {
            for (ApplicationServer applicationServer : testEnvironment.getApplicationServers()) {
                try {
                    String url = applicationServer.getUrl();
                    ActiveScanner.scan(url);
                    ActiveScanner.waitScanningDone(ActiveScanner.getLastScannerId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    static void generateReport() throws ClientApiException, IOException {
        byte[] bytes = Core.getHtmlReport();
        File file = new File(PropHelper.PENETRATION_TEST_HTML_REPORT_FILE);
        FileUtils.writeByteArrayToFile(file, bytes);
    }

    static class Core {
        static void config() throws ClientApiException {
            //may set unique release name into second parameter
            newSession("", "");
        }

        static ApiResponse newSession(String name, String overwrite) throws ClientApiException {
            return clientApi.core.newSession(API_KEY, name, overwrite);
        }

        static ApiResponse deleteAllAlerts() throws ClientApiException {
            return clientApi.core.deleteAllAlerts(API_KEY);
        }

        static byte[] getXmlReport() throws ClientApiException {
            return clientApi.core.xmlreport(API_KEY);
        }

        static byte[] getHtmlReport() throws ClientApiException {
            return clientApi.core.htmlreport(API_KEY);
        }

        static void shutdown() throws ClientApiException {
            clientApi.core.shutdown(API_KEY);
        }
    }

    static class ActiveScanner {

        static void config() throws ClientApiException {
            removeAllScans();
            setAlertAndAttackStrength();
            PassiveScanner.setEnabled(true);
        }

        static int getLastScannerId() throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getLastScan().getId();
        }

        static int getScanningProgress(int id) throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getScanById(id).getProgress();
        }

        static ScanInfo.State getScanningState(int id) throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getScanById(id).getState();
        }

        static List<String> getScanningResults(int id) throws ClientApiException {
            List<String> results = new ArrayList<>();
            ApiResponseList responseList = (ApiResponseList) clientApi.spider.results(String.valueOf(id));
            for (ApiResponse response : responseList.getItems()) {
                results.add(((ApiResponseElement) response).getValue());
            }
            return results;
        }

        static void waitScanningDone(int id) throws ClientApiException, InterruptedException {
            logger.info("active scanning...");
            int progress = 0;
            while (progress < 100) {
                progress = getScanningProgress(id);
                logger.info(String.format("scanner id=%d scanning progress=%d", id, progress));
                Thread.sleep(1000);
            }
            logger.info("active scanning done");
        }

        static ApiResponse removeAllScans() throws ClientApiException {
            return clientApi.ascan.removeAllScans(API_KEY);
        }

        static ApiResponse setAttackStrength(String scannerId, String strength) throws ClientApiException {
            return setAttackStrength(scannerId, strength, null);
        }

        static ApiResponse setAttackStrength(String scannerId, String strength, String scanPolicyName) throws ClientApiException {
            return clientApi.ascan.setScannerAttackStrength(API_KEY, scannerId, strength, scanPolicyName);
        }

        static ApiResponse setScannerAlertThreshold(String scannerId, String threshold) throws ClientApiException {
            return clientApi.ascan.setScannerAlertThreshold(API_KEY, scannerId, threshold, null);
        }

        static ApiResponse enableScanners(String ids) throws ClientApiException {
            return clientApi.ascan.enableScanners(API_KEY, ids);
        }

        static ApiResponse disableScanners(String ids) throws ClientApiException {
            return clientApi.ascan.disableScanners(API_KEY, ids);
        }

        static ApiResponse disableAllScanners(String scanPolicyName) throws ClientApiException {
            return clientApi.ascan.disableAllScanners(API_KEY, scanPolicyName);
        }

        static ApiResponse enableAllScanners(String scanPolicyName) throws ClientApiException {
            return clientApi.ascan.enableAllScanners(API_KEY, scanPolicyName);
        }

        static ApiResponse scan(String url) throws ClientApiException {
            return scan(url, "true", "false", null, null, null);
        }

        static ApiResponse scan(String url, String recurse, String inScopeOnly, String scanPolicyName, String method, String postData) throws ClientApiException {
            return clientApi.ascan.scan(API_KEY, url, recurse, inScopeOnly, scanPolicyName, method, postData);
        }

        static ApiResponse scans() throws ClientApiException {
            return clientApi.ascan.scans();
        }

        static void excludeFromScanner(String regex) throws ClientApiException {
            clientApi.ascan.excludeFromScan(API_KEY, regex);
        }
    }

    static class PassiveScanner {
        static void setEnabled(boolean enabled) throws ClientApiException {
            clientApi.pscan.setEnabled(API_KEY, Boolean.toString(enabled));
        }
    }

    static class Spider {
        static void config() throws ClientApiException {
//            Spider.excludeFromScan(myApp.LOGOUT_URL);
            Spider.setOptionThreadCount(5);
            Spider.setOptionMaxDepth(5);
            Spider.setOptionPostForm(false);
        }

        static int getLastScannerId() throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getLastScan().getId();
        }

        static int getScanningProgress(int id) throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getScanById(id).getProgress();
        }

        static ScanInfo.State getScanningState(int id) throws ClientApiException {
            ApiResponseList apiResponseList = (ApiResponseList) scans();
            return (new ScanResponse(apiResponseList)).getScanById(id).getState();
        }

        static List<String> getScanningResults(int id) throws ClientApiException {
            List<String> results = new ArrayList<>();
            ApiResponseList responseList = (ApiResponseList) clientApi.spider.results(String.valueOf(id));
            for (ApiResponse response : responseList.getItems()) {
                results.add(((ApiResponseElement) response).getValue());
            }
            return results;
        }

        static void waitScanningDone(int id) throws ClientApiException, InterruptedException {
            logger.info("spider scanning...");
            int progress = 0;
            while (progress < 100) {
                progress = getScanningProgress(id);
                logger.info(String.format("scanner id=%d scanning progress=%d", id, progress));
                Thread.sleep(1000);
            }
            logger.info("spider scanning done");
        }

        static ApiResponse scans() throws ClientApiException {
            return clientApi.spider.scans();
        }

        static ApiResponse excludeFromScan(String regex) throws ClientApiException {
            return clientApi.spider.excludeFromScan(API_KEY, regex);
        }

        static ApiResponse setOptionMaxDepth(int depth) throws ClientApiException {
            return clientApi.spider.setOptionMaxDepth(API_KEY, depth);
        }

        static ApiResponse setOptionPostForm(boolean post) throws ClientApiException {
            return clientApi.spider.setOptionPostForm(API_KEY, post);
        }

        static ApiResponse setOptionThreadCount(int threads) throws ClientApiException {
            return clientApi.spider.setOptionThreadCount(API_KEY, threads);
        }

        static ApiResponse scan(String url) throws ClientApiException {
            return scan(url, null, true, null);
        }

        static ApiResponse scan(String url, Integer maxChildren, boolean recurse, String contextName) throws ClientApiException {
            String contextNameString = contextName == null ? "Default Context" : contextName; //Something must be specified else zap throws an exception
            String maxChildrenString = maxChildren == null ? null : String.valueOf(maxChildren);
            return clientApi.spider.scan(API_KEY, url, maxChildrenString, String.valueOf(recurse), contextNameString);
        }
    }

    static List<Alert> getAlerts() throws ClientApiException {
        return getAlerts(-1, -1);
    }

    static List<Alert> getAlerts(int start, int count) throws ClientApiException {
        return getAlerts("", start, count);
    }

    static List<Alert> getAlerts(String baseurl, int start, int count) throws ClientApiException {
        return clientApi.getAlerts(baseurl, start, count);
    }

    static void setAlertAndAttackStrength() throws ClientApiException {
        for (String policyName : policyNames) {
            String ids = enableScanners(policyName);
            for (String id : ids.split(",")) {
                ActiveScanner.setScannerAlertThreshold(id, MEDIUM);
                ActiveScanner.setAttackStrength(id, HIGH);
            }
        }
    }

    static String enableScanners(String policyName) throws ClientApiException {
        String scannerIds = null;
        switch (policyName.toLowerCase()) {
            case "directory-browsing":
                scannerIds = "0";
                break;
            case "cross-site-scripting":
                scannerIds = "40012,40014,40016,40017";
                break;
            case "sql-injection":
                scannerIds = "40018";
                break;
            case "path-traversal":
                scannerIds = "6";
                break;
            case "remote-file-inclusion":
                scannerIds = "7";
                break;
            case "server-side-include":
                scannerIds = "40009";
                break;
            case "script-active-scan-rules":
                scannerIds = "50000";
                break;
            case "server-side-code-injection":
                scannerIds = "90019";
                break;
            case "remote-os-command-injection":
                scannerIds = "90020";
                break;
            case "external-redirect":
                scannerIds = "20019";
                break;
            case "crlf-injection":
                scannerIds = "40003";
                break;
            case "source-code-disclosure":
                scannerIds = "42,10045,20017";
                break;
            case "shell-shock":
                scannerIds = "10048";
                break;
            case "remote-code-execution":
                scannerIds = "20018";
                break;
            case "ldap-injection":
                scannerIds = "40015";
                break;
            case "xpath-injection":
                scannerIds = "90021";
                break;
            case "xml-external-entity":
                scannerIds = "90023";
                break;
            case "padding-oracle":
                scannerIds = "90024";
                break;
            case "el-injection":
                scannerIds = "90025";
                break;
            case "insecure-http-methods":
                scannerIds = "90028";
                break;
            case "parameter-pollution":
                scannerIds = "20014";
                break;
            default:
                throw new RuntimeException(String.format("%s was not a valid policy name", policyName));
        }
        ActiveScanner.enableScanners(scannerIds);
        return scannerIds;
    }
}
