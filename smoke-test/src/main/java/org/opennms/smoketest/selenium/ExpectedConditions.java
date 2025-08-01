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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExpectedConditions {

    private ExpectedConditions() {}

    private static final Logger LOG = LoggerFactory.getLogger(ExpectedConditions.class);

    public static ExpectedCondition<Boolean> pageContainsText(String text) {
        LOG.debug("pageContainsText: {}", text);
        final String escapedText = text.replace("\'", "\\\'");
        return driver -> {
            final String xpathExpression = "//*[contains(., '" + escapedText + "')]";
            LOG.debug("XPath expression: {}", xpathExpression);
            try {
                final WebElement element = driver.findElement(By.xpath(xpathExpression));
                return element != null;
            } catch (final NoSuchElementException e) {
                return false;
            }
        };
    }
}
