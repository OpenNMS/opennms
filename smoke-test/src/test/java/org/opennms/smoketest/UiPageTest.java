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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class UiPageTest extends OpenNMSSeleniumIT {

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

    protected class TextInput {
        private final String id;

        public TextInput(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public void setInput(String newInput) {
            final WebElement element = execute(() -> driver.findElement(By.id(id)));
            if (!Objects.equals(element.getText(), newInput)) {
                element.clear();
                if (newInput != null && !newInput.equals("")) {
                    element.sendKeys(newInput);
                }
            }
        }
    }

    protected class Toggle {
        private final String id;

        public Toggle(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public boolean isOn() {
            final String xpath = String.format("//toggle[@id='%s']//div[contains(@class, 'toggle') and not(contains(@class, 'toggle-group')) and not(contains(@class, 'off'))]", id);
            final List<WebElement> elements = execute(() -> driver.findElements(By.xpath(xpath)));
            boolean disabled = elements.isEmpty();
            return !disabled;
        }

        public void toggle() {
            boolean previousState = isOn();
            execute(() -> driver.findElement(By.id(id))).click();
            new WebDriverWait(driver, 5, 500).until((Function<WebDriver, Boolean>) webDriver -> previousState != isOn());
        }

        public void setValue(boolean value) {
            if (value != isOn()) {
                toggle();
            }
        }
    }
}
