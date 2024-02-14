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

import java.nio.charset.StandardCharsets;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.opennms.netmgt.provision.support.codec.MultilineOrientedCodecFactory;

/**
 * <p>FtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class FtpDetector extends AsyncMultilineDetectorMinaImpl {
    
    private static final String DEFAULT_SERVICE_NAME = "FTP";
    private static final int DEFAULT_PORT = 21;
    private String m_multilineIndicator = "-";
    
    /**
     * Default constructor
     */
    public FtpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public FtpDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        //setup the correct codec for this Detector
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineOrientedCodecFactory(StandardCharsets.UTF_8, getMultilineIndicator())));
        
        expectBanner(expectCodeRange(100, 600));
        send(request("quit"), expectCodeRange(100,600));
    }

    /**
     * <p>setMultilineIndicator</p>
     *
     * @param multilineIndicator a {@link java.lang.String} object.
     */
    public void setMultilineIndicator(final String multilineIndicator) {
        m_multilineIndicator = multilineIndicator;
    }

    /**
     * <p>getMultilineIndicator</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilineIndicator() {
        return m_multilineIndicator;
    }
}
