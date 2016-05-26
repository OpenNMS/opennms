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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

/**
 * Simple checks to verify the REST endpoints provided
 * by the OSGi Plugin Manager are reachable.
 *
 * @author jwhite
 */
public class OSGIPluginManagerIT extends OpenNMSSeleniumTestCase {

    @Test
    public void canListProducts() throws ClientProtocolException, IOException, InterruptedException {
        assertEquals(Integer.valueOf(200), doRequest(new HttpGet(getBaseUrl() + "/opennms/licencemgr/rest/v1-0/product-pub/list")));
    }

    @Test
    public void canListFeatures() throws ClientProtocolException, IOException, InterruptedException {
        assertEquals(Integer.valueOf(200), doRequest(new HttpGet(getBaseUrl() + "/opennms/featuremgr/rest/v1-0/features-list")));
    }
}
