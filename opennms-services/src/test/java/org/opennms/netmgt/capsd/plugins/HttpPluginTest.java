/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class HttpPluginTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore("This operation is not supported yet, see http://issues.opennms.org/browse/NMS-2963")
    @JUnitHttpServer(port=10342, basicAuth=true)
    public void testHttpBasicAuth() throws UnknownHostException {
        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        Parameter p = new Parameter();

        HttpPlugin plugin = new HttpPlugin();

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("3000");
        m.put(p.getKey(), p.getValue());

        p.setKey("response");
        p.setValue("100-302");
        m.put(p.getKey(), p.getValue());

        p.setKey("verbose");
        p.setValue("true");
        m.put(p.getKey(), p.getValue());

        p.setKey("host-name");
        p.setValue("localhost");
        m.put(p.getKey(), p.getValue());

        p.setKey("url");
        p.setValue("/index.html");
        m.put(p.getKey(), p.getValue());

        assertFalse(plugin.isProtocolSupported(InetAddressUtils.getLocalHostAddress(), m));

        p.setKey("basic-authentication");
        p.setValue("admin:istrator");
        m.put(p.getKey(), p.getValue());

        assertTrue(plugin.isProtocolSupported(InetAddressUtils.getLocalHostAddress(), m));
    }
}
