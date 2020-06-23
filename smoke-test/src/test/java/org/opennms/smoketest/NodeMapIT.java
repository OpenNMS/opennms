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

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.SECONDS;

import org.junit.Test;

public class NodeMapIT extends OpenNMSSeleniumIT {

    /**
     * Checks whether a node without interfaces will appear on the node map. See NMS-12171 for details.
     */
    @Test
    public void testNodeMaps() throws Exception {
        try {
            deleteTestRequisition();
            // create dummy node without any interface
            final String node = "<node type=\"A\" label=\"TestMachine\" foreignSource=\"" + REQUISITION_NAME + "\" foreignId=\"TestMachine\">" +
                    "<labelSource>H</labelSource>" +
                    "<sysContact>The Owner</sysContact>" +
                    "<sysDescription>" +
                    "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
                    "</sysDescription>" +
                    "<sysLocation>DevJam</sysLocation>" +
                    "<sysName>TestMachine</sysName>" +
                    "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
                    "</node>";
            sendPost("/rest/nodes", node, 201);

            // add geo location
            sendPut("rest/nodes/" + REQUISITION_NAME + ":TestMachine/assetRecord", "longitude=30", 204);
            sendPut("rest/nodes/" + REQUISITION_NAME + ":TestMachine/assetRecord", "latitude=30", 204);

            // navigate to the node-map page
            this.driver.get(this.getBaseUrlInternal() + "opennms/node-maps");

            // check whether the node will appear even if it has no associated interface
            with().pollInterval(1, SECONDS).await().atMost(20, SECONDS).until(() -> (findElementByXpath("//*[contains(@class, 'leaflet-marker-icon')]") != null));
        } finally {
            deleteTestRequisition();
        }
    }
}
