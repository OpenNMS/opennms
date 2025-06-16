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
 * <p>SmtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class SmtpDetector extends AsyncMultilineDetectorMinaImpl {
    
    private static final String DEFAULT_SERVICE_NAME = "SMTP";
    private static final int DEFAULT_PORT = 25;

    /**
     * Default constructor
     */
    public SmtpDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public SmtpDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        setProtocolCodecFilter(new ProtocolCodecFilter(new MultilineOrientedCodecFactory(StandardCharsets.UTF_8, "-")));
        
        expectBanner(startsWith("220"));
        send(request("HELO LOCALHOST"), startsWith("250"));
        send(request("QUIT"), startsWith("221"));
    }
}
