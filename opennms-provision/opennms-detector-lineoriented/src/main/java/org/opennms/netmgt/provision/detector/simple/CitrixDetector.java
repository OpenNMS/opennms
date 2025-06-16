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
 * Detects the presence of the Citrix service on an IP address using
 * a line oriented protocol.
 *
 * @author <a href="mailto:thedesloge@opennms.org">Donald Desloge</a>
 * @version $Id: $
 */


public class CitrixDetector extends AsyncLineOrientedDetectorMinaImpl {
    
    
    private static final String DEFAULT_SERVICE_NAME = "CITRIX";
    private static final int DEFAULT_PORT = 1494;

    /**
     * Default constructor
     */
    public CitrixDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public CitrixDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(startsWith("ICA"));
    }


}
