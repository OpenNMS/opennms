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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private static final Logger logger = LoggerFactory.getLogger(GenericURLConnection.class);

    /**
     * URL for connection
     */
    private URL m_url;

    /**
     * User and password delimiter for URL user:pass@host
     */
    private static final String USERINFO_DELIMITER = ":";

    /**
     * Delimiter for URL arguments
     */
    private static final String URL_QUERY_ARGS_DELIMITERS = "[&;]";

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
     * @param url a {java.net.URL} object
     */
    protected GenericURLConnection(URL url) {
        super(url);
        this.m_url = url;
    }

    /**
     * Get user name from a given URL
     *
     * @return a {@link java.lang.String} user name
     */
    protected String getUsername() {
        String userInfo = this.m_url.getUserInfo();
        if (userInfo != null) {
            if (userInfo.contains(USERINFO_DELIMITER)) {
                String[] userName = userInfo.split(USERINFO_DELIMITER);
                return userName[0]; // return the user name
            } else {
                logger.warn("Only user name without password configured. Return user info: '{}'", userInfo);
                return userInfo; // no password just a user name
            }
        } else {
            logger.warn("No credentials for URL connection configured.");
            return null; // no user info
        }
    }

    /**
     * Get password from a given url
     *
     * @return a {@link java.lang.String} password
     */
    protected String getPassword() {
        String userInfo = this.m_url.getUserInfo();
        if (userInfo != null) {
            if (userInfo.contains(USERINFO_DELIMITER)) {
                String[] userPass = userInfo.split(USERINFO_DELIMITER);
                return userPass[1];  // return password
            } else {
                logger.warn("Only user name without password configured. Return empty string as password");
                return EMPTY_STRING; // user name defined without password
            }
        } else {
            logger.warn("No credentials for URL connection configured.");
            return null; // no user info
        }
    }

    /**
     * Get all URL query arguments
     *
     * @return a {@link java.util.HashMap} with arguments as key value map
     */
    protected Map<String, String> getQueryArgs() {
        return getQueryStringParameters(m_url.getQuery());
    }

    public static Map<String, String> getQueryStringParameters(final String queryString) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        if (queryString != null) {

            String decodedQueryString = queryString;
            try {
                decodedQueryString = URLDecoder.decode(queryString, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                // Your system does not support UTF-8 encoding
                logger.error("Unsupported {} encoding for URL query string: '{}'. Error message: '{}'", StandardCharsets.UTF_8.name(), queryString, e.getMessage());
            }

            // queryString is everthing behind "?"
            String[] queryArgs = decodedQueryString.split(URL_QUERY_ARGS_DELIMITERS);

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
