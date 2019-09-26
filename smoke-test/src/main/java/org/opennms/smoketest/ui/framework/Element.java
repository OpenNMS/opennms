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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Element {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected final WebDriver driver;
    private final int implicitWait;
    private final TimeUnit implicitWaitUnit;

    public Element(final WebDriver driver, int implicitWait, TimeUnit implictWaitUnit) {
        this.driver = driver;
        this.implicitWait = implicitWait;
        this.implicitWaitUnit = Objects.requireNonNull(implictWaitUnit);
    }

    public Element(final WebDriver driver) {
        this(driver, 2, TimeUnit.SECONDS);
    }

    public WebElement findElementById(final String id) {
        LOG.debug("findElementById: id={}", id);
        return execute(() -> driver.findElement(By.id(id)));
    }

    public WebElement findElementByLink(final String link) {
        LOG.debug("findElementByLink: link={}", link);
        return execute(() -> driver.findElement(By.linkText(link)));
    }

    public WebElement findElementByName(final String name) {
        LOG.debug("findElementByName: name={}", name);
        return execute(() -> driver.findElement(By.name(name)));
    }

    public WebElement findElementByCss(final String css) {
        LOG.debug("findElementByCss: selector={}", css);
        return execute(() -> driver.findElement(By.cssSelector(css)));
    }

    public WebElement findElementByXpath(final String xpath) {
        LOG.debug("findElementByXpath: selector={}", xpath);
        return execute(() -> driver.findElement(By.xpath(xpath)));
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected <X> X execute(Supplier<X> supplier) {
        try {
            driver.manage().timeouts().implicitlyWait(implicitWait, implicitWaitUnit.SECONDS);
            return supplier.get();
        } finally {
            driver.manage().timeouts().implicitlyWait(AbstractOpenNMSSeleniumHelper.LOAD_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }
}
