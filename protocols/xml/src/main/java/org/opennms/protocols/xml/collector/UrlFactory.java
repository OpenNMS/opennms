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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.opennms.protocols.http.HttpUrlConnection;
import org.opennms.protocols.http.HttpUrlHandler;
import org.opennms.protocols.http.HttpsUrlHandler;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.sftp.SftpUrlConnection;
import org.opennms.protocols.sftp.SftpUrlHandler;
import org.opennms.protocols.xml.config.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating URL objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class UrlFactory {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(UrlFactory.class);

    /**
     * Instantiates a new URL factory.
     */
    private UrlFactory() {}

    /**
     * Gets the URL Object.
     * <p>This method has been created because it is not possible to call URL.setURLStreamHandlerFactory more than once.</p>
     *
     * @param urlStr the URL String
     * @param request the request
     * @return the URL Object
     * @throws MalformedURLException the malformed URL exception
     */
    public static URL getUrl(String urlStr, Request request) throws MalformedURLException {
        URL url = null;
        String protocol = null;
        try {
            protocol = urlStr.substring(0, urlStr.indexOf("://")).toLowerCase();
        } catch (Exception e) {
            return null;
        }
        if (SftpUrlHandler.PROTOCOL.equals(protocol)) {
            url = new URL(null, urlStr, new SftpUrlHandler());
        } else if (Sftp3gppUrlHandler.PROTOCOL.equals(protocol)) {
            url = new URL(null, urlStr, new Sftp3gppUrlHandler());
        } else if (HttpUrlHandler.HTTP.equals(protocol)) {
            url = new URL(null, urlStr, new HttpUrlHandler(request));
        } else if (HttpsUrlHandler.HTTPS.equals(protocol)) {
            url = new URL(null, urlStr, new HttpsUrlHandler(request));
        } else {
            url = new URL(urlStr);
        }
        return url;
    }

    /**
     * Disconnect.
     *
     * @param connection the URL connection
     */
    public static void disconnect(URLConnection connection) {
        try {
            if (connection == null)
                return;
            if (connection instanceof SftpUrlConnection) // We need to be sure to close the connections for SFTP
                ((SftpUrlConnection)connection).disconnect();
            if (connection instanceof HttpUrlConnection)
                ((HttpUrlConnection)connection).disconnect();
        } catch (Exception e) {
            LOG.error("Can't close open connection.", e);
        }
    }
}
