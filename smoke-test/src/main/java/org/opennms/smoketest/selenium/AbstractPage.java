/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AbstractPage {
    protected static final long SHORT_WAIT_SECONDS = 5;
    protected static final long LONG_WAIT_SECONDS = 10;

    protected final AbstractOpenNMSSeleniumHelper testCase;

    public AbstractPage(AbstractOpenNMSSeleniumHelper testCase) {
        this.testCase = Objects.requireNonNull(testCase);
    }

    protected WebDriver getDriver() {
        return testCase.getDriver();
    }

    protected void get(String path) {
        final String fullURL = testCase.buildUrlInternal(path);
        getDriver().get(fullURL);
    }

    protected List<WebElement> findElements(By by) {
        try {
            testCase.setImplicitWait(LONG_WAIT_SECONDS, TimeUnit.SECONDS);
            return getDriver().findElements(by);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement findElement(By by) {
        try {
            testCase.setImplicitWait(SHORT_WAIT_SECONDS, TimeUnit.SECONDS);
            return getDriver().findElement(by);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement findElementByName(final String name) {
        try {
            testCase.setImplicitWait(SHORT_WAIT_SECONDS, TimeUnit.SECONDS);
            return testCase.findElementByName(name);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement findElementByXpath(final String xpath) {
        try {
            testCase.setImplicitWait(SHORT_WAIT_SECONDS, TimeUnit.SECONDS);
            return testCase.findElementByXpath(xpath);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement clickElement(final By by) {
        try {
            testCase.setImplicitWait(SHORT_WAIT_SECONDS, TimeUnit.SECONDS);
            return testCase.clickElement(by);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement enterText(final By by, final String text) {
        return testCase.enterText(by, text);
    }

    protected void waitUntil(ExpectedCondition<Boolean> condition) {
        try {
            testCase.setImplicitWait(LONG_WAIT_SECONDS, TimeUnit.SECONDS);
            new WebDriverWait(getDriver(), Duration.ofSeconds(LONG_WAIT_SECONDS)).until(condition);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected ExpectedCondition<Boolean> pageContainsText(String text) {
        return testCase.pageContainsText(text);
    }
}
