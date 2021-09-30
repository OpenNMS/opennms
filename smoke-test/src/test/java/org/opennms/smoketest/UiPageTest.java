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

package org.opennms.smoketest;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;

public class UiPageTest extends OpenNMSSeleniumIT {

    @Rule
    public TestRule loggingRule = (base, description) -> {
        LoggerFactory.getLogger(UiPageTest.this.getClass()).debug("Executing test: {}.{}()", description.getClassName(), description.getMethodName());
        return base;
    };

    @Before
    public void before() {
        setImplicitWait(5, TimeUnit.SECONDS);
    }

    @After
    public void after() {
        setImplicitWait();
    }

    protected <X> X execute(Supplier<X> supplier) {
        return execute(supplier, 1);
    }

    protected <X> X execute(Supplier<X> supplier, int implicitWaitInSeconds) {
        try {
            this.setImplicitWait(implicitWaitInSeconds, TimeUnit.SECONDS);
            return supplier.get();
        } finally {
            this.setImplicitWait();
        }
    }

    protected void verifyElementNotPresent(final By by) {
        new WebDriverWait(driver, 7 /* seconds */).until(
                ExpectedConditions.not((ExpectedCondition<Boolean>) input -> execute(() -> {
                    try {
                        WebElement elementFound = input.findElement(by);
                        return elementFound != null;
                    } catch (NoSuchElementException ex) {
                        return false;
                    }
                }, 5 /* seconds */))
        );
    }

}
