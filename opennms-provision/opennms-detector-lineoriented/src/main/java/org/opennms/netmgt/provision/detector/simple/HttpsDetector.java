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
package org.opennms.netmgt.provision.detector.simple;


/**
 * <p>HttpsDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class HttpsDetector extends HttpDetector {
    
    
    private static final String DEFAULT_SERVICE_NAME = "HTTPS";
    private static final int DEFAULT_PORT = 443;

    /**
     * <p>Constructor for HttpsDetector.</p>
     */
    public HttpsDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
        setUseSSLFilter(true);
        setUrl("/");
        setMaxRetCode(500);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public HttpsDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
}
