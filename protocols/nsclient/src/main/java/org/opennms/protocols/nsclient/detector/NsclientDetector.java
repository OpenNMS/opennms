/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
