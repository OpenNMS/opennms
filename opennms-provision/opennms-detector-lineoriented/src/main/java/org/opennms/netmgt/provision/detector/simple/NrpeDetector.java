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

import org.opennms.netmgt.provision.detector.simple.client.NrpeClient;
import org.opennms.netmgt.provision.detector.simple.request.NrpeRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.nrpe.NrpePacket;

/**
 * <p>NrpeDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */

public class NrpeDetector extends BasicDetector<NrpeRequest, NrpePacket> {
    
    private static final String DEFAULT_SERVICE_NAME = "NRPE";

    private static final int DEFAULT_PORT = 5666;
    
    /**
     * Default whether to use SSL
     */
    private static final boolean DEFAULT_USE_SSL = true;
    
    private boolean m_useSsl = DEFAULT_USE_SSL;
    private int m_padding = 2;
    private String m_command = NrpePacket.HELLO_COMMAND;

    /**
     * Default constructor
     */
    public NrpeDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public NrpeDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(m_command), resultCodeEquals(0));
    }
    
    private static ResponseValidator<NrpePacket> resultCodeEquals(final int desiredResultCode){
        return new ResponseValidator<NrpePacket>() {

            @Override
            public boolean validate(final NrpePacket response) {
                if(response.getResultCode() == desiredResultCode) {
                    return true;
                }
                return false;
            }
            
        };
    }
    
    /**
     * @return
     */
    private NrpeRequest request(final String command) {
        final NrpePacket packet = new NrpePacket(NrpePacket.QUERY_PACKET, (short) 0, command);
        final byte[] b = packet.buildPacket(getPadding());
        return new NrpeRequest(b);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<NrpeRequest, NrpePacket> getClient() {
        final NrpeClient client = new NrpeClient();
        client.setPadding(getPadding());
        client.setUseSsl(isUseSsl());
        return client;
    }


    /**
     * <p>setUseSsl</p>
     *
     * @param useSsl a boolean.
     */
    public void setUseSsl(final boolean useSsl) {
        m_useSsl = useSsl;
    }

    /**
     * <p>isUseSsl</p>
     *
     * @return a boolean.
     */
    public boolean isUseSsl() {
        return m_useSsl;
    }

    /**
     * <p>setPadding</p>
     *
     * @param padding a int.
     */
    public void setPadding(final int padding) {
        m_padding = padding;
    }

    /**
     * <p>getPadding</p>
     *
     * @return a int.
     */
    public int getPadding() {
        return m_padding;
    }

    /**
     * <p>setCommand</p>
     *
     * @param command a String.
     */
    public void setCommand(final String command) {
        this.m_command = command;
    }

    /**
     * <p>getCommand</p>
     *
     * @return a String.
     */
    public String getCommand() {
        return m_command;
    }

}
