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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Shared click-guard helpers to reduce Selenium click flakiness checks drifting across call sites.
 */
public final class ElementClickGuards {
    private static final String IS_CENTER_POINT_OBSCURED_SCRIPT =
            "var el = arguments[0];"
            + "if (!el) { return true; }"
            + "var rect = el.getBoundingClientRect();"
            + "if (rect.width === 0 || rect.height === 0) { return true; }"
            + "var x = rect.left + (rect.width / 2);"
            + "var y = rect.top + (rect.height / 2);"
            + "var top = document.elementFromPoint(x, y);"
            + "if (!top) { return true; }"
            + "return top !== el && !el.contains(top);";

    private ElementClickGuards() {
    }

    public static boolean isCenterPointObscured(final JavascriptExecutor executor, final WebElement element) {
        final Object result = executor.executeScript(IS_CENTER_POINT_OBSCURED_SCRIPT, element);
        return result instanceof Boolean && (Boolean) result;
    }
}