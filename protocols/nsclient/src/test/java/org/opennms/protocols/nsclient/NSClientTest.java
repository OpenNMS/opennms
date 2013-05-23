/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
