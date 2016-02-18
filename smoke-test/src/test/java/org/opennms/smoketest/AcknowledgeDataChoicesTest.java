/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * This test is named so that it is the first class to run.
 *
 * FIXME: This logic must be moved into the default OpenNMSSeleniumTestCase
 * implementation (or alternative) before being merge upstream.
 *
 * @author jwhite
 */
public class AcknowledgeDataChoicesTest extends OpenNMSSeleniumTestCase {

    @Before
    public void before() {
        m_driver.get(BASE_URL+"opennms");
    }

    @Test
    public void canEnableDatachoices() throws Exception {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id("datachoices-enable")));
            findElementById("datachoices-enable").click();
        } catch (Throwable t) {
            // pass
        }
    }
}
