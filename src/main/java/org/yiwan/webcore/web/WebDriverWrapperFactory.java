package org.yiwan.webcore.web;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
//import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.test.pojo.TestCapability;
import org.yiwan.webcore.util.ActiveNodeDeterminer;
import org.yiwan.webcore.util.IAction;
import org.yiwan.webcore.util.PropHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Kenny Wang
 */
public class WebDriverWrapperFactory {
    private final static Logger logger = LoggerFactory.getLogger(WebDriverWrapperFactory.class);

    private final String os;
    private final String os_version;
    private final String browser;
    private final String browser_version;
    private final String resolution;
    private final Proxy seleniumProxy;

    public WebDriverWrapperFactory(TestCapability testCapability) {
        this(testCapability, null);
    }

    public WebDriverWrapperFactory(TestCapability testCapability, Proxy seleniumProxy) {
        this.os = (testCapability == null || testCapability.getOs() == null) ? System.getProperty("os") : testCapability.getOs();
        this.os_version = (testCapability == null || testCapability.getOsVersion() == null) ? System.getProperty("os.version") : testCapability.getOsVersion();
        this.browser = (testCapability == null || testCapability.getBrowser() == null) ? System.getProperty("browser", PropHelper.DEFAULT_BROWSER) : testCapability.getBrowser();
        this.browser_version = (testCapability == null || testCapability.getBrowserVersion() == null) ? System.getProperty("browser.version") : testCapability.getBrowserVersion();
        this.resolution = (testCapability == null || testCapability.getResolution() == null) ? System.getProperty("resolution") : testCapability.getResolution();
        this.seleniumProxy = seleniumProxy;
    }

    public IWebDriverWrapper create() throws MalformedURLException {
        IWebDriverWrapper webDriverWrapper = null;
        if (PropHelper.DUMMY_TEST) {
            webDriverWrapper = new DummyDriverWrapper();
        } else {
            if (PropHelper.REMOTE) {
                logger.info("choosing remote test mode");
                webDriverWrapper = createRemoteWebDriverWrapper();
            } else {
                logger.info("choosing local test mode");
                webDriverWrapper = createWebDriverWrapper();
            }
//        use explicit wait to replace implicitly wait
//        webDriver.manage().timeouts().implicitlyWait(PropHelper.TIMEOUT_INTERVAL, TimeUnit.SECONDS);
        }
        logger.info("created web driver with following information\nSession ID: {}\n{}", webDriverWrapper.getSessionId(), webDriverWrapper.getCapabilities());
        return webDriverWrapper;
    }

    private IWebDriverWrapper createWebDriverWrapper() {
        switch (browser.toLowerCase()) {
            case "chrome":
                return createChromeDriverWrapper();
            case "ie":
                return createInternetExplorerDriverWrapper();
            case "htmlunit":
                return createHtmlUnitDriverWrapper();
            case "htmlunitjs":
                return createHtmlUnitJSDriverWrapper();
            case "phantomjs":
                return createPhantomJSDriverWrapper();
            default:
                return createFirefoxDriverWrapper();
        }
    }

    private IWebDriverWrapper createRemoteWebDriverWrapper() throws MalformedURLException {
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        if (os != null) {
            logger.info("choosing platform " + os + (os_version == null ? "" : " " + os_version));
            capabilities.setPlatform(Platform.fromString(os));
            if (os_version != null) {
                capabilities.setCapability("os_version", os_version);
            }
        }
        setRemoteBrowserCapabilities(capabilities);

//        resolve selenium grid issue of "org.openqa.selenium.WebDriverException: Error forwarding the new session Error forwarding the request Read timed out"
        final URL addressOfRemoteServer = new URL(PropHelper.REMOTE_ADDRESS);
        final RemoteWebDriver[] rwd = new RemoteWebDriver[1];
        new WebDriverActionExecutor().execute(new IAction() {
            @Override
            public void execute() {
                rwd[0] = new RemoteWebDriver(addressOfRemoteServer, capabilities);
            }
        });

//        HttpClient.Factory factory = new ApacheHttpClient.Factory(new HttpClientFactory(PropHelper.REMOTE_CONNECTION_TIMEOUT, PropHelper.REMOTE_SOCKET_TIMEOUT));
//        HttpCommandExecutor executor = new HttpCommandExecutor(Collections.<String, CommandInfo>emptyMap(), addressOfRemoteServer, factory);
//        RemoteWebDriver rwd = new RemoteWebDriver(executor, capabilities);

        rwd[0].setFileDetector(new LocalFileDetector());
        IWebDriverWrapper webDriverWrapper =  wrapWebDriver(rwd[0]);
        ActiveNodeDeterminer.getNodeInfoForSession(PropHelper.REMOTE_ADDRESS, webDriverWrapper.getSessionId());
        return webDriverWrapper;
    }

    private IWebDriverWrapper wrapWebDriver(WebDriver webDriver) {
        switch (browser.toLowerCase()) {
            case "chrome":
                return new ChromeDriverWrapper(webDriver);
            case "ie":
                return new InternetExplorerDriverWrapper(webDriver);
            case "htmlunit":
                return new HtmlUnitDriverWrapper(webDriver);
            case "htmlunitjs":
                return new HtmlUnitDriverWrapper(webDriver);
            case "phantomjs":
                return new PhantomJSDriverWrapper(webDriver);
            default:
                return new FirefoxDriverWrapper(webDriver);
        }
    }

    private void setRemoteBrowserCapabilities(DesiredCapabilities capabilities) {
        setBrowserCapabilities(capabilities);
        switch (browser.toLowerCase()) {
            case "ie":
                setInternetExplorerCapabilities(capabilities);
                break;
            case "chrome":
                setChromeCapabilities(capabilities);
                break;
            case "htmlunit":
                setHtmlUnitCapabilities(capabilities);
                break;
            case "htmlunitjs":
                setHtmlUnitCapabilities(capabilities);
                break;
            case "phantomjs":
                setPhantomJSCapabilities(capabilities);
                break;
            default:
                setFirefoxCapabilities(capabilities);
        }
    }

    private IWebDriverWrapper createHtmlUnitDriverWrapper() {
        DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
        setBrowserCapabilities(capabilities);
        setHtmlUnitCapabilities(capabilities);
        return new HtmlUnitDriverWrapper(new HtmlUnitDriver(capabilities));
    }

    private IWebDriverWrapper createHtmlUnitJSDriverWrapper() {
        DesiredCapabilities capabilities = DesiredCapabilities.htmlUnitWithJs();
        setBrowserCapabilities(capabilities);
        setHtmlUnitCapabilities(capabilities);
        return new HtmlUnitDriverWrapper(new HtmlUnitDriver(capabilities));
    }

    private IWebDriverWrapper createPhantomJSDriverWrapper() {
        System.setProperty(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PropHelper.PHANTOMJS_PATH);
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        setBrowserCapabilities(capabilities);
        setPhantomJSCapabilities(capabilities);
        return new PhantomJSDriverWrapper(new PhantomJSDriver(capabilities));
    }

    private IWebDriverWrapper createChromeDriverWrapper() {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, PropHelper.CHROME_WEBDRIVER);
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        setBrowserCapabilities(capabilities);
        setChromeCapabilities(capabilities);
        return new ChromeDriverWrapper(new ChromeDriver(capabilities));
    }

    private IWebDriverWrapper createInternetExplorerDriverWrapper() {
        System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, PropHelper.IE_WEBDRIVER);
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        setBrowserCapabilities(capabilities);
        setInternetExplorerCapabilities(capabilities);
        return new InternetExplorerDriverWrapper(new InternetExplorerDriver(capabilities));
    }

    private IWebDriverWrapper createFirefoxDriverWrapper() {
//        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_BINARY, PropHelper.FIREFOX_PATH);
        System.setProperty("webdriver.gecko.driver", PropHelper.MARIONETTE_WEBDRIVER);
        FirefoxBinary firefoxBinary;
        if (PropHelper.FIREFOX_PATH != null && !PropHelper.FIREFOX_PATH.trim().isEmpty()) {
            firefoxBinary = new FirefoxBinary(new File(PropHelper.FIREFOX_PATH));
        } else {
            firefoxBinary = new FirefoxBinary();
        }

        FirefoxProfile profile = new FirefoxProfile();
        setFirefoxProfile(profile);

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        setBrowserCapabilities(capabilities);
        setFirefoxCapabilities(capabilities);

        if (capabilities.getCapability("marionette").equals(true)) {
//            return new FirefoxDriverWrapper(new MarionetteDriver(capabilities));
            System.setProperty("webdriver.firefox.marionette", PropHelper.MARIONETTE_WEBDRIVER);
        }
        return new FirefoxDriverWrapper(new FirefoxDriver(firefoxBinary, profile, capabilities));
    }

    private void setBrowserCapabilities(DesiredCapabilities capabilities) {
        logger.info("choosing browser " + browser + (browser_version == null ? "" : " " + browser_version));
        if (browser_version != null) {
            capabilities.setVersion(browser_version);
        }
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, PropHelper.ACCEPT_SSL_CERTS);
        capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, PropHelper.NATIVE_EVENTS);
        if (PropHelper.UNEXPECTED_ALERT_BEHAVIOUR != null) {
            capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.fromString(PropHelper.UNEXPECTED_ALERT_BEHAVIOUR));
        }
        if (seleniumProxy != null) {
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        }
    }

    private void setHtmlUnitCapabilities(DesiredCapabilities capabilities) {
        capabilities.setBrowserName(BrowserType.HTMLUNIT);
    }

    private void setPhantomJSCapabilities(DesiredCapabilities capabilities) {
        capabilities.setBrowserName(BrowserType.PHANTOMJS);
        capabilities.setCapability("takesScreenshot", true);
        String[] phantomjsCliArgs = StringUtils.split(PropHelper.PHANTOMJS_CLI_ARGS.trim());
        if (PropHelper.PHANTOMJS_CLI_ARGS != null && phantomjsCliArgs.length > 0) {
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, Arrays.asList(phantomjsCliArgs));
        }
        String[] phantomjsGhostdriverCliArgs = StringUtils.split(PropHelper.PHANTOMJS_GHOSTDRIVER_CLI_ARGS.trim());
        if (PropHelper.PHANTOMJS_GHOSTDRIVER_CLI_ARGS != null && phantomjsGhostdriverCliArgs.length > 0) {
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, Arrays.asList(phantomjsGhostdriverCliArgs));
        }
    }

    private void setInternetExplorerCapabilities(DesiredCapabilities capabilities) {
        capabilities.setBrowserName(BrowserType.IE);
        capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, PropHelper.IGNORE_PROTECTED_MODE_SETTINGS);
        if (PropHelper.INITIAL_BROWSER_URL != null) {
            capabilities.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, PropHelper.INITIAL_BROWSER_URL);
        }
        capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, PropHelper.IGNORE_ZOOM_SETTING);
        capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, PropHelper.REQUIRE_WINDOW_FOCUS);
        capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, PropHelper.ENABLE_PERSISTENT_HOVER);
//        capabilities.setCapability("disable-popup-blocking", true);
    }

    private void setFirefoxCapabilities(DesiredCapabilities capabilities) {
        capabilities.setBrowserName(BrowserType.FIREFOX);
        if (browser_version != null && Integer.parseInt(browser_version) > 47) {
            logger.info("choosing marionette mode");
            capabilities.setCapability("marionette", true);
        }
    }

    private void setChromeCapabilities(DesiredCapabilities capabilities) {
        capabilities.setBrowserName(BrowserType.CHROME);
    }

    private void setFirefoxProfile(FirefoxProfile profile) {
        profile.setAcceptUntrustedCertificates(true);
        profile.setPreference("browser.download.manager.showWhenStarting", false);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/plain,text/xml,text/csv,image/jpeg,application/zip,application/vnd.ms-excel,application/pdf,application/xml");
//        profile.setEnableNativeEvents(PropHelper.NATIVE_EVENTS);
//        profile.setPreference("browser.download.folderList", 2);
//        profile.setPreference("browser.download.dir", "D:\\mydownloads\\");
//        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
//        profile.setPreference("browser.helperApps.neverAsk.openFile", "text/csv,application/pdf,application/x-msexcel,application/excel,application/x-excel,application/vnd.ms-excel,application/x-excel,application/x-msexcel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml");
//        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv,application/pdf,application/x-msexcel,application/excel,application/x-excel,application/excel,application/x-excel,application/excel,application/vnd.ms-excel,application/x-excel,application/x-msexcel,image/png,image/pjpeg,image/jpeg,text/html,text/plain,application/msword,application/xml,application/excel");
//        Then add the proxy setting to the Firefox profile we created
//        profile.setPreference("network.proxy.http", "localhost");
//        profile.setPreference("network.proxy.http_port", "8888");
    }
}