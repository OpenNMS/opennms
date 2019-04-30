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

package org.opennms.smoketest.utils.org.opennms.smoketest.selenium;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumMatchers {

    private static final Logger LOG = LoggerFactory.getLogger(SeleniumMatchers.class);

    public static int countElementsMatchingCss(final WebDriver driver, final String css) {
        LOG.debug("countElementsMatchingCss: selector={}", css);

        // Selenium has a bug where the findElements(By) doesn't return elements; even if I attempt to do it manually
        // using JavascriptExecutor.execute(), so... parse the DOM on the Java side instead.  :/
        final org.jsoup.nodes.Document doc = Jsoup.parse(driver.getPageSource());
        final Elements matching = doc.select(css);
        return matching.size();

        // The original one-line implementation, for your edification.  Look at the majesty!
        // A single tear rolls down your cheek as you imagine what could have been, if
        // Selenium wasn't junk.
        //return getDriver().findElements(By.cssSelector(css)).size();
    }

}
