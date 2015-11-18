/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BSMAdminIT extends OpenNMSSeleniumTestCase {

    private final String RENAMED_SERVICE_NAME = "renamed_service";
    private final String BSM_LIST_URL = BASE_URL + "opennms/admin/bsm/index.jsp";
    private final String BASIC_SERVICE_NAME = "BasicService";
    private final String BASIC_SERVICE_NAME_TO_CHANGE = "BasicServiceToChange";

    @Test
    public void testCreateBusinessServices() {
        m_driver.get(BSM_LIST_URL);
        findElementById("createInput").sendKeys(BASIC_SERVICE_NAME);
        findElementById("createButton").click();
        findElementByXpath("//*[contains(text(), '" + BASIC_SERVICE_NAME + "')]");
    }

    @Test
    public void testDeleteBusinessServices() {
        m_driver.get(BSM_LIST_URL);
        findElementById("delete-" + BASIC_SERVICE_NAME).click();
        findElementByXpath("//*[contains(text(), 'There are no business services defined.')]");
    }

    @Test
    public void testEditBusinessServices() {
        //open the page and check that there are no BusinessServices
        m_driver.get(BSM_LIST_URL);
        findElementByXpath("//*[contains(text(), 'There are no business services defined.')]");

        //create a new BusinessService
        findElementById("createInput").sendKeys(BASIC_SERVICE_NAME_TO_CHANGE);
        findElementById("createButton").click();
        findElementByXpath("//*[contains(text(), '" + BASIC_SERVICE_NAME_TO_CHANGE + "')]");

        //edit the new BusinessService
        findElementById("edit-" + BASIC_SERVICE_NAME_TO_CHANGE).click();
        findElementById("bsName").clear();
        findElementById("bsName").sendKeys(RENAMED_SERVICE_NAME);
        findElementById("update").click();

        //verify that the changed BusinessService is displayed
        findElementByXpath("//*[contains(text(), '" + RENAMED_SERVICE_NAME + "')]");

        //delete the BusinessService to cleanup
        findElementById("delete-" + RENAMED_SERVICE_NAME).click();
        findElementByXpath("//*[contains(text(), 'There are no business services defined.')]");
    }

    @Test
    public void testShowNoBusinessServicesPresentMessage() {
        m_driver.get(BSM_LIST_URL);
        findElementByXpath("//*[contains(text(), 'There are no business services defined.')]");
    }
}
