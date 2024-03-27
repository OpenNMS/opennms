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

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class Toggle extends UiElement {

    public Toggle(WebDriver driver, String elementId) {
        super(driver, elementId);
    }

    public boolean isOn() {
        final String xpath = String.format("//toggle[@id='%s']//div[contains(@class, 'toggle') and not(contains(@class, 'toggle-group')) and not(contains(@class, 'off'))]", elementId);
        final List<WebElement> elements = execute(() -> driver.findElements(By.xpath(xpath)));
        boolean disabled = elements.isEmpty();
        return !disabled;
    }

    public void toggle() {
        boolean previousState = isOn();
        execute(() -> driver.findElement(By.id(elementId))).click();
        new WebDriverWait(driver, Duration.ofSeconds(5), Duration.ofMillis(500)).until((Function<WebDriver, Boolean>) webDriver -> previousState != isOn());
    }

    public void setValue(boolean value) {
        if (value != isOn()) {
            toggle();
        }
    }
}
