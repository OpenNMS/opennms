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

package org.opennms.protocols.sftp;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.protocols.xml.collector.UrlFactory;

/**
 * The Class Sftp3gppUrlConnectionTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppUrlConnectionTest {
    private static final Logger LOG = LoggerFactory.getLogger(Sftp3gppUrlConnectionTest.class);

    /**
     * Test path for Standard SFTP
     *
     * @throws Exception the exception
     */
    @Test
    public void testPathForSFTP() throws Exception {
        URL url = UrlFactory.getUrl("sftp://admin:admin@192.168.1.1/opt/hitachi/cnp/data/pm/reports/3gpp/5/data.xml", null);
        URLConnection conn = url.openConnection();
        Assert.assertTrue(conn instanceof SftpUrlConnection);
        UrlFactory.disconnect(conn);
    }

    /**
     * Test path for 3GPP (NE Mode ~ A).
     *
     * @throws Exception the exception
     */
    @Test
    public void testPathFor3GPPA() throws Exception {
        URL url = UrlFactory.getUrl("sftp.3gpp://admin:admin@192.168.1.1/opt/hitachi/cnp/data/pm/reports/3gpp/5?step=300&timezone=GMT-5&neId=MME00001", null);
        URLConnection conn = url.openConnection();
        Assert.assertTrue(conn instanceof Sftp3gppUrlConnection);
        String path = ((Sftp3gppUrlConnection) conn).getPath();
        LOG.debug(path);
        UrlFactory.disconnect(conn);
    }

    /**
     * Test path for 3GPP (NE Mode ~ A), using custom timestamp as a reference.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCustomPathFor3GPPA() throws Exception {
        long ts = 1320257100000l;
        Date date = new Date(ts);
        LOG.debug("Timestamp = {}", date);
        URL url = UrlFactory.getUrl("sftp.3gpp://admin:admin@192.168.1.1/opt/3gpp?step=300&timezone=GMT-5&neId=MME00001&referenceTimestamp=" + ts, null);
        URLConnection conn = url.openConnection();
        Assert.assertTrue(conn instanceof Sftp3gppUrlConnection);
        String path = ((Sftp3gppUrlConnection) conn).getPath();
        LOG.debug(path);
        UrlFactory.disconnect(conn);
        Assert.assertEquals("/opt/3gpp/A20111102.1300-0500-1305-0500_MME00001", path);
    }

    /**
     * Test the string to timestamp conversion for 3GPP file names.
     * 
     * @throws Exception the exception
     */
    @Test
    public void testGetTimeStampFromFile() throws Exception {
        URL url = UrlFactory.getUrl("sftp.3gpp://admin:admin@192.168.1.1/opt/3gpp?step=300&neId=MME00001&deleteFile=true", null);
        URLConnection conn = url.openConnection();
        Assert.assertTrue(conn instanceof Sftp3gppUrlConnection);
        Sftp3gppUrlConnection c = (Sftp3gppUrlConnection) conn;
        Assert.assertTrue(Boolean.parseBoolean(c.getQueryMap().get("deletefile")));
        long t1 = c.getTimeStampFromFile("A20111102.1300-0500-1305-0500_MME00001");
        long t2 = c.getTimeStampFromFile("A20111102.1305-0500-1310-0500_MME00001");
        Assert.assertTrue(t2 > t1);
        Assert.assertTrue(t2 - t1 == Long.parseLong(c.getQueryMap().get("step")) * 1000);
    }
}
