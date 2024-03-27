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
package org.opennms.netmgt.provision.detector.ssh;

import org.opennms.netmgt.provision.detector.ssh.client.SshClient;
import org.opennms.netmgt.provision.detector.ssh.request.NullRequest;
import org.opennms.netmgt.provision.detector.ssh.response.SshResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.ssh.Ssh;

/**
 * <p>SshDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SshDetector extends BasicDetector<NullRequest, SshResponse>{
    
    private static final String DEFAULT_SERVICE_NAME = "SSH";
    private static final int DEFAULT_PORT = 22;
    private String m_banner = null;
    private String m_match = null;
    private String m_clientBanner = Ssh.DEFAULT_CLIENT_BANNER;
    
    /**
     * Default constructor
     */
    public SshDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public SshDetector(String serviceName, int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<NullRequest, SshResponse> getClient() {
        SshClient client = new SshClient();
        client.setBanner(getBanner());
        client.setMatch(getMatch());
        client.setClientBanner(getClientBanner());
        return client;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(sshIsAvailable());
    }

    /**
     * @return
     */
    private static ResponseValidator<SshResponse> sshIsAvailable() {
        
        return new ResponseValidator<SshResponse>(){

            @Override
            public boolean validate(SshResponse response) {
                return response.isAvailable();
            }
            
        };
    }

    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(String banner) {
        m_banner = banner;
    }

    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }

    /**
     * <p>setMatch</p>
     *
     * @param match a {@link java.lang.String} object.
     */
    public void setMatch(String match) {
        m_match = match;
    }

    /**
     * <p>getMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMatch() {
        return m_match;
    }

    /**
     * <p>setClientBanner</p>
     *
     * @param clientBanner a {@link java.lang.String} object.
     */
    public void setClientBanner(String clientBanner) {
        m_clientBanner = clientBanner;
    }

    /**
     * <p>getClientBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClientBanner() {
        return m_clientBanner;
    }
	
}
