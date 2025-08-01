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
package org.opennms.protocols.sftp;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.junit.Assert;

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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Sftp3gppUrlConnectionTest.class);

    /**
     * Test path for Standard SFTP.
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
