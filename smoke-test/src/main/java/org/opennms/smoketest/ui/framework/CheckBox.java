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
