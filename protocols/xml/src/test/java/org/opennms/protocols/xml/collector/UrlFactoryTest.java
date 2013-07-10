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

package org.opennms.protocols.xml.collector;

import java.net.URL;

import junit.framework.Assert;

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
