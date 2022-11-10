/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
