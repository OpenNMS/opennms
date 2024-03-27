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
package org.opennms.netmgt.protocols;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.ssh.SshServerDataProvider;
import org.opennms.core.test.ssh.SshServerDataProviderAware;
import org.opennms.core.test.ssh.annotations.JUnitSshServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.monitors.support.Ssh;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitSshServer
public class SshIT implements SshServerDataProviderAware {
    private static final InetAddress bad = InetAddressUtils.UNPINGABLE_ADDRESS;

    private static final int TIMEOUT = 500;

    private SshServerDataProvider sshServerProvider;
    private Ssh ssh;
    private TimeoutTracker tt;

    @Test
    public void testSshGood() throws Exception {
        init();

        final var host = sshServerProvider.getHost();
        ssh.setAddress(host);

        assertTrue("SSH poll against " + sshServerProvider.getHost() + " failed", ssh.poll(tt).isAvailable());
    }

    @Test
    public void testSshBad() throws Exception {
        init();
        ssh.setAddress(bad);

        final var start = new Date();
        assertFalse(ssh.poll(tt).isAvailable());
        final var end = new Date();

        // make sure it timed out in TIMEOUT ms
        assertTrue(end.getTime() - start.getTime() < (TIMEOUT * 1.2));
    }

    protected void init() throws Exception {
        final var parameters = new HashMap<String,String>();

        parameters.put("retries", "0");
        parameters.put("port", Integer.toString(sshServerProvider.getPort()));
        parameters.put("timeout", Integer.toString(TIMEOUT));
        
        tt = new TimeoutTracker(parameters, 0, TIMEOUT);
        ssh = new Ssh();
        ssh.setPort(sshServerProvider.getPort());
        ssh.setTimeout(TIMEOUT);
    }

    @Override
    public void setSshServerDataProvider(final SshServerDataProvider provider) {
        sshServerProvider = provider;
    }
    
}
