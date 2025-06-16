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

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

public class CheckBox extends UiElement {

    public CheckBox(WebDriver driver, String elementId) {
        super(driver, elementId);
    }

    public void setSelected(boolean selected) {
        LOG.debug("Update setSelected {} of element with id: {}", selected, elementId);
        final Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(Exception.class);

        if (selected != isSelected()) {
            // Wait until the element is clickable and toggle the state by clicking
            wait.until(driver -> elementToBeClickable(getElement()));
            getElement().click();
            // Wait for the state change
            // Verifying the state immediately does not always return the expected result
            wait.until(driver -> selected == isSelected());
        }

        // Fail if we have not produced the expected state
        LOG.debug("Expecting {} for isSelected. Actual value is {}. Element id: {}", selected, isSelected(), elementId);
        assertEquals(String.format("Expected checkbox to be selected=%s", selected), selected, isSelected());
    }

    public boolean isSelected() {
        return getElement().isSelected();
    }
}
