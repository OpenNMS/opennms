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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TextInput extends UiElement {

    public TextInput(WebDriver driver, String elementId) {
        super(driver, elementId);
    }

    public void setInput(String newInput) {
        final WebElement element = execute(() -> driver.findElement(By.id(elementId)));
        if (!Objects.equals(element.getText(), newInput)) {
            element.clear();
            if (newInput != null && !newInput.isEmpty()) {
                element.sendKeys(newInput);
            }
        }
    }

    public void setInput(Integer newInput) {
        if (newInput != null) {
            setInput(newInput.toString());
        }
    }
}
