/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.utils.org.opennms.smoketest.selenium;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SeleniumUtils.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SeleniumUtils(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver);
        wait = new WebDriverWait(driver, TimeUnit.SECONDS.convert(2, TimeUnit.MINUTES));
    }

    public WebDriverWait getWait() {
        return wait;
    }

    public WebElement findElementByName(final String name) {
        LOG.debug("findElementByName: name={}", name);
        return driver.findElement(By.name(name));
    }

    public WebElement findElementById(final String id) {
        LOG.debug("findElementById: id={}", id);
        return driver.findElement(By.id(id));
    }

    public WebElement waitForElement(final By by) {
        return waitForElement(wait, by);
    }

    public WebElement waitForElement(final WebDriverWait w, final By by) {
        return waitUntil(null, w, new Callable<WebElement>() {
            @Override public WebElement call() throws Exception {
                final WebElement el = driver.findElement(by);
                if (el.isDisplayed() && el.isEnabled()) {
                    return el;
                }
                return null;
            }
        });
    }

    public <T> T waitUntil(final Callable<T> callable) {
        return waitUntil(null, wait, callable);
    }

    public <T> T waitUntil(final WebDriverWait w, final Callable<T> callable) {
        return waitUntil(null, w, callable);
    }

    public <T> T waitUntil(final Long implicitWait, final WebDriverWait w, final Callable<T> callable) {
        return waitUntil(implicitWait, w, d -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T waitUntil(final Long implicitWait, final WebDriverWait w, final ExpectedCondition<T> condition) {
        final WebDriverWait wdw = w == null? wait : w;
        try {
            setImplicitWait(implicitWait == null? 50 : implicitWait, TimeUnit.MILLISECONDS);
            return wdw.until(condition);
        } finally {
            setImplicitWait();
        }
    }

    public void invokeWithImplicitWait(int implicitWait, Runnable runnable) {
        Objects.requireNonNull(runnable);
        try {
            // Disable implicitlyWait
            setImplicitWait(Math.max(0, implicitWait), TimeUnit.MILLISECONDS);
            runnable.run();
        } finally {
            setImplicitWait();
        }
    }

    protected WebDriver.Timeouts setImplicitWait() {
        return setImplicitWait(2, TimeUnit.MINUTES);
    }

    protected WebDriver.Timeouts setImplicitWait(final long time, final TimeUnit unit) {
        LOG.trace("Setting implicit wait to {} milliseconds.", unit.toMillis(time));
        return driver.manage().timeouts().implicitlyWait(time, unit);
    }


    /**
     * CAUTION: There are a variety of Firefox-specific bugs related to using
     * {@link WebElement#sendKeys(CharSequence...)}. We're doing this bizarre
     * sequence of operations to try and work around them.
     *
     * @see https://code.google.com/p/selenium/issues/detail?id=2487
     * @see https://code.google.com/p/selenium/issues/detail?id=8180
     */
    @SuppressWarnings("JavadocReference")
    protected WebElement enterText(final By selector, final CharSequence... text) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence seq : text) {
            sb.append(seq);
        }
        final String textString = sb.toString();
        LOG.debug("Enter text: '{}' into selector: '{}'", text, selector);

        // First, attempt to focus on the element before typing
        scrollToElement(selector);
        final WebElement el = waitForElement(selector);
        if (el.isDisplayed() && el.isEnabled()) {
            el.click();
        }
        sleep(500);

        final long end = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
        boolean found = false;
        int count = 0;

        final WebDriverWait shortWait = new WebDriverWait(driver, 10);

        do {
            LOG.debug("enterText({},{}): {}", selector, text, ++count);
            // Clear the element content and then confirm it's really clear
            waitForElement(selector).clear();
            waitForValue(selector, "");

            // Click the element to make sure it's still got the focus
            waitForElement(selector).click();
            sleep(500);
            // Send the keys
            waitForElement(selector).sendKeys(text);
            waitForElement(selector).click();
            sleep(500);

            if (text.length == 1 && text[0] != Keys.ENTER) { // special case, carriage-return for a previously-entered entry
                try {
                    final String elementText = waitForElement(shortWait, selector).getText();
                    final String elementValue = waitForElement(shortWait, selector).getAttribute("value");
                    found = elementText.contains(textString) || elementValue.contains(textString);
                } catch (final Exception e) {
                    LOG.warn("Failed when checking for {} to equal '{}'.", selector, textString, e);
                }
            } else {
                LOG.info("Skipped waiting for {} to equal {}", selector, textString);
                found = true;
            }
        } while (!found && System.currentTimeMillis() < end);

        return driver.findElement(selector);
    }

    protected void sleep(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected WebElement scrollToElement(final By by) {
        return scrollToElement(by, true);
    }

    protected WebElement scrollToElement(final By by, final boolean waitForElement) {
        LOG.debug("scrollToElement: by={}", by);

        if (waitForElement) {
            try {
                setImplicitWait(200, TimeUnit.MILLISECONDS);
                final WebElement element = wait.until(new ExpectedCondition<WebElement>() {
                    @Override public WebElement apply(final WebDriver driver) {
                        final WebElement el = waitForElement? waitForElement(by) : driver.findElement(by);
                        return doScroll(driver, el);
                    }

                });
                if (element == null) {
                    throw new RuntimeException("Failed to scroll to element: " + by);
                }
                return element;
            } finally {
                setImplicitWait();
            }
        } else {
            final WebElement el = driver.findElement(by);
            return doScroll(driver, el);
        }
    }

    private WebElement doScroll(final WebDriver driver, final WebElement element) {
        try {
            final List<Integer> bounds = getBoundedRectangleOfElement(driver, element);
            final int windowHeight = driver.manage().window().getSize().getHeight();
            final JavascriptExecutor je = (JavascriptExecutor)driver;
            je.executeScript("window.scrollTo(0, " + (bounds.get(1) - (windowHeight/2)) + ");");
            return element;
        } catch (final Exception e) {
            return null;
        }
    }

    protected static List<Integer> getBoundedRectangleOfElement(final WebDriver driver, final WebElement we) {
        LOG.debug("getBoundedRectangleOfElement: element={}", we);
        final JavascriptExecutor je = (JavascriptExecutor)driver;
        final List<String> bounds = (ArrayList<String>) je.executeScript(
                "var rect = arguments[0].getBoundingClientRect();" +
                        "return [ '' + parseInt(rect.left), '' + parseInt(rect.top), '' + parseInt(rect.width), '' + parseInt(rect.height) ]", we);
        final List<Integer> ret = new ArrayList<>();
        for (final String entry : bounds) {
            ret.add(Integer.valueOf(entry));
        }
        return ret;
    }

    protected void waitForValue(final By selector, final String value) {
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return value.equals(driver.findElement(selector).getAttribute("value"));
            }
        });
    }
}
