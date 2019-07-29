/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.ui.framework;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class UiElement {
    protected final WebDriver driver;
    protected final String elementId;

    private final int implicitWait;
    private final TimeUnit implicitWaitUnit;

    public UiElement(final WebDriver driver, final String elementId, int implicitWait, TimeUnit implictWaitUnit) {
        this.elementId = Objects.requireNonNull(elementId);
        this.driver = driver;
        this.implicitWait = implicitWait;
        this.implicitWaitUnit = Objects.requireNonNull(implictWaitUnit);
    }

    public UiElement(final WebDriver driver, final String elementId) {
        this(driver, elementId, 2, TimeUnit.SECONDS);
    }

    protected <X> X execute(Supplier<X> supplier) {
        try {
            driver.manage().timeouts().implicitlyWait(implicitWait, implicitWaitUnit.SECONDS);
            return supplier.get();
        } finally {
            driver.manage().timeouts().implicitlyWait(AbstractOpenNMSSeleniumHelper.LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    protected WebElement getElement() {
        return execute(() -> driver.findElement(By.id(elementId)));
    }
}
