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
package org.opennms.protocols.nsclient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>JUnit Test Class for NsclientManager.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NSClientTest extends AbstractNsclientTest {

    private NsclientManager m_nsclientManager;

    private String[] counters = {
            "\\Processor(_Total)\\% Processor Time",
            "\\Processor(_Total)\\% Interrupt Time", 
            "\\Processor(_Total)\\% Privileged Time",
            "\\Processor(_Total)\\% User Time"
    };

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        startServer("None&8&", "10");
        m_nsclientManager = new NsclientManager(getServer().getInetAddress().getHostAddress(), getServer().getLocalPort());
    }

    @After
    @Override
    public void tearDown() throws Exception{
        stopServer();
        super.tearDown();
    }

    @Test
    public void testGetCounters() throws Exception {
        for (String counter : counters) {
            m_nsclientManager.init();
            NsclientPacket result = getCounter(counter);
            m_nsclientManager.close();
            validatePacket(counter, result);
        }
    }

    @Test
    public void testGetCountersWithSharedConnection() throws Exception {
        m_nsclientManager.init();
        for (String counter : counters) {
            NsclientPacket result = getCounter(counter);
            validatePacket(counter, result);
        }
        m_nsclientManager.close();
    }

    private void validatePacket(String counter, NsclientPacket result) {
        int value = Integer.parseInt(result.getResponse());
        System.err.println(counter + "=" + value);
        Assert.assertEquals(10, value);
        boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
        Assert.assertTrue(isAvailable);
    }    

    private NsclientPacket getCounter(String counter) throws NsclientException {
        NsclientCheckParams params = new NsclientCheckParams(counter);
        NsclientPacket result = m_nsclientManager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
        return result;
    }

}
