/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractPage {

    protected final OpenNMSSeleniumTestCase testCase;

    public AbstractPage(OpenNMSSeleniumTestCase testCase) {
        this.testCase = Objects.requireNonNull(testCase);
    }

    protected WebDriver getDriver() {
        return testCase.m_driver;
    }

    protected void get(String path) {
        final String fullURL = testCase.buildUrl(path);
        getDriver().get(fullURL);
    }

    protected List<WebElement> findElements(By by) {
        try {
            testCase.setImplicitWait(5, TimeUnit.SECONDS);
            return getDriver().findElements(by);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected WebElement findElement(By by) {
        try {
            testCase.setImplicitWait(1, TimeUnit.SECONDS);
            return getDriver().findElement(by);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected void waitUntil(ExpectedCondition<Boolean> condition) {
        try {
            testCase.setImplicitWait(5, TimeUnit.SECONDS);
            new WebDriverWait(getDriver(), 5).until(condition);
        } finally {
            testCase.setImplicitWait();
        }
    }

    protected ExpectedCondition<Boolean> pageContainsText(String text) {
        return testCase.pageContainsText(text);
    }
}
