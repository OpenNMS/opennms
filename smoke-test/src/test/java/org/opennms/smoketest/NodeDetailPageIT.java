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

import org.junit.Test;

public class NodeDetailPageIT extends OpenNMSSeleniumTestCase {

    // See NMS-10679
    @Test
    public void verifyNodeNotFoundMessageIsShown() {
        final String NODE_NOT_FOUND = "Node Not Found";
        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=12345");
        pageContainsText(NODE_NOT_FOUND);

        final String NODE_ID_NOT_FOUND = "Node ID Not Found";
        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=abc");
        pageContainsText(NODE_ID_NOT_FOUND);
        m_driver.get(getBaseUrl() + "opennms/element/node.jsp?node=ab:cd");
        pageContainsText(NODE_ID_NOT_FOUND);
    }
}
