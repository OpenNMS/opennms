/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

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
        // If the URL contains a username/password, it might need to be decoded
        if(urlStr.contains("@")){
            try {
                urlStr = URLDecoder.decode(urlStr, "UTF-8");
                LOG.debug("Decoded URL is: " + urlStr);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
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
