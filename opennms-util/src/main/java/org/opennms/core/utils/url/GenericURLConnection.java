/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.url;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience classes to provide additional functions on URL connections.
 *
 * @author <a href="mailto:christian.pape@informatik.hs-fulda.de">Christian Pape</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public abstract class GenericURLConnection extends URLConnection {

    /**
     * Logging to output.log
     */
    private final Logger logger = LoggerFactory.getLogger(GenericURLConnection.class);

    /**
     * URI for connection
     */
    private URI m_uri;

    /**
     * User and password delimiter for URI user:pass@host
     */
    private static final String USERINFO_DELIMITER = ":";

    /**
     * Default encoding for URI
     */
    private static final String UTF8_ENCODING = "UTF-8";

    /**
     * Delimiter for URI arguments
     */
    private static final String URI_QUERY_ARGS_DELIMITERS = "[&;]";

    /**
     * Delimiter for argument and values
     */
    private static final String KEY_VALUE_DELIMITER = "=";

    /**
     * Empty String
     */
    private static final String EMPTY_STRING = "";

    /**
     * Default constructor
     *
     * @param uri a {java.net.URI} object
     */
    protected GenericURLConnection(URI uri) throws java.net.MalformedURLException {

        super(uri.toURL());

        this.m_uri = uri;
    }

    /**
     * Get user name from a given URI
     *
     * @return a {@link java.lang.String} user name
     */
    protected String getUsername() {
        String userInfo = this.m_uri.getUserInfo();
        if (userInfo != null) {
            if (userInfo.contains(USERINFO_DELIMITER)) {
                String[] userName = userInfo.split(USERINFO_DELIMITER);
                return userName[0]; // return the user name
            } else {
                logger.warn("Only user name without password configured. Return user info: '{}'", userInfo);
                return userInfo; // no password just a user name
            }
        } else {
            logger.warn("No credentials for URI connection configured.");
            return null; // no user info
        }
    }

    /**
     * Get password from a given URI
     *
     * @return a {@link java.lang.String} password
     */
    protected String getPassword() {
        String userInfo = this.m_uri.getUserInfo();
        if (userInfo != null) {
            if (userInfo.contains(USERINFO_DELIMITER)) {
                String[] userPass = userInfo.split(USERINFO_DELIMITER);
                return userPass[1];  // return password
            } else {
                logger.warn("Only user name without password configured. Return empty string as password");
                return EMPTY_STRING; // user name defined without password
            }
        } else {
            logger.warn("No credentials for URI connection configured.");
            return null; // no user info
        }
    }

    /**
     * Get all URI query arguments
     *
     * @return a {@link java.util.HashMap} with arguments as key value map
     */
    protected Map<String, String> getQueryArgs() {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        String queryString = this.m_uri.getQuery();

        if (queryString != null) {

            try {
                queryString = URLDecoder.decode(queryString, UTF8_ENCODING);
            } catch (UnsupportedEncodingException e) {
                // Your system does not support UTF-8 encoding
                logger.error("Unsupported " + UTF8_ENCODING + " encoding for URI query string: '{}'. Error message: '{}'", queryString, e.getMessage());
            }

            // queryString is everthing behind "?"
            String[] queryArgs = queryString.split(URI_QUERY_ARGS_DELIMITERS);

            for (String queryArg : queryArgs) {

                String key = queryArg;
                String value = EMPTY_STRING;

                if (queryArg.contains(KEY_VALUE_DELIMITER)) {
                    String[] keyValue = queryArg.split(KEY_VALUE_DELIMITER);

                    // Assign key[KEY_VALUE_DELIMITER]value
                    key = keyValue[0];
                    value = keyValue[1];
                }

                if (!EMPTY_STRING.equals(key)) {
                    hashMap.put(key, value);
                    logger.debug("Key: '{}' : Value: '{}'", key, value);
                }
            }
        }
        return hashMap;
    }
}
