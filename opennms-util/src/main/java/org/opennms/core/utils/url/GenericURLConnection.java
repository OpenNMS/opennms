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
