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

import java.io.File;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.RrdException;
import org.junit.Test;
import org.opennms.netmgt.jasper.helper.JRobinDirectoryUtil;

public class RrdXportCmdTest {
    
    @Test
    public void testExecute() throws RrdException, IOException, JRException {
         JRobinDataSource dataSource = (JRobinDataSource) new RrdXportCmd().executeCommand(getQueryString());
         assertTrue(dataSource.next());
    }

    /**
     * This test attempts to create a JRobinDataSource from JRB files
     * in a directory that has colons in the path, like the latency
     * metrics for an IPv6 interface would.
     * 
     * @throws RrdException
     * @throws IOException
     * @throws JRException
     */
    @Test
    public void testExecuteInIPv6Directory() throws RrdException, IOException, JRException {
        File tempDir = null;
        try {
            tempDir = new File(FileUtils.getTempDirectory(), getClass().getSimpleName());
            File tempSubDir = new File(tempDir, "0000:0000:0000:0000:0000:0000:0000:0001");
            if (tempSubDir.mkdirs()) {
                FileUtils.copyFileToDirectory(new File("src/test/resources/http-8980.jrb"), tempSubDir);
                FileUtils.copyFileToDirectory(new File("src/test/resources/ssh.jrb"), tempSubDir);
                String queryString = getIPv6QueryString(new File(tempSubDir, "http-8980.jrb").toString(), new File(tempSubDir, "ssh.jrb").toString());

                JRobinDataSource dataSource = (JRobinDataSource) new RrdXportCmd().executeCommand(queryString);
                assertTrue(dataSource.next());
            } else {
                System.out.println("Test directory could not be created on this filesystem.");
            }
        } finally {
            FileUtils.deleteDirectory(tempDir);
        }
   }

    private String getIPv6QueryString(String httpPath, String sshPath) {
        return "--start 1287005100 --end 1287018990 DEF:xx=" + JRobinDirectoryUtil.escapeColons(httpPath) + ":http-8980:AVERAGE DEF:zz=" + JRobinDirectoryUtil.escapeColons(sshPath) + ":ssh:AVERAGE XPORT:xx:HttpLatency XPORT:zz:SshLatency";
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
