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
        this.driver = Objects.requireNonNull(driver);
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
