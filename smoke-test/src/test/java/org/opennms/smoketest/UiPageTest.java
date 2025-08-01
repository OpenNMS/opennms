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
package org.opennms.smoketest;

import java.time.Duration;
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
        new WebDriverWait(driver, Duration.ofSeconds(7)).until(
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
