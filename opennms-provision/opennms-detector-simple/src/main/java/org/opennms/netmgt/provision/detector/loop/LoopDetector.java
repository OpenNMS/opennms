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
package org.opennms.netmgt.provision.detector.loop;

import org.opennms.netmgt.provision.detector.loop.client.LoopClient;
import org.opennms.netmgt.provision.detector.loop.response.LoopResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>LoopDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class LoopDetector extends BasicDetector<LineOrientedRequest, LoopResponse> {
    private static final String DEFAULT_SERVICE_NAME = "LOOP";
    private static final int DEFAULT_PORT = 0;
    
    private String m_ipMatch;
    private boolean m_isSupported = true;
    
    /**
     * <p>Constructor for LoopDetector.</p>
     */
    public LoopDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LoopResponse> getClient() {
        LoopClient loopClient = new LoopClient();
        loopClient.setSupported(isSupported());
        return loopClient;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(ipMatches(getIpMatch()));
    }

    private static ResponseValidator<LoopResponse> ipMatches(final String ipAddr) {
        
        return new ResponseValidator<LoopResponse>(){

            @Override
            public boolean validate(LoopResponse response) {
                return response.validateIPMatch(ipAddr);
            }
            
        };
    }

    /**
     * <p>setIpMatch</p>
     *
     * @param ipMatch a {@link java.lang.String} object.
     */
    public void setIpMatch(String ipMatch) {
        m_ipMatch = ipMatch;
    }

    /**
     * <p>getIpMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpMatch() {
        return m_ipMatch;
    }

    /**
     * <p>setSupported</p>
     *
     * @param isSupported a boolean.
     */
    public void setSupported(boolean isSupported) {
        m_isSupported = isSupported;
    }

    /**
     * <p>isSupported</p>
     *
     * @return a boolean.
     */
    public boolean isSupported() {
        return m_isSupported;
    }

}
