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
package org.opennms.protocols.nsclient.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.protocols.nsclient.NSClientAgentConfig;
import org.opennms.protocols.nsclient.NsclientManager;
import org.opennms.protocols.nsclient.NsclientPacket;
import org.opennms.protocols.nsclient.detector.client.NsclientClient;
import org.opennms.protocols.nsclient.detector.request.NsclientRequest;

/**
 * <p>NsclientDetector class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */

public class NsclientDetector extends BasicDetector<NsclientRequest, NsclientPacket> {

    private static final String DEFAULT_SERVICE_NAME = "NSClient";

    private String command = NsclientManager.convertTypeToString(NsclientManager.CHECK_CLIENTVERSION);

    private String password = NSClientAgentConfig.DEFAULT_PASSWORD;

    private String parameter;

    private int warnPerc;

    private int critPerc;

    /**
     * Default constructor
     */
    public NsclientDetector() {
        super(DEFAULT_SERVICE_NAME, NSClientAgentConfig.DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public NsclientDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    @Override
    protected void onInit() {
        send(getRequest(), getNsclientValidator());
    }

    private static ResponseValidator<NsclientPacket> getNsclientValidator() {
        return new ResponseValidator<NsclientPacket>() {
            @Override
            public boolean validate(final NsclientPacket pack) {
                // only fail on critical and unknown returns.
                return pack != null
                    && pack.getResultCode() != NsclientPacket.RES_STATE_CRIT
                    && pack.getResultCode() != NsclientPacket.RES_STATE_UNKNOWN;
            }
        };
    }

    private NsclientRequest getRequest() {
        final NsclientRequest request = new NsclientRequest();
        request.setCommand(getCommand());
        request.setParameter(getParameter());
        request.setWarnPerc(getWarnPerc());
        request.setCritPerc(getCritPerc());
        request.setRetries(getRetries());
        return request;
    }

    @Override
    protected Client<NsclientRequest, NsclientPacket> getClient() {
        final NsclientClient client = new NsclientClient();
        client.setPassword(getPassword());
        return client;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public int getWarnPerc() {
        return warnPerc;
    }

    public void setWarnPerc(int warnPerc) {
        this.warnPerc = warnPerc;
    }

    public int getCritPerc() {
        return critPerc;
    }

    public void setCritPerc(int critPerc) {
        this.critPerc = critPerc;
    }

}
