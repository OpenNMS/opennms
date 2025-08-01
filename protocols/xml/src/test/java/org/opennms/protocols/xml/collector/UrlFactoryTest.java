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
package org.opennms.protocols.xml.collector;

import java.net.URL;

import org.junit.Assert;

import org.junit.Test;
import org.opennms.protocols.http.HttpUrlConnection;
import org.opennms.protocols.http.HttpUrlHandler;
import org.opennms.protocols.http.HttpsUrlHandler;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.sftp.SftpUrlConnection;
import org.opennms.protocols.sftp.SftpUrlHandler;

/**
 * The Test Class for UrlFactory.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class UrlFactoryTest {

    /**
     * Test time parser.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUrlFactory() throws Exception {
        URL url = UrlFactory.getUrl("https://www.google.com", null);
        Assert.assertEquals(HttpsUrlHandler.HTTPS, url.getProtocol());
        Assert.assertTrue(url.openConnection() instanceof HttpUrlConnection);
        url = UrlFactory.getUrl("HTTP://www.opennms.org", null);
        Assert.assertEquals(HttpUrlHandler.HTTP, url.getProtocol());
        Assert.assertTrue(url.openConnection() instanceof HttpUrlConnection);
        url = UrlFactory.getUrl("sftp://www.opennms.org", null);
        Assert.assertEquals(SftpUrlHandler.PROTOCOL, url.getProtocol());
        Assert.assertTrue(url.openConnection() instanceof SftpUrlConnection);
        url = UrlFactory.getUrl("sftp.3GPP://junier-router.local/opt/3gpp/data", null);
        Assert.assertEquals(Sftp3gppUrlHandler.PROTOCOL, url.getProtocol());
        Assert.assertTrue(url.openConnection() instanceof Sftp3gppUrlConnection);
    }

}
