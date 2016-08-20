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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * The Test Class for the Index Page.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class IndexPageIT extends OpenNMSSeleniumTestCase {

    /**
     * Can render search boxes.
     *
     * @throws Exception the exception
     */
    @Test
    public void canRenderSearchBoxes() throws Exception {
        m_driver.get(getBaseUrl() + "opennms/index.jsp");
        // The following input fields will exist on index.jsp, only if includes/search-box.jsp is rendered and processed by AngularJS
        WebElement asyncKsc = findElementByXpath("//input[@ng-model='asyncKsc']");
        Assert.assertNotNull(asyncKsc);
        WebElement asyncNode = findElementByXpath("//input[@ng-model='asyncNode']");
        Assert.assertNotNull(asyncNode);
    }

}
