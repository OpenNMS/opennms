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
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class HttpNotificationStrategyIT {
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
            final List<Argument> arguments = new ArrayList<>();

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
