/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.nsclient.capsd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.protocols.nsclient.NSClientAgentConfig;
import org.opennms.protocols.nsclient.NsclientCheckParams;
import org.opennms.protocols.nsclient.NsclientException;
import org.opennms.protocols.nsclient.NsclientManager;
import org.opennms.protocols.nsclient.NsclientPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test
 * whether a NSClient service is running on the remote server and if the given
 * command can be successfully executed against the service.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class NsclientPlugin extends AbstractPlugin {
	
	private static final Logger LOG = LoggerFactory.getLogger(NsclientPlugin.class);


    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "NSCLIENT";

    /**
     * Default number of retries for TCP requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000;

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller.
     * <P>
     * The NsclientPlugin does not support undirected checks, we must have a
     * map of parameters to determine how to issue a check to the target
     * server.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        throw new UnsupportedOperationException(
                                                "Undirected TCP checking not "
                                                        + "supported");
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller. The qualifier map passed to the method is used by the plugin to
     * return additional information by key-name. These key-value pairs can be
     * added to service events if needed.
     * <P>
     * The following parameters are used by this plugin:
     * <UL>
     * <LI>command - the command to be executed on this node.
     * <LI>port - used to override the default NSClient port.
     * <LI>password - used to override the default NSClient password
     * <LI>retry - overrides the number of times to retry connecting to the
     * service.
     * <LI>timeout - tcp port timeout.
     * <LI>parameter - a string used for checking services. see documentation
     * on specific check types for use.
     * <LI>criticalPercent - typically a percentage used for testing results,
     * for example disk space used.
     * <LI>warningPercent - typically a percentage used for testing results,
     * for example memory space used.
     * </UL>
     * Protocol will return as supported only if the result code is
     * <code>NsclientPacket.RES_STATE_OK</code> or
     * <code>NsclientPacket.RES_STATE_WARNING</code>.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;
        int port = NsclientManager.DEFAULT_PORT;

        String password = NSClientAgentConfig.DEFAULT_PASSWORD;
        String parameter = null;
        String command = null;
        int critPerc = 0, warnPerc = 0;

        if (qualifiers != null) {
            command = ParameterMap.getKeyedString(
                                                  qualifiers,
                                                  "command",
                                                  NsclientManager.convertTypeToString(NsclientManager.CHECK_CLIENTVERSION));
            port = ParameterMap.getKeyedInteger(qualifiers, "port",
                                                NsclientManager.DEFAULT_PORT);
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry",
                                                   DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout",
                                                   DEFAULT_TIMEOUT);
            parameter = ParameterMap.getKeyedString(qualifiers, "parameter",
                                                    null);
            critPerc = ParameterMap.getKeyedInteger(qualifiers,
                                                    "criticalPercent", 0);
            warnPerc = ParameterMap.getKeyedInteger(qualifiers,
                                                    "warningPercent", 0);
            password = ParameterMap.getKeyedString(qualifiers, "password",
                                                   NSClientAgentConfig.DEFAULT_PASSWORD);
        }

        // set up my check params.
        NsclientCheckParams params = new NsclientCheckParams(critPerc,
                                                             warnPerc,
                                                             parameter);
        // and perform the check, we'll get a packet back containing the check
        // data.
        NsclientPacket pack = isServer(address, port, password, command, retries,
                                       timeout, params);

        if (pack == null) {
            LOG.debug("Received a null packet response from isServer.");
            return false;
        }

        // only fail on critical and unknown returns .
        if (pack.getResultCode() != NsclientPacket.RES_STATE_CRIT
                && pack.getResultCode() != NsclientPacket.RES_STATE_UNKNOWN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * <P>
     * Test to see if the paassed host-port pair is an endpoint for a TCP
     * server. If there is a TCP server at the destination value then a
     * connection is made using the params variable data and a check is
     * requested from the remote NSClient service.
     * </P>
     * 
     * @param host
     *            The remote host to connect to.
     * @param port
     *            The remote port on the host.
     * @param command
     *            The command to execute on the remote server.
     * @param retries
     *            The number of retries to attempt when connecting.
     * @param timeout
     *            The TCP socket timeout to use.
     * @param params
     *            The NSClient parameters used to validate the response.
     * @return The NsclientPacket the server sent, updated by NsclientManager
     *         to contain the proper result code based on the params passed.
     */
    private NsclientPacket isServer(InetAddress host, int port,
            String password, String command, int retries, int timeout,
            NsclientCheckParams params) {
        boolean isAServer = false;

        NsclientPacket response = null;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            try {
                NsclientManager client = new NsclientManager(InetAddressUtils.str(host), port, password);

                client.setTimeout(timeout);
                client.init();

                response = client.processCheckCommand(NsclientManager.convertStringToType(command), params);
                LOG.debug("NsclientPlugin: {}: {}", command, response.getResponse());
                isAServer = true;
            } catch (NsclientException e) {
                StringBuffer message = new StringBuffer();
                message.append("NsclientPlugin: Check failed... NsclientManager returned exception: ");
                message.append(e.getMessage());
                message.append(" : ");
                message.append((e.getCause() == null ? "": e.getCause().getMessage()));
                LOG.info(message.toString());
                isAServer = false;
            }
        }
        return response;
    }


}
