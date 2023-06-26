/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

