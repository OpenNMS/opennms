/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.jrobin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.jasperreports.engine.JRException;

import org.jrobin.core.RrdException;
import org.junit.Test;


public class RrdXportCmdTest {
    
    @Test
    public void testExecute() throws RrdException, IOException, JRException {
         JRobinDataSource dataSource = (JRobinDataSource) new RrdXportCmd().executeCommand(getQueryString());
         assertTrue(dataSource.next());
    }
    


    private String getQueryString() {
        return "--start 1287005100 --end 1287018990 DEF:xx=src/test/resources/http-8980.jrb:http-8980:AVERAGE DEF:zz=src/test/resources/ssh.jrb:ssh:AVERAGE XPORT:xx:HttpLatency XPORT:zz:SshLatency";
    }
    
    private String getWindowsString() {
        return "--start 1287005100 --end 1287018990 DEF:xx=\\jrbs\\http-8980.jrb:http-8980:AVERAGE DEF:zz=\\jrbs\\ssh.jrb:ssh:AVERAGE XPORT:xx:HttpLatency XPORT:zz:SshLatency";
    }
    
    @Test
    public void testColonSplitter() throws RrdException {
        String[] splitter = new ColonSplitter("DEF:xx=C\\:\\jrbs\\http-8980.jrb:http-8980:AVERAGE").split();
        assertEquals(4, splitter.length);
    }
}
