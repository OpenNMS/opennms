/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.poller.nsclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NSClientTest {
    
    NsclientManager m_nsclientManager;

    String[] counters = {
            "\\Processor(_Total)\\% Processor Time",
            "\\Processor(_Total)\\% Interrupt Time", 
            "\\Processor(_Total)\\% Privileged Time",
            "\\Processor(_Total)\\% User Time"
    };

    @Before
    public void setUp() throws Exception {
    	// Change this to your NSClient test server
        m_nsclientManager = new NsclientManager("192.168.149.250", 12489);
    }
    
    @Test
    @Ignore
    public void testGetCounters() throws Exception {
        for (String counter : counters) {
            m_nsclientManager.init();
            NsclientPacket result = getCounter(counter);
            m_nsclientManager.close();
            System.err.println(counter + "=" + result.getResponse());
            boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
            Assert.assertTrue(isAvailable);            
        }
    }
    
    @Test
    @Ignore
    public void testGetCountersWithSharedConnection() throws Exception {
        m_nsclientManager.init();
        for (String counter : counters) {
            NsclientPacket result = getCounter(counter);
            System.err.println(counter + "=" + result.getResponse());
            boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
            Assert.assertTrue(isAvailable);            
        }
        m_nsclientManager.close();
    }

    private NsclientPacket getCounter(String counter) throws NsclientException {
        NsclientCheckParams params = new NsclientCheckParams(counter);
        NsclientPacket result = m_nsclientManager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
        return result;
    }

}
