package org.opennms.smoketest.expectations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Expectation {
    private static final Logger LOG = LoggerFactory.getLogger(Expectation.class);
    final ExecutorService m_executor = Executors.newFixedThreadPool(5);

    public static enum Type {
        OR,
        AND
    }

    private final String m_target;
    private Type m_matchType = Type.OR;
    private List<String> m_textPresent = new ArrayList<>();
    private Long m_waitTime;
    private TimeUnit m_waitUnits;

    public Expectation(final String target) {
        m_target = target;
    }

    public void setMatchType(final Expectation.Type type) {
        m_matchType = type;
    }

    public void setTextPresent(final List<String> textPresent) {
        if (textPresent != m_textPresent) {
            m_textPresent.clear();
            m_textPresent.addAll(textPresent);
        }
    }

    public void addTextPresent(final String text) {
        m_textPresent.add(text);
    }

    public void setWaitTime(final Long time, final TimeUnit units) {
        m_waitTime = time;
        m_waitUnits = units;
    }

    public void check(final WebDriver webdriver) throws Exception {
        final Wait<WebDriver> wait = new WebDriverWait(webdriver, TimeUnit.SECONDS.convert(OpenNMSSeleniumTestCase.LOAD_TIMEOUT, TimeUnit.MILLISECONDS));
        
        final WebElement element;
        if (m_target.startsWith("link=")) {
            final String target = m_target.replaceFirst("link=", "");
            element = wait.until(visibilityOfElementLocated(By.linkText(target)));
        } else if (m_target.startsWith("css=")) {
            final String target = m_target.replaceFirst("css=", "");
            element = wait.until(visibilityOfElementLocated(By.cssSelector(target)));
        } else {
            element = wait.until(visibilityOfElementLocated(By.xpath(m_target)));
        }
        LOG.debug("found element: {}", element);
        element.click();

        if (m_textPresent.size() == 0) {
            return;
        }

        if (m_waitTime != null) {
            Thread.sleep(m_waitUnits.toMillis(m_waitTime));
        }

        final CompletionService<Boolean> completionService = new ExecutorCompletionService<>(m_executor);
        final List<Future<Boolean>> futures = Collections.synchronizedList(new ArrayList<Future<Boolean>>());

        for (final String tp : m_textPresent) {
            futures.add(completionService.submit(new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    return wait.until(pageContainsText(tp));
                }
            }));
        }

        if (m_matchType == Type.OR) {
            while (futures.size() > 0) {
                final Future<Boolean> future = completionService.take();
                futures.remove(future);
                if (future.get() == true) {
                    for (final Future<Boolean> f : futures) {
                        f.cancel(true);
                    }
                    // we passed, exit the matching
                    return;
                }
            }
            // we never received a true value
            throw new ExpectationFailed(this);
        } else if (m_matchType == Type.AND) {
            while (futures.size() > 0) {
                final Future<Boolean> future = completionService.take();
                futures.remove(future);
                if (future.get() == false) {
                    for (final Future<Boolean> f : futures) {
                        f.cancel(true);
                    }
                    throw new ExpectationFailed(this, "isTextPresent &= " + m_textPresent);
                }
            }
        }
    }

    private ExpectedCondition<WebElement> visibilityOfElementLocated(final By locator) {
        return new ExpectedCondition<WebElement>() {
            @Override public WebElement apply(WebDriver driver) {
                final WebElement toReturn = driver.findElement(locator);
                if (toReturn.isDisplayed()) {
                    return toReturn;
                }
                return null;
            }
        };
    }

    private ExpectedCondition<Boolean> pageContainsText(final String text) {
        final String escapedText = text.replace("\'", "\\\'");
        return new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(final WebDriver driver) {
                final String xpathExpression = "//*[contains(., '" + escapedText + "')]";
                LOG.debug("XPath expression: {}", xpathExpression);
                final WebElement element = driver.findElement(By.xpath(xpathExpression));
                return element != null;
            }
        };
    }

    @Override
    public String toString() {
        return "Expectation [target=" + m_target + ", matchType=" + m_matchType + ", textPresent=" + m_textPresent + "]";
    }
}