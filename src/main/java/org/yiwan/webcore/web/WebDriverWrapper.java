package org.yiwan.webcore.web;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yiwan.webcore.locator.Locator;
import org.yiwan.webcore.test.TestCaseManager;
import org.yiwan.webcore.util.PropHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class WebDriverWrapper implements IWebDriverWrapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private WebDriver driver;
    private JavascriptExecutor js;
    private Wait<WebDriver> wait;

    public WebDriverWrapper(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.wait = new WebDriverWait(driver, PropHelper.TIMEOUT_INTERVAL, PropHelper.TIMEOUT_POLLING_INTERVAL)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .ignoring(UnreachableBrowserException.class)
                .ignoring(InvalidElementStateException.class);
    }

    public SoftAssertions getSoftAssertions() {
        return TestCaseManager.getTestCase().getSoftAssertions();
    }

    @Override
    public IBrowseNavigation navigate() {
        return new BrowseNavigation();
    }

    @Override
    public IWebDriverWrapper maximize() {
        logger.info("maximizing browser");
        driver.manage().window().maximize();
        return this;
    }

    @Override
    public IWebDriverWrapper close() {
        logger.info("closing browser tab with title {}", getPageTitle());
        driver.close();
        return this;
    }

    @Override
    public IWebDriverWrapper closeAll() {
        logger.info("closing all browser tabs");
        for (String handle : driver.getWindowHandles()) {
            switchTo().window(handle);
            close();
        }
        return this;
    }

    @Override
    public IWebDriverWrapper quit() {
        logger.info("quiting driver");
        driver.quit();
        return this;
    }

    @Override
    public IWebDriverWrapper deleteAllCookies() {
        logger.info("deleting all cookies");
        driver.manage().deleteAllCookies();
        return this;
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    @Override
    public ITargetLocatorWrapper switchTo() {
        return new TargetLocatorWrapper();
    }

    @Override
    public String getPageTitle() {
        return driver.getTitle();
    }

    @Override
    public SessionId getSessionId() {
        return ((RemoteWebDriver) driver).getSessionId();
    }

    @Override
    public Capabilities getCapabilities() {
        return ((RemoteWebDriver) driver).getCapabilities();
    }

    @Override
    public IWebDriverWrapper clickSmartly(Locator... locators) {
        for (Locator locator : locators) {
            if (element(locator).clickSmartly()) {
                break;
            }
        }
        return this;
    }

    @Override
    public IWebDriverWrapper inputSmartly(String value, Locator... locators) {
        for (Locator locator : locators) {
            if (element(locator).inputSmartly(value)) {
                break;
            }
        }
        return this;
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        TakesScreenshot takesScreenshot;
//        if (PropHelper.REMOTE) {
//            // RemoteWebDriver does not implement the TakesScreenshot class if
//            // the driver does have the Capabilities to take a screenshot then
//            // Augmenter will add the TakesScreenshot methods to the instance
//            takesScreenshot = (TakesScreenshot) (new Augmenter().augment(driver));
//        } else {
        takesScreenshot = (TakesScreenshot) driver;
//        }
        return takesScreenshot.getScreenshotAs(target);
    }

    @Override
    public IActionsWrapper actions() {
        return new ActionsWrapper();
    }

    @Override
    public Object executeScript(final String script, final Object... args) {
        return js.executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return js.executeAsyncScript(script, args);
    }

    @Override
    public IWebElementWrapper element(Locator locator) {
        return new WebLocatorWrapper(locator);
    }

    private IWebElementWrapper element(WebElement webElement) {
        return new WebElementWrapper(webElement);
    }

    @Override
    public IFluentLocatorWait waitThat(Locator locator) {
        return new FluentLocatorWait(locator);
    }

    @Override
    public IFluentWait waitThat() {
        return new FluentWait();
    }

    @Override
    public IFluentLocatorAssertion assertThat(Locator locator) {
        return new FluentLocatorAssertion(locator);
    }

    @Override
    public IFluentLocatorAssertion assertThat(IWebElementWrapper webElementWrapper) {
        return new FluentWebElementWrapperAssertion(webElementWrapper);
    }

    @Override
    public IFluentAssertion assertThat() {
        return new FluentAssertion();
    }

    @Override
    public IFluentLocatorAssertion validateThat(Locator locator) {
        return new FluentLocatorValidation(locator);
    }

    @Override
    public IFluentAssertion validateThat() {
        return new FluentValidation();
    }

    @Override
    public IAlertWrapper alert() {
        return new AlertWrapper();
    }

    public IWebDriverWrapper doPostAction() {
        if (!alert().isPresent()) {
            try {
                waitThat().document().toBeReady();
                waitThat().jQuery().toBeInactive();
            } catch (TimeoutException e) {
                logger.warn(e.getMessage());
            }
        }
        return this;
    }

    private class ActionsWrapper implements IActionsWrapper {
        private Actions actions = new Actions(driver);
        private StringBuffer trace = new StringBuffer("try to perform following actions:");

        @Override
        public IActionsWrapper click() {
            trace.append("click anywhere;");
            actions.click();
            return this;
        }

        @Override
        public IActionsWrapper click(Locator locator) {
            trace.append(String.format("click %s;", locator));
            actions.click(waitThat(locator).toBeClickable());
            return this;
        }

        @Override
        public IActionsWrapper clickAndHold() {
            trace.append("click and hold anywhere;");
            actions.clickAndHold();
            return this;
        }

        @Override
        public IActionsWrapper clickAndHold(Locator locator) {
            trace.append(String.format("click and hold %s;", locator));
            actions.clickAndHold(waitThat(locator).toBeClickable());
            return this;
        }

        @Override
        public IActionsWrapper contextClick() {
            trace.append("context click anywhere;");
            actions.contextClick();
            return this;
        }

        @Override
        public IActionsWrapper contextClick(Locator locator) {
            trace.append(String.format("context click %s;", locator));
            actions.contextClick(waitThat(locator).toBeClickable());
            return this;
        }

        @Override
        public IActionsWrapper release() {
            trace.append("release on anywhere;");
            actions.release();
            return this;
        }

        @Override
        public IActionsWrapper release(Locator locator) {
            trace.append(String.format("release on %s;", locator));
            actions.release(waitThat(locator).toBeVisible());
            return this;
        }

        @Override
        public IActionsWrapper doubleClick() {
            trace.append("double click anywhere;");
            actions.doubleClick();
            return this;
        }

        @Override
        public IActionsWrapper doubleClick(Locator locator) {
            trace.append(String.format("double click %s;", locator));
            actions.doubleClick(waitThat(locator).toBeClickable());
            return this;
        }

        @Override
        public IActionsWrapper dragAndDrop(Locator source, Locator target) {
            trace.append(String.format("drag %s and drop on %s;", source, target));
            actions.dragAndDrop(waitThat(source).toBeClickable(), waitThat(target).toBeVisible());
            return this;
        }

        @Override
        public IActionsWrapper dragAndDrop(Locator source, int xOffset, int yOffset) {
            trace.append(String.format("drag %s and drop on (%d, %d);", source, xOffset, yOffset));
            actions.dragAndDropBy(waitThat(source).toBeClickable(), xOffset, yOffset);
            return this;
        }

        @Override
        public IActionsWrapper keyDown(Keys theKey) {
            trace.append(String.format("type key %s down on anywhere;", theKey));
            actions.keyDown(theKey);
            return this;
        }

        @Override
        public IActionsWrapper keyDown(Locator locator, Keys theKey) {
            trace.append(String.format("type key %s down on %s;", theKey, locator));
            actions.keyDown(waitThat(locator).toBeVisible(), theKey);
            return this;
        }

        @Override
        public IActionsWrapper keyUp(Keys theKey) {
            trace.append(String.format("type key %s up on anywhere;", theKey));
            actions.keyUp(theKey);
            return this;
        }

        @Override
        public IActionsWrapper keyUp(Locator locator, Keys theKey) {
            trace.append(String.format("type key %s up on %s;", theKey, locator));
            actions.keyUp(waitThat(locator).toBeVisible(), theKey);
            return this;
        }

        @Override
        public IActionsWrapper sendKeys(Locator locator, CharSequence... keysToSend) {
            trace.append(String.format("send keys %s on %s;", StringUtils.join(keysToSend), locator));
            actions.sendKeys(waitThat(locator).toBeVisible(), keysToSend);
            return this;
        }

        @Override
        public IActionsWrapper sendKeys(CharSequence... keysToSend) {
            trace.append(String.format("send keys %s on anywhere;", StringUtils.join(keysToSend)));
            actions.sendKeys(keysToSend);
            return this;
        }

        @Override
        public IActionsWrapper moveTo(Locator locator) {
            trace.append(String.format("move to %s;", locator));
            actions.moveToElement(waitThat(locator).toBeVisible());
            return this;
        }

        @Override
        public IActionsWrapper moveTo(Locator locator, int xOffset, int yOffset) {
            trace.append(String.format("move to %s offset (%d, %d);", locator, xOffset, yOffset));
            actions.moveToElement(waitThat(locator).toBeVisible(), xOffset, yOffset);
            return this;
        }

        @Override
        public IActionsWrapper moveTo(int xOffset, int yOffset) {
            trace.append(String.format("move to (%d, %d);", xOffset, yOffset));
            actions.moveByOffset(xOffset, yOffset);
            return this;
        }

        @Override
        public Action build() {
            trace.append("build all actions");
            final Action action = actions.build();
            return new Action() {
                @Override
                public void perform() {
                    logger.info(trace.toString());
                    wait.until(new ExpectedCondition<Boolean>() {
                        @Override
                        public Boolean apply(WebDriver driver) {
                            action.perform();
                            return true;
                        }
                    });
                    doPostAction();
                }
            };
        }

        @Override
        public void perform() {
            logger.info(trace.toString());
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    actions.perform();
                    return true;
                }
            });
            doPostAction();
        }
    }

    private class AlertWrapper implements IAlertWrapper {
        private Alert alert;

        public AlertWrapper() {
        }

        public AlertWrapper(Alert alert) {
            this.alert = alert;
        }

        @Override
        public void dismiss() {
            if (alert != null) {
                logger.info("dismissing alert {}", alert.getText());
                alert.dismiss();
            } else {
                waitThat().alert().toBePresent().dismiss();
            }
        }

        @Override
        public void accept() {
            if (alert != null) {
                logger.info("accepting alert {}", alert.getText());
                alert.accept();
            } else {
                waitThat().alert().toBePresent().accept();
            }
        }

        @Override
        public String getText() {
            if (alert != null) {
                return alert.getText();
            } else {
                return waitThat().alert().toBePresent().getText();
            }
        }

        @Override
        public boolean isPresent() {
            try {
                driver.switchTo().alert();
                return true;
            } catch (WebDriverException e) {
                return false;
            }
        }

        @Override
        public void disable() {
            disable(true);
        }

        @Override
        public void disable(boolean accept) {
            logger.info("disabling javascript alert by {}ing all of them", accept ? "accept" : "dismiss");
            executeScript(String.format("window.alert = function(msg) {}; window.confirm = function(msg) { return %b; }; window.prompt = function(msg) { return %b; };", accept, accept));
        }

        @Override
        public void enable() {
            logger.info("enabling javascript alert");
            executeScript("delete window.alert; delete window.confirm; delete window.prompt;");
        }
    }

    private class BrowseNavigation implements IBrowseNavigation {
        @Override
        public IBrowseNavigation to(final String url) {
            logger.info("navigating to url {}", url);
            driver.navigate().to(url);
            doPostAction();
            return this;
        }

        @Override
        public IBrowseNavigation forward() {
            logger.info("navigating forward");
            driver.navigate().forward();
            doPostAction();
            return this;
        }

        @Override
        public IBrowseNavigation backward() {
            logger.info("navigating back");
            driver.navigate().back();
            doPostAction();
            return this;
        }

        @Override
        public IBrowseNavigation refresh() {
            logger.info("refreshing page");
            driver.navigate().refresh();
            doPostAction();
            return this;
        }
    }

    private class WebLocatorWrapper implements IWebElementWrapper {
        private Locator locator;

        public WebLocatorWrapper(Locator locator) {
            this.locator = locator;
        }

        private WebElement findElement() {
            return driver.findElement(locator.by());
        }

        private List<WebElement> findElements() {
            return driver.findElements(locator.by());
        }

        @Override
        public IWebElementWrapper click() {
            return click(true);
        }

        private IWebElementWrapper click(boolean logging) {
            if (logging) {
                logger.info("clicking {}", locator);
            }
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    findElement().click();
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper submit() {
            submit(true);
            return this;
        }

        public IWebElementWrapper submit(boolean logging) {
            if (logging) {
                logger.info("submit {}", locator);
            }
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    findElement().submit();
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public boolean clickSmartly() {
            if (isDisplayed()) {
                click();
                return true;
            }
            return false;
        }

        @Override
        public IWebElementWrapper clickByJavaScript() {
            logger.info("clicking {} by executing javascript", locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].click()", findElement());
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper clickCircularly() throws InterruptedException {
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    click();
                    return !isDisplayed();
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper doubleClick() {
            actions().doubleClick(locator).perform();
            return this;
        }

        @Override
        public IWebElementWrapper contextClick() {
            actions().contextClick(locator).perform();
            return this;
        }

        @Override
        public IWebElementWrapper dragAndDrop(Locator target) {
            actions().dragAndDrop(locator, target).perform();
            return this;
        }

        @Override
        public IWebElementWrapper dragAndDrop(int xOffset, int yOffset) {
            actions().dragAndDrop(locator, xOffset, yOffset).perform();
            return this;
        }

        @Override
        public IWebElementWrapper type(final CharSequence... value) {
            logger.info("typing {} on {}", StringUtils.join(value), locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    findElement().sendKeys(value);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper clear() {
            logger.info("clearing value on " + locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    findElement().clear();
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper input(String value) {
            return clear().type(value);
        }

        @Override
        public IWebElementWrapper forceInput(String value) {
            do {
                clear().type(value);
            }while (!this.getInputDisplayedValue().trim().equals(value.trim()));
            return this;
        }

        @Override
        public boolean inputSmartly(String value) {
            if (isDisplayed()) {
                input(value);
                return true;
            }
            return false;
        }

        @Override
        public IWebElementWrapper check(boolean checked) {
            logger.info("checking {} {}", checked ? "on" : "off", locator);
            if (isChecked() != checked) {
                click(false);
            }
            assertThat(locator).selected().isEqualTo(checked);
            return this;
        }

        @Override
        public boolean isChecked() {
            return isSelected();
        }

        @Override
        public boolean tick(boolean checked) {
            logger.info("ticking {} {}", checked ? "on" : "off", locator);
            if (isChecked() != checked) {
                click(false);
                return true;
            }
            assertThat(locator).selected().isEqualTo(checked);
            return false;
        }

        @Override
        public IWebElementWrapper selectAll() {
            Select select = new Select(waitThat(locator).toBeVisible());
            if (!select.isMultiple()) {
                throw new UnsupportedOperationException(String.format("You may only select all options of a multi-select %s", locator));
            }

            for (String text : getAllOptionTexts()) {
                selectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper checkByJavaScript(boolean checked) {
            logger.info("checking {} {} by javascript", checked ? "on" : "off", locator);
            if (checked) {
                setAttribute("checked", "checked");
            } else {
                removeAttribute("checked");
            }
            return this;
        }

        @Override
        public IWebElementWrapper selectByVisibleText(final String text) {
            logger.info("selecting by visible text {} on {}", text, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).selectByVisibleText(text);
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper selectByVisibleText(String... texts) {
            for (String text : texts) {
                selectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper selectByIndex(final int index) {
            logger.info("selecting by index {} on {}", index, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).selectByIndex(index);
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper selectByValue(final String value) {
            logger.info("selecting by value {} on {}", value, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).selectByValue(value);
                    return true;
                }
            });
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper deselectAll() {
            logger.info("deselecting all options on {}", locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).deselectAll();
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper deselectByVisibleText(final String text) {
            logger.info("deselecting by visible text {} on {}", text, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).deselectByVisibleText(text);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper deselectByVisibleText(String... texts) {
            for (String text : texts) {
                deselectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper deselectByIndex(final int index) {
            logger.info("deselecting by index {} on {}", index, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).deselectByIndex(index);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper deselectByValue(final String value) {
            logger.info("deselecting by value {} on {}", value, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    new Select(findElement()).deselectByValue(value);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper moveTo() {
            actions().moveTo(locator).perform();
            return this;
        }

        @Override
        public boolean isPresent() {
            try {
                findElement();
                return true;
            } catch (WebDriverException e) {
                return false;
            }
        }

        @Override
        public boolean isEnabled() {
            final boolean[] ret = new boolean[1];
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    ret[0] = findElement().isEnabled();
                    return true;
                }
            });
            return ret[0];
        }

        @Override
        public boolean isDisplayed() {
            try {
                return findElement().isDisplayed();
            } catch (WebDriverException e) {
                return false;
            }
        }

        @Override
        public boolean isSelected() {
            final boolean[] ret = new boolean[1];
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    ret[0] = findElement().isSelected();
                    return true;
                }
            });
            return ret[0];
        }

        @Override
        public String getAttribute(final String attribute) {
            return wait.until(new ExpectedCondition<String>() {
                @Override
                public String apply(WebDriver driver) {
                    return findElement().getAttribute(attribute);
                }
            });
        }

        @Override
        public String getCssValue(final String attribute) {
            return wait.until(new ExpectedCondition<String>() {
                @Override
                public String apply(WebDriver driver) {
                    return findElement().getCssValue(attribute);
                }
            });
        }

        @Override
        public String getInnerText() {
            return wait.until(new ExpectedCondition<String>() {
                @Override
                public String apply(WebDriver driver) {
                    return findElement().getText();
                }
            });
        }

        @Override
        public String getInputDisplayedValue() {
            return (String) executeScript("return arguments[0].value", findElement());
        }

        @Override
        public List<String> getAllInnerTexts() {
            List<String> texts = new ArrayList<>();
            for (IWebElementWrapper webElementWrapper : getAllMatchedElements()) {
                texts.add(webElementWrapper.getInnerText());
            }
            return texts;
        }

        @Override
        public IWebElementWrapper setInnerText(final String text) {
            logger.info("setting innertext {} on {}", text, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].innerText=arguments[1]", findElement(), text);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper setValue(final String value) {
            logger.info("setting text {} on {}", value, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].value=arguments[1]", findElement(), value);
                    return true;
                }
            });
            return this;
        }

        @Override
        public List<WebElement> getAllSelectedOptions() {
            return wait.until(new ExpectedCondition<List<WebElement>>() {
                @Override
                public List<WebElement> apply(WebDriver driver) {
                    return new Select(findElement()).getAllSelectedOptions();
                }
            });
        }

        @Override
        public List<WebElement> getAllOptions() {
            return wait.until(new ExpectedCondition<List<WebElement>>() {
                @Override
                public List<WebElement> apply(WebDriver driver) {
                    return new Select(findElement()).getOptions();
                }
            });
        }

        @Override
        public List<String> getAllOptionTexts() {
            List<String> list = new ArrayList<>();
            for (WebElement option : getAllOptions()) {
                list.add(element(option).getInnerText());
            }
            return list;
        }

        @Override
        public String getSelectedText() {
            return getAllSelectedTexts().get(0);
        }

        @Override
        public List<String> getAllSelectedTexts() {
            List<String> list = new ArrayList<>();
            for (WebElement option : getAllSelectedOptions()) {
                list.add(element(option).getInnerText());
            }
            return list;
        }

        @Override
        public IWebElementWrapper fireEvent(final String event) {
            logger.info("firing {} on {}", event, locator);
            try {
                String eventType = event.toLowerCase().startsWith("on") ? event.substring(2) : event;
                executeScript("var evt = document.createEvent('HTMLEvents'); evt.initEvent(arguments[1], true, true); arguments[0].dispatchEvent(evt);", waitThat(locator).toBeVisible(), eventType);
            } catch (WebDriverException e) {// ie vith version less than 9
                executeScript("arguments[0].fireEvent(arguments[1]);", waitThat(locator).toBeVisible(), event);
            }
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper scrollTo() {
            logger.info("scrolling to {}", locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    WebElement element = findElement();
                    executeScript("window.scrollTo(arguments[0],arguments[1])", element.getLocation().x, element.getLocation().y);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper scrollIntoView() {
            scrollIntoView(true);
            return this;
        }

        @Override
        public IWebElementWrapper scrollIntoView(final boolean bAlignToTop) {
            logger.info("scrolling into view {}on {}", bAlignToTop ? "by aligning to top " : "", locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].scrollIntoView(arguments[1])", findElement(), bAlignToTop);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper setAttribute(final String attribute, final String value) {
            logger.info("setting attribute value of {} to {} on {}", attribute, value, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].setAttribute(arguments[1], arguments[2])", findElement(), attribute, value);
                    return true;
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper removeAttribute(final String attribute) {
            logger.info("removing attribute {} on {}", attribute, locator);
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    executeScript("arguments[0].removeAttribute(arguments[1])", findElement(), attribute);
                    return true;
                }
            });
            return this;
        }

        @Override
        public long getCellRow() {
            return wait.until(new ExpectedCondition<Long>() {
                @Override
                public Long apply(WebDriver driver) {
                    long ret = (long) executeScript("return arguments[0].parentNode.rowIndex", findElement());
                    return ++ret;// row index starts with zero
                }
            });
        }

        @Override
        public long getCellColumn() {
            return wait.until(new ExpectedCondition<Long>() {
                @Override
                public Long apply(WebDriver driver) {
                    long ret = (long) executeScript("return arguments[0].cellIndex", findElement());
                    return ++ret;// column index starts with zero
                }
            });
        }

        @Override
        public long getRow() {
            return wait.until(new ExpectedCondition<Long>() {
                @Override
                public Long apply(WebDriver driver) {
                    long ret = (long) executeScript("return arguments[0].rowIndex", findElement());
                    return ++ret;// row index starts with zero
                }
            });
        }

        @Override
        public long getRowCount() {
            return wait.until(new ExpectedCondition<Long>() {
                @Override
                public Long apply(WebDriver driver) {
                    return (Long) executeScript("return arguments[0].rows.length", waitThat(locator).toBePresent());
                }
            });
        }

        @Override
        public IWebDriverWrapper switchTo() {
            logger.info("switching to {}", locator);
            IWebDriverWrapper ret = waitThat(locator).frameToBeAvailableAndSwitchToIt();
            doPostAction();
            return ret;
        }

        @Override
        public int getNumberOfMatches() {
            int num = 0;
            for (WebElement webElement : findElements()) {
                if (webElement.isDisplayed()) {
                    num++;
                }
            }
            return num;
        }

        @Override
        public List<IWebElementWrapper> getAllMatchedElements() {
            List<IWebElementWrapper> webElementWrappers = new ArrayList<>();
            for (WebElement webElement : findElements()) {
                if (webElement.isDisplayed()) {
                    webElementWrappers.add(element(webElement));
                }
            }
            return webElementWrappers;
        }
    }

    private class WebElementWrapper implements IWebElementWrapper {
        private WebElement webElement;

        public WebElementWrapper(WebElement webElement) {
            this.webElement = webElement;
        }

        @Override
        public IWebElementWrapper click() {
            return click(true);
        }

        public IWebElementWrapper click(boolean logging) {
            if (logging) {
                logger.info("clicking {}", webElement);
            }
            wait.until(ExpectedConditions.visibilityOf(webElement)).click();
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper submit() {
            submit(true);
            return this;
        }

        public IWebElementWrapper submit(boolean logging) {
            if (logging) {
                logger.info("submit {}", webElement);
            }
            wait.until(ExpectedConditions.visibilityOf(webElement)).submit();
            doPostAction();
            return this;
        }

        @Override
        public boolean clickSmartly() {
            if (isDisplayed()) {
                click();
                return true;
            }
            return false;
        }

        @Override
        public IWebElementWrapper clickByJavaScript() {
            logger.info("clicking {} by executing javascript", webElement);
            executeScript("arguments[0].click()", wait.until(ExpectedConditions.visibilityOf(webElement)));
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper clickCircularly() throws InterruptedException {
            wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    click();
                    return !isDisplayed();
                }
            });
            return this;
        }

        @Override
        public IWebElementWrapper doubleClick() {
            logger.info("double clicking {}", webElement);
            Actions actions = new Actions(driver);
            actions.doubleClick(wait.until(ExpectedConditions.visibilityOf(webElement))).perform();
            return this;
        }

        @Override
        public IWebElementWrapper contextClick() {
            logger.info("context clicking {}", webElement);
            Actions actions = new Actions(driver);
            actions.contextClick(wait.until(ExpectedConditions.visibilityOf(webElement))).perform();
            return this;
        }

        @Override
        public IWebElementWrapper dragAndDrop(Locator target) {
            logger.info("dragging {} and dropping to {}", webElement, target);
            Actions actions = new Actions(driver);
            actions.dragAndDrop(wait.until(ExpectedConditions.visibilityOf(webElement)), waitThat(target).toBeVisible()).perform();
            return this;
        }

        @Override
        public IWebElementWrapper dragAndDrop(int xOffset, int yOffset) {
            logger.info("dragging {} and dropping to ({},{})", webElement, xOffset, yOffset);
            Actions actions = new Actions(driver);
            actions.dragAndDropBy(wait.until(ExpectedConditions.visibilityOf(webElement)), xOffset, yOffset).perform();
            return this;
        }

        @Override
        public IWebElementWrapper type(CharSequence... value) {
            logger.info("typing {} on {}", StringUtils.join(value), webElement);
            wait.until(ExpectedConditions.visibilityOf(webElement)).sendKeys(value);
            return this;
        }

        @Override
        public IWebElementWrapper clear() {
            logger.info("clearing value on " + webElement);
            wait.until(ExpectedConditions.visibilityOf(webElement)).clear();
            return this;
        }

        @Override
        public IWebElementWrapper input(String value) {
            return clear().type(value);
        }

        @Override
        public IWebElementWrapper forceInput(String value) {
            do {
                clear().type(value);
            }while (!this.getInputDisplayedValue().trim().equals(value.trim()));
            return this;
        }

        @Override
        public boolean inputSmartly(String value) {
            if (isDisplayed()) {
                input(value);
                return true;
            }
            return false;
        }

        @Override
        public IWebElementWrapper check(boolean checked) {
            logger.info("checking {} {}", checked ? "on" : "off", webElement);
            if (isChecked() != checked) {
                click(false);
            }
            assertThat(new WebElementWrapper(webElement)).selected().isEqualTo(checked);
            return this;
        }

        @Override
        public boolean isChecked() {
            return isSelected();
        }

        @Override
        public boolean tick(boolean checked) {
            logger.info("ticking {} {}", checked ? "on" : "off", webElement);
            if (isChecked() != checked) {
                click(false);
                return true;
            }
            assertThat(new WebElementWrapper(webElement)).selected().isEqualTo(checked);
            return false;
        }

        @Override
        public IWebElementWrapper selectAll() {
            Select select = new Select(wait.until(ExpectedConditions.visibilityOf(webElement)));
            if (!select.isMultiple()) {
                throw new UnsupportedOperationException(String.format("You may only select all options of a multi-select %s", webElement));
            }

            for (String text : getAllOptionTexts()) {
                selectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper checkByJavaScript(boolean checked) {
            logger.info("checking {} {} by javascript", checked ? "on" : "off", webElement);
            if (checked) {
                setAttribute("checked", "checked");
            } else {
                removeAttribute("checked");
            }
            return this;
        }

        @Override
        public IWebElementWrapper selectByVisibleText(String text) {
            logger.info("selecting by visible text {} on {}", text, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).selectByVisibleText(text);
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper selectByVisibleText(String... texts) {
            for (String text : texts) {
                selectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper selectByIndex(int index) {
            logger.info("selecting by index {} on {}", index, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).selectByIndex(index);
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper selectByValue(String value) {
            logger.info("selecting by value {} on {}", value, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).selectByValue(value);
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper deselectAll() {
            logger.info("deselecting all options on {}", webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).deselectAll();
            return this;
        }

        @Override
        public IWebElementWrapper deselectByVisibleText(String text) {
            logger.info("deselecting by visible text {} on {}", text, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).deselectByVisibleText(text);
            return this;
        }

        @Override
        public IWebElementWrapper deselectByVisibleText(String... texts) {
            for (String text : texts) {
                deselectByVisibleText(text);
            }
            return this;
        }

        @Override
        public IWebElementWrapper deselectByIndex(int index) {
            logger.info("deselecting by index {} on {}", index, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).deselectByIndex(index);
            return this;
        }

        @Override
        public IWebElementWrapper deselectByValue(String value) {
            logger.info("deselecting by value {} on {}", value, webElement);
            new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).deselectByValue(value);
            return this;
        }

        @Override
        public IWebElementWrapper moveTo() {
            Actions actions = new Actions(driver);
            actions.moveToElement(wait.until(ExpectedConditions.visibilityOf(webElement))).perform();
            return this;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return wait.until(ExpectedConditions.visibilityOf(webElement)).isEnabled();
        }

        @Override
        public boolean isDisplayed() {
            return webElement.isDisplayed();
        }

        @Override
        public boolean isSelected() {
            return wait.until(ExpectedConditions.visibilityOf(webElement)).isSelected();
        }

        @Override
        public String getAttribute(String attribute) {
            return wait.until(ExpectedConditions.visibilityOf(webElement)).getAttribute(attribute);
        }

        @Override
        public String getCssValue(String attribute) {
            return wait.until(ExpectedConditions.visibilityOf(webElement)).getCssValue(attribute);
        }

        @Override
        public String getInnerText() {
            return wait.until(ExpectedConditions.visibilityOf(webElement)).getText();
        }

        @Override
        public String getInputDisplayedValue() {
            return (String) executeScript("return arguments[0].value", webElement);
        }

        @Override
        public List<String> getAllInnerTexts() {
            return Arrays.asList(getInnerText());
        }

        @Override
        public IWebElementWrapper setInnerText(String text) {
            logger.info("setting innertext {} on {}", text, webElement);
            executeScript("arguments[0].innerText=arguments[1]", wait.until(ExpectedConditions.visibilityOf(webElement)), text);
            return this;
        }

        @Override
        public IWebElementWrapper setValue(String value) {
            logger.info("setting text {} on {}", value, webElement);
            executeScript("arguments[0].value=arguments[1]", wait.until(ExpectedConditions.visibilityOf(webElement)), value);
            return this;
        }

        @Override
        public List<WebElement> getAllSelectedOptions() {
            return new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).getAllSelectedOptions();
        }

        @Override
        public List<WebElement> getAllOptions() {
            return new Select(wait.until(ExpectedConditions.visibilityOf(webElement))).getOptions();
        }

        @Override
        public List<String> getAllOptionTexts() {
            List<String> list = new ArrayList<>();
            List<WebElement> options = getAllOptions();
            for (WebElement option : options) {
                list.add(option.getText());
            }
            return list;
        }

        @Override
        public String getSelectedText() {
            return getAllSelectedTexts().get(0);
        }

        @Override
        public List<String> getAllSelectedTexts() {
            List<String> list = new ArrayList<>();
            List<WebElement> options = getAllSelectedOptions();
            for (WebElement option : options) {
                list.add(option.getText());
            }
            return list;
        }

        @Override
        public IWebElementWrapper fireEvent(String event) {
            logger.info("firing {} on {}", event, webElement);
            try {
                String eventType = event.toLowerCase().startsWith("on") ? event.substring(2) : event;
                executeScript("var evt = document.createEvent('HTMLEvents'); evt.initEvent(arguments[1], true, true); arguments[0].dispatchEvent(evt);", wait.until(ExpectedConditions.visibilityOf(webElement)), eventType);
            } catch (WebDriverException e) {// ie vith version less than 9
                executeScript("arguments[0].fireEvent(arguments[1]);", wait.until(ExpectedConditions.visibilityOf(webElement)), event);
            }
            doPostAction();
            return this;
        }

        @Override
        public IWebElementWrapper scrollTo() {
            logger.info("scrolling to {}", webElement);
            WebElement element = wait.until(ExpectedConditions.visibilityOf(webElement));
            executeScript("window.scrollTo(arguments[0],arguments[1])", element.getLocation().x, element.getLocation().y);
            return this;
        }

        @Override
        public IWebElementWrapper scrollIntoView() {
            scrollIntoView(true);
            return this;
        }

        @Override
        public IWebElementWrapper scrollIntoView(boolean bAlignToTop) {
            logger.info("scrolling into view {}on {}", bAlignToTop ? "by aligning to top " : "", webElement);
            executeScript("arguments[0].scrollIntoView(arguments[1])", wait.until(ExpectedConditions.visibilityOf(webElement)), bAlignToTop);
            return this;
        }

        @Override
        public IWebElementWrapper setAttribute(String attribute, String value) {
            logger.info("setting attribute value of {} to {} on {}", attribute, value, webElement);
            executeScript("arguments[0].setAttribute(arguments[1], arguments[2])", wait.until(ExpectedConditions.visibilityOf(webElement)), attribute, value);
            return this;
        }

        @Override
        public IWebElementWrapper removeAttribute(String attribute) {
            logger.info("removing attribute {} on {}", attribute, webElement);
            executeScript("arguments[0].removeAttribute(arguments[1])", wait.until(ExpectedConditions.visibilityOf(webElement)), attribute);
            return this;
        }

        @Override
        public long getCellRow() {
            long ret = (long) executeScript("return arguments[0].parentNode.rowIndex", wait.until(ExpectedConditions.visibilityOf(webElement)));
            return ++ret;// row index starts with zero
        }

        @Override
        public long getCellColumn() {
            long ret = (long) executeScript("return arguments[0].cellIndex", wait.until(ExpectedConditions.visibilityOf(webElement)));
            return ++ret;// column index starts with zero
        }

        @Override
        public long getRow() {
            long ret = (long) executeScript("return arguments[0].rowIndex", wait.until(ExpectedConditions.visibilityOf(webElement)));
            return ++ret;// row index starts with zero
        }

        @Override
        public long getRowCount() {
            return (long) executeScript("return arguments[0].rows.length", wait.until(ExpectedConditions.visibilityOf(webElement)));
        }

        @Override
        public IWebDriverWrapper switchTo() {
            logger.info("switching to {}", webElement);
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(webElement));
            doPostAction();
            return WebDriverWrapper.this;
        }

        @Override
        public int getNumberOfMatches() {
            return 1;
        }

        @Override
        public List<IWebElementWrapper> getAllMatchedElements() {
            List<IWebElementWrapper> webElementWrappers = new ArrayList<>();
            webElementWrappers.add(this);
            return webElementWrappers;
        }
    }

    private class FluentLocatorWait implements IFluentLocatorWait {
        private Locator locator;

        public FluentLocatorWait(Locator locator) {
            this.locator = locator;
        }

        @Override
        public IFluentStringWait innerText() {
            return new IFluentStringWait() {

                @Override
                public Boolean toBe(String text) {
                    return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator.by(), text));
                }

                @Override
                public Boolean toBeEmpty() {
                    return toBe("");
                }

                @Override
                public Boolean notToBe(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getInnerText();
                            return !currentValue.equals(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait innerText not to be %s while current value is %s on %s", text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean contains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getInnerText();
                            return currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait innerText contains %s while current value is %s on %s", text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean notContains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getInnerText();
                            return !currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait innerText not contains %s while current value is %s on %s", text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean startsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getInnerText();
                            return currentValue.startsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait innerText starts with %s while current value is %s on %s", text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean endsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getInnerText();
                            return currentValue.endsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait innerText ends with %s while current value is %s on %s", text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean matches(Pattern pattern) {
                    return wait.until(ExpectedConditions.textMatches(locator.by(), pattern));
                }
            };
        }

        @Override
        public Boolean toBeInvisible() {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator.by()));
        }

        @Override
        public WebElement toBePresent() {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator.by()));
        }

        @Override
        public WebElement toBeEnabled() {
            return wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver driver) {
                    WebElement element = driver.findElement(locator.by());
                    return (element.isDisplayed() && element.isEnabled()) ? element : null;
                }

                @Override
                public String toString() {
                    return "visibility and feasibility of element located by " + locator;
                }
            });
        }

        @Override
        public List<WebElement> toBeAllPresent() {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator.by()));
        }

        @Override
        public Boolean toBeAbsent() {
            return wait.until(new ExpectedCondition<Boolean>() {
                @Override
                public Boolean apply(WebDriver driver) {
                    return !element(locator).isPresent();
                }

                @Override
                public String toString() {
                    return String.format("wait absence of %s", locator);
                }
            });
        }

        @Override
        public Boolean toBeSelected() {
            return wait.until(ExpectedConditions.elementToBeSelected(locator.by()));
        }

        @Override
        public Boolean toBeDeselected() {
            return wait.until(ExpectedConditions.elementSelectionStateToBe(locator.by(), false));
        }

        @Override
        public IFluentLocatorWait toBePresentIn(long milliseconds) {
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < milliseconds) {
                if (element(locator).isPresent()) {
                    return this;
                }
            }
            logger.warn("wait presence of {} in {} milliseconds", locator, milliseconds);
            return this;
        }

        @Override
        public IFluentLocatorWait toBeAbsentIn(long milliseconds) {
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < milliseconds) {
                if (!element(locator).isPresent()) {
                    return this;
                }
            }
            logger.warn("wait absence of {} in {} milliseconds", locator, milliseconds);
            return this;
        }

        @Override
        public IFluentLocatorWait toBeAppearedIn(long milliseconds) {
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < milliseconds) {
                if (element(locator).isDisplayed()) {
                    return this;
                }
            }
            logger.warn("wait visibility of {} in {} milliseconds", locator, milliseconds);
            return this;
        }

        @Override
        public IFluentLocatorWait toBeDisappearedIn(long milliseconds) {
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < milliseconds) {
                if (!element(locator).isDisplayed()) {
                    return this;
                }
            }
            logger.warn("wait invisibility of {} in {} milliseconds", locator, milliseconds);
            return this;
        }

        @Override
        public WebElement toBeClickable() {
            return wait.until(ExpectedConditions.elementToBeClickable(locator.by()));
        }

        @Override
        public WebElement toBeVisible() {
// following step has bug 'IEDriver: Error determining if element is displayed #7524'
// logged at https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/7524
//            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator.by()));
            return wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver driver) {
                    try {
                        WebElement webElement = driver.findElement(locator.by());
                        return webElement.isDisplayed() ? webElement : null;
                    } catch (WebDriverException e) {
                        return null;
                    }
                }

                @Override
                public String toString() {
                    return "visibility of element located by " + locator;
                }
            });
        }

        @Override
        public List<WebElement> toBeAllVisible() {
            return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator.by()));
        }

        @Override
        public IWebDriverWrapper frameToBeAvailableAndSwitchToIt() {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator.by()));
            return WebDriverWrapper.this;
        }

        @Override
        public IFluentStringWait attributeValueOf(final String attribute) {
            return new IFluentStringWait() {
                @Override
                public Boolean toBe(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return currentValue.equals(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s to be %s while current value is %s on %s", attribute, text.isEmpty() ? "empty" : text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean toBeEmpty() {
                    return toBe("");
                }

                @Override
                public Boolean notToBe(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return !currentValue.equals(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s not to be %s while current value is %s on %s", attribute, text.isEmpty() ? "empty" : text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean contains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s contains %s while current value is %s on %s", attribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean notContains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return !currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s not contains %s while current value is %s on %s", attribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean startsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return currentValue.startsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s starts with %s while current value is %s on %s", attribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean endsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getAttribute(attribute);
                            return currentValue.endsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s ends with %s while current value is %s on %s", attribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean matches(final Pattern pattern) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            try {
                                currentValue = element(locator).getAttribute(attribute);
                                return pattern.matcher(currentValue).find();
                            } catch (Exception e) {
                                return false;
                            }
                        }

                        @Override
                        public String toString() {
                            return String.format("wait attribute value of %s matches pattern %s while current value is %s on %s", attribute, pattern.pattern(), currentValue, locator);
                        }
                    });
                }
            };
        }

        @Override
        public IFluentStringWait cssValueOf(final String cssAttribute) {
            return new IFluentStringWait() {

                @Override
                public Boolean toBe(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return currentValue.equals(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s to be %s while current value is %s on %s", cssAttribute, text.isEmpty() ? "empty" : text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean toBeEmpty() {
                    return toBe("");
                }

                @Override
                public Boolean notToBe(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return !currentValue.equals(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s not to be %s while current value is %s on %s", cssAttribute, text.isEmpty() ? "empty" : text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean contains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s contains %s while current value is %s on %s", cssAttribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean notContains(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return !currentValue.contains(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s not contains %s while current value is %s on %s", cssAttribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean startsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return currentValue.startsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s starts with %s while current value is %s on %s", cssAttribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean endsWith(final String text) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getCssValue(cssAttribute);
                            return currentValue.endsWith(text);
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s ends with %s while current value is %s on %s", cssAttribute, text, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean matches(final Pattern pattern) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            try {
                                currentValue = element(locator).getCssValue(cssAttribute);
                                return pattern.matcher(currentValue).find();
                            } catch (Exception e) {
                                return false;
                            }
                        }

                        @Override
                        public String toString() {
                            return String.format("wait css attribute value of %s matches pattern %s while current value is %s on %s", cssAttribute, pattern.pattern(), currentValue, locator);
                        }
                    });
                }
            };
        }

        @Override
        public IFluentNumberWait numberOfElements() {
            return new IFluentNumberWait() {
                @Override
                public Boolean equalTo(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue == number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be equal to %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean notEqualTo(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue != number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be not equal to %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean lessThan(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue < number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be less than %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean greaterThan(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue > number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be greater than %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean equalToOrLessThan(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue <= number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be equal to or less than %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }

                @Override
                public Boolean equalToOrGreaterThan(final int number) {
                    return wait.until(new ExpectedCondition<Boolean>() {
                        private int currentValue = 0;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            currentValue = element(locator).getNumberOfMatches();
                            return currentValue >= number;
                        }

                        @Override
                        public String toString() {
                            return String.format("wait number of elements to be equal to or greater than %d while current value is %d, located by %s", number, currentValue, locator);
                        }
                    });
                }
            };
        }

        @Override
        public IFluentLocatorWait nestedElements(Locator locator) {
            return null;
        }
    }

    private class FluentWait implements IFluentWait {
        @Override
        public IFluentWait timeout(long milliseconds) throws InterruptedException {
            logger.info("wait {} milliseconds", milliseconds);
            Thread.sleep(milliseconds);
            return this;
        }

        @Override
        public IFluentDocumentWait document() {
            return new IFluentDocumentWait() {
                @Override
                public void toBeReady() {
                    wait.until(new ExpectedCondition<Boolean>() {
                        private String currentValue = null;

                        @Override
                        public Boolean apply(WebDriver driver) {
                            try {
                                currentValue = (String) executeScript("return document.readyState");
                                //return currentValue.equals("interactive") || currentValue.equals("complete");
                                logger.debug("Wait for document to be ready. Current document status is " + currentValue);
                                return currentValue.equals("complete");
                            } catch (WebDriverException e) {
                                logger.warn("javascript error while waiting document ready");
                                return true;
                            }
                        }

                        @Override
                        public String toString() {
                            return String.format("wait document to be complete while current value is %s", currentValue);
                        }
                    });
                }
            };
        }

        @Override
        public IFluentJQueryWait jQuery() {
            return new IFluentJQueryWait() {
                @Override
                public boolean isJQuerySupported() {
                    try {
                        return (boolean) executeScript("return jQuery()!=null");
                    } catch (WebDriverException e) {
                        return false;
                    }
                }

                private void injectJQuery() {
                    executeScript(" var headID = "
                            + "document.getElementsByTagName(\"head\")[0];"
                            + "var newScript = document.createElement('script');"
                            + "newScript.type = 'text/javascript';" + "newScript.src = "
                            + "'http://ajax.googleapis.com/ajax/"
                            + "libs/jquery/1.7.2/jquery.min.js';"
                            + "headID.appendChild(newScript);");
                }

                @Override
                public void toBeInactive() {
                    if (isJQuerySupported()) {
                        wait.until(new ExpectedCondition<Boolean>() {
                            private long currentValue = 0L;

                            @Override
                            public Boolean apply(WebDriver driver) {
                                try {
                                    currentValue = (long) executeScript("return jQuery.active");
                                    logger.debug("Wait JQuery to be inactive. The JQuery current status is " + currentValue);
                                    return 0L == currentValue;
                                } catch (WebDriverException e) {
                                    logger.warn("javascript error while waiting jQuery inactive");
                                    return true;
                                }
                            }

                            @Override
                            public String toString() {
                                return String.format("wait jQuery inactive to be 0 while current value is %d", currentValue);
                            }
                        });
                    }
                }
            };
        }

        @Override
        public IFluentAlertWait alert() {
            return new IFluentAlertWait() {
                @Override
                public IAlertWrapper toBePresent() {
                    return new AlertWrapper(wait.until(ExpectedConditions.alertIsPresent()));
                }
            };
        }

        @Override
        public IFluentPageWait page() {
            return new IFluentPageWait() {
                @Override
                public IFluentStringWait title() {
                    return new IFluentStringWait() {

                        @Override
                        public Boolean toBe(String text) {
                            return wait.until(ExpectedConditions.titleIs(text));
                        }

                        @Override
                        public Boolean toBeEmpty() {
                            return toBe("");
                        }

                        @Override
                        public Boolean notToBe(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageTitle();
                                    return !currentValue.equals(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page title not to be %s while current value is %s", text.isEmpty() ? "empty" : text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean contains(String text) {
                            return wait.until(ExpectedConditions.titleContains(text));
                        }

                        @Override
                        public Boolean notContains(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageTitle();
                                    return !currentValue.contains(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page title not contains %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean startsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageTitle();
                                    return currentValue.startsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page title starts with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean endsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageTitle();
                                    return currentValue.endsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page title ends with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean matches(final Pattern pattern) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    try {
                                        currentValue = getPageTitle();
                                        return pattern.matcher(currentValue).find();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page titie to match pattern %s while current value is %s", pattern.pattern(), currentValue);
                                }
                            });
                        }
                    }

                            ;
                }

                @Override
                public IFluentStringWait source() {
                    return new IFluentStringWait() {

                        @Override
                        public Boolean toBe(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return currentValue.equals(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source to be %s while current value is %s", text.isEmpty() ? "empty" : text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean toBeEmpty() {
                            return toBe("");
                        }

                        @Override
                        public Boolean notToBe(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return !currentValue.equals(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source not to be %s while current value is %s", text.isEmpty() ? "empty" : text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean contains(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return currentValue.contains(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source contains %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean notContains(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return !currentValue.contains(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source not contains %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean startsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return currentValue.startsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source starts with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean endsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getPageSource();
                                    return currentValue.endsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source ends with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean matches(final Pattern pattern) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    try {
                                        currentValue = getPageSource();
                                        return pattern.matcher(currentValue).find();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait page source matches pattern %s while current value is %s", pattern.pattern(), currentValue);
                                }
                            });
                        }
                    };
                }

                @Override
                public IFluentStringWait url() {
                    return new IFluentStringWait() {

                        @Override
                        public Boolean toBe(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return currentValue.equals(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url to be %s while current value is %s", text.isEmpty() ? "empty" : text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean toBeEmpty() {
                            return toBe("");
                        }

                        @Override
                        public Boolean notToBe(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return !currentValue.equals(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url not to be %s while current value is %s", text.isEmpty() ? "empty" : text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean contains(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return currentValue.contains(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url contains %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean notContains(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return !currentValue.contains(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url not contains %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean startsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return currentValue.startsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url starts with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean endsWith(final String text) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    currentValue = getCurrentUrl();
                                    return currentValue.endsWith(text);
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url ends with %s while current value is %s", text, currentValue);
                                }
                            });
                        }

                        @Override
                        public Boolean matches(final Pattern pattern) {
                            return wait.until(new ExpectedCondition<Boolean>() {
                                private String currentValue = null;

                                @Override
                                public Boolean apply(WebDriver driver) {
                                    try {
                                        currentValue = getCurrentUrl();
                                        return pattern.matcher(currentValue).find();
                                    } catch (Exception e) {
                                        return false;
                                    }
                                }

                                @Override
                                public String toString() {
                                    return String.format("wait current url matches pattern %s while current value is %s", pattern.pattern(), currentValue);
                                }
                            });
                        }
                    };
                }
            };
        }
    }

    private class FluentWebElementWrapperAssertion implements IFluentLocatorAssertion {
        private IWebElementWrapper webElementWrapper;

        public FluentWebElementWrapperAssertion(IWebElementWrapper webElementWrapper) {
            this.webElementWrapper = webElementWrapper;
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allSelectedTexts() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getAllSelectedTexts()).as("assert %s all selected texts", webElementWrapper);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> selectedText() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getSelectedText()).as("assert %s selected text", webElementWrapper);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allOptionTexts() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getAllOptionTexts()).as("assert %s all option texts", webElementWrapper);
        }

        @Override
        public AbstractBooleanAssert<?> present() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.isPresent()).as("assert %s present", webElementWrapper);
        }

        @Override
        public AbstractBooleanAssert<?> enabled() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.isEnabled()).as("assert %s enabled", webElementWrapper);
        }

        @Override
        public AbstractBooleanAssert<?> displayed() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.isDisplayed()).as("assert %s displayed", webElementWrapper);
        }

        @Override
        public AbstractBooleanAssert<?> selected() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.isSelected()).as("assert %s selected", webElementWrapper);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> innerText() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getInnerText()).as("assert %s innertText", webElementWrapper);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allInnerTexts() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getAllInnerTexts()).as("assert %s all innerTexts", webElementWrapper);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> attributeValueOf(String attribute) {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getAttribute(attribute)).as("assert attribute value of %s on %s", attribute, webElementWrapper);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> cssValueOf(String cssAttribute) {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getCssValue(cssAttribute)).as("assert css attribute value of %s on %s", cssAttribute, webElementWrapper);
        }

        @Override
        public AbstractIntegerAssert<? extends AbstractIntegerAssert<?>> numberOfElements() {
            return org.assertj.core.api.Assertions.assertThat(webElementWrapper.getNumberOfMatches()).as("assert number of elements %s", webElementWrapper);
        }

        @Override
        public IFluentLocatorAssertion nestedElements(Locator locator) {
            return null;
        }
    }

    private class FluentLocatorAssertion implements IFluentLocatorAssertion {
        private Locator locator;

        public FluentLocatorAssertion(Locator locator) {
            this.locator = locator;
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allSelectedTexts() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getAllSelectedTexts()).as("assert %s all selected texts", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> selectedText() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getSelectedText()).as("assert %s selected text", locator);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allOptionTexts() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getAllOptionTexts()).as("assert %s all option texts", locator);
        }

        @Override
        public AbstractBooleanAssert<?> present() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).isPresent()).as("assert %s present", locator);
        }

        @Override
        public AbstractBooleanAssert<?> enabled() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).isEnabled()).as("assert %s enabled", locator);
        }

        @Override
        public AbstractBooleanAssert<?> displayed() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).isDisplayed()).as("assert %s displayed", locator);
        }

        @Override
        public AbstractBooleanAssert<?> selected() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).isSelected()).as("assert %s selected", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> innerText() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getInnerText()).as("assert %s innertText", locator);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allInnerTexts() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getAllInnerTexts()).as("assert %s all innerTexts", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> attributeValueOf(String attribute) {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getAttribute(attribute)).as("assert attribute value of %s on %s", attribute, locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> cssValueOf(String cssAttribute) {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getCssValue(cssAttribute)).as("assert css attribute value of %s on %s", cssAttribute, locator);
        }

        @Override
        public AbstractIntegerAssert<? extends AbstractIntegerAssert<?>> numberOfElements() {
            return org.assertj.core.api.Assertions.assertThat(element(locator).getNumberOfMatches()).as("assert number of elements %s", locator);
        }

        @Override
        public IFluentLocatorAssertion nestedElements(Locator locator) {
            return null;
        }
    }

    private class FluentAssertion implements IFluentAssertion {
        @Override
        public IFluentAlertAssertion alert() {
            return new IFluentAlertAssertion() {
                @Override
                public AbstractBooleanAssert<?> present() {
                    return org.assertj.core.api.Assertions.assertThat(WebDriverWrapper.this.alert().isPresent()).as("assert alert present");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> text() {
                    return org.assertj.core.api.Assertions.assertThat(WebDriverWrapper.this.alert().getText()).as("assert alert text");
                }
            };
        }

        @Override
        public IFluentPageAssertion page() {
            return new IFluentPageAssertion() {
                @Override
                public AbstractCharSequenceAssert<?, String> title() {
                    return org.assertj.core.api.Assertions.assertThat(getPageTitle()).as("assert page title");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> source() {
                    return org.assertj.core.api.Assertions.assertThat(getPageSource()).as("assert page source");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> url() {
                    return org.assertj.core.api.Assertions.assertThat(getCurrentUrl()).as("assert current url");
                }
            };
        }

        @Override
        public IFluentStringAssertion string() {
            return new IFluentStringAssertion() {
                @Override
                public AbstractCharSequenceAssert<?, String> value(String value) {
                    return org.assertj.core.api.Assertions.assertThat(value).as("assert string value");
                }
            };
        }
    }

    private class FluentLocatorValidation implements IFluentLocatorAssertion {
        private Locator locator;

        public FluentLocatorValidation(Locator locator) {
            this.locator = locator;
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allSelectedTexts() {
            return getSoftAssertions().assertThat(element(locator).getAllSelectedTexts()).as("validate %s all selected texts", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> selectedText() {
            return getSoftAssertions().assertThat(element(locator).getSelectedText()).as("validate %s selected text", locator);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allOptionTexts() {
            return getSoftAssertions().assertThat(element(locator).getAllOptionTexts()).as("validate %s all option texts", locator);
        }

        @Override
        public AbstractBooleanAssert<?> present() {
            return getSoftAssertions().assertThat(element(locator).isPresent()).as("validate %s present", locator);
        }

        @Override
        public AbstractBooleanAssert<?> enabled() {
            return getSoftAssertions().assertThat(element(locator).isEnabled()).as("validate %s enabled", locator);
        }

        @Override
        public AbstractBooleanAssert<?> displayed() {
            return getSoftAssertions().assertThat(element(locator).isDisplayed()).as("validate %s displayed", locator);
        }

        @Override
        public AbstractBooleanAssert<?> selected() {
            return getSoftAssertions().assertThat(element(locator).isSelected()).as("validate %s selected", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> innerText() {
            return getSoftAssertions().assertThat(element(locator).getInnerText()).as("validate %s innertText", locator);
        }

        @Override
        public AbstractListAssert<? extends AbstractListAssert, ? extends List, String> allInnerTexts() {
            return getSoftAssertions().assertThat(element(locator).getAllInnerTexts()).as("validate %s all innerTexts", locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> attributeValueOf(String attribute) {
            return getSoftAssertions().assertThat(element(locator).getAttribute(attribute)).as("validate attribute value of %s on %s", attribute, locator);
        }

        @Override
        public AbstractCharSequenceAssert<?, String> cssValueOf(String cssAttribute) {
            return getSoftAssertions().assertThat(element(locator).getCssValue(cssAttribute)).as("validate css attribute value of %s on %s", cssAttribute, locator);
        }

        @Override
        public AbstractIntegerAssert<? extends AbstractIntegerAssert<?>> numberOfElements() {
            return getSoftAssertions().assertThat(element(locator).getNumberOfMatches()).as("validate number of elements %s", locator);
        }

        @Override
        public IFluentLocatorAssertion nestedElements(Locator locator) {
            return null;
        }
    }

    private class FluentValidation implements IFluentAssertion {
        @Override
        public IFluentAlertAssertion alert() {
            return new IFluentAlertAssertion() {
                @Override
                public AbstractBooleanAssert<?> present() {
                    return getSoftAssertions().assertThat(WebDriverWrapper.this.alert().isPresent()).as("validate alert present");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> text() {
                    return getSoftAssertions().assertThat(WebDriverWrapper.this.alert().getText()).as("validate alert text");
                }
            };
        }

        @Override
        public IFluentPageAssertion page() {
            return new IFluentPageAssertion() {
                @Override
                public AbstractCharSequenceAssert<?, String> title() {
                    return getSoftAssertions().assertThat(getPageTitle()).as("validate page title");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> source() {
                    return getSoftAssertions().assertThat(getPageSource()).as("validate page source");
                }

                @Override
                public AbstractCharSequenceAssert<?, String> url() {
                    return getSoftAssertions().assertThat(getCurrentUrl()).as("validate current url");
                }
            };
        }

        @Override
        public IFluentStringAssertion string() {
            return new IFluentStringAssertion() {
                @Override
                public AbstractCharSequenceAssert<?, String> value(String value) {
                    return getSoftAssertions().assertThat(value).as("validate string value");
                }
            };
        }
    }

    private class TargetLocatorWrapper implements ITargetLocatorWrapper {
        @Override
        public IWebElementWrapper activeElement() {
            logger.info("switching to active element");
            return element(driver.switchTo().activeElement());
        }

        @Override
        public IAlertWrapper alert() {
            logger.info("switching to active modal dialog");
            return new AlertWrapper(driver.switchTo().alert());
        }

        @Override
        public IWebDriverWrapper defaultContent() {
            logger.info("switching to default content");
            driver.switchTo().defaultContent();
            return doPostAction();
        }

        @Override
        public IWebDriverWrapper frame(int index) {
            logger.info("switching to frame {}", index);
            driver.switchTo().frame(index);
            return doPostAction();
        }

        @Override
        public IWebDriverWrapper frame(String nameOrId) {
            logger.info("switching to frame {}", nameOrId);
            driver.switchTo().frame(nameOrId);
            return doPostAction();
        }

        @Override
        public IWebDriverWrapper frame(Locator locator) {
            logger.info("switching to frame {}", locator);
            driver.switchTo().frame(driver.findElement(locator.by()));
            return doPostAction();
        }

        @Override
        public IWebDriverWrapper parentFrame() {
            logger.info("switching to parent frame");
            driver.switchTo().parentFrame();
            return doPostAction();
        }

        @Override
        public IWebDriverWrapper window(String nameOrHandle) {
            logger.info("switching to window {}", nameOrHandle);
            driver.switchTo().window(nameOrHandle);
            return doPostAction();
        }
    }
}
