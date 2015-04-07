/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HttpNotificationStrategyTest {
    /*
     * Test method for 'org.opennms.netmgt.notifd.HttpNotificationStrategy.send(List)'
     */
    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/cgi-bin/noauth", path="src/test/resources/HttpNotificationStrategyTest")
    })
    public void testSend() {
        final int port = JUnitHttpServerExecutionListener.getPort();
        assertTrue(port > 0);
        try {
            final String message = "text message for unit testing";

            final NotificationStrategy ns = new HttpNotificationStrategy();
            final List<Argument> arguments = new ArrayList<Argument>();

            arguments.add(new Argument("url", null, "http://localhost:" + port + "/cgi-bin/noauth/nmsgw.pl", false));
            arguments.add(new Argument("post-NodeID", null, "1", false));
            arguments.add(new Argument("result-match", null, "(?s).*OK\\s+([0-9]+).*", false));
            arguments.add(new Argument("post-message", null, "-tm", false));
            arguments.add(new Argument("-tm", null, message, false));
            arguments.add(new Argument("sql", null, "UPDATE alarms SET tticketID=${1} WHERE lastEventID = 1", false));

            final int statusCode = ns.send(arguments);

            assertEquals(200, statusCode);
            
            final Map<String,String> parameters = HttpNotificationStrategyTestServlet.getRequestParameters();
            assertNotNull(parameters);
            assertEquals(2, parameters.size());
            assertEquals("1", parameters.get("NodeID"));
            assertEquals(message, parameters.get("message"));
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Caught Exception: " + e.getMessage());
        }
    }

}
