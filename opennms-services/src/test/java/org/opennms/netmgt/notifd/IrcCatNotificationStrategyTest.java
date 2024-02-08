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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;

/**
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class IrcCatNotificationStrategyTest {
    /**
     * This doesn't really do anything, but it's a placeholder so that the testSend() test can be left disabled.
     */
    @Test
    public void testInstantiate() {
        new IrcCatNotificationStrategy();
    }
    
    //@Test
    public void testSend() throws UnknownHostException {
        IrcCatNotificationStrategy strategy = new IrcCatNotificationStrategy();
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument(NotificationManager.PARAM_EMAIL, null, "#opennms-test", false));
        arguments.add(new Argument(NotificationManager.PARAM_TEXT_MSG, null, "Test notification from " + getClass() + " from " + InetAddress.getLocalHost(), false));
        strategy.send(arguments);
    }
}
 