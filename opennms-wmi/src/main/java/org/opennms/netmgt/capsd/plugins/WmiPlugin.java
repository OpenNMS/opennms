//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 12 Feb 2010: Avoid an NPE when isServer returns a null (bug 3555) - jeffg@opennms.org
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.protocols.wmi.WmiAgentConfig;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test whether
 * a WMI service is running on the remote server and if the given class/object
 * can be successfully retrieved from the service.
 * </P>
 * 
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class WmiPlugin extends AbstractPlugin {
	/**
	 * The protocol supported by the plugin
	 */
	private final static String PROTOCOL_NAME = "WMI";

	private final static String DEFAULT_WMI_CLASS = "Win32_ComputerSystem";
	private final static String DEFAULT_WMI_OBJECT = "Status";
	private final static String DEFAULT_WMI_COMP_VAL = "OK";
	private final static String DEFAULT_WMI_MATCH_TYPE = "all";
	private final static String DEFAULT_WMI_COMP_OP = "EQ";
    private final static String DEFAULT_WMI_WQL = "NOTSET";

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
	 * Returns true if the protocol defined by this plugin is supported. If the
	 * protocol is not supported then a false value is returned to the caller.
	 * <P>
	 * The WmiPlugin does not support undirected checks, we must have a map of
	 * parameters to determine how to issue a check to the target server.
	 * 
	 * @param address
	 *            The address to check for support.
	 * @return True if the protocol is supported by the address.
	 * @throws java.lang.UnsupportedOperationException
	 *             This is always thrown by this plugin.
	 */
	@Override
	public boolean isProtocolSupported(InetAddress address) {
		throw new UnsupportedOperationException("Undirected TCP checking not "
				+ "supported");
	}

	/**
	 * Returns true if the protocol defined by this plugin is supported. If the
	 * protocol is not supported then a false value is returned to the caller.
	 * The qualifier map passed to the method is used by the plugin to return
	 * additional information by key-name. These key-value pairs can be added to
	 * service events if needed.
	 * <P>
	 * The following parameters are used by this plugin:
	 * <UL>
	 * <LI>wmiObject - the command to be executed on this node.
	 * <LI>wmiClass - the command to be executed on this node.
	 * <LI>password - used to override the default WMI password
	 * <LI>retry - overrides the number of times to retry connecting to the
	 * service.
	 * <LI>timeout - tcp port timeout.
	 * <LI>parameter - a string used for checking services. see documentation
	 * on specific check types for use.
	 * </UL>
	 * Protocol will return as supported only if the result code is
	 * <code>WmiResult.RES_STATE_OK</code> or
	 * <code>WmiResult.RES_STATE_WARNING</code>.
	 * 
	 * @param address
	 *            The address to check for support.
	 * @param qualifiers
	 *            The map where qualification are set by the plugin.
	 * @return True if the protocol is supported by the address.
	 */
	@Override
	public boolean isProtocolSupported(InetAddress address,
			Map<String, Object> qualifiers) {
		WmiAgentConfig agentConfig = WmiPeerFactory.getInstance().getAgentConfig(address);
		String matchType = DEFAULT_WMI_MATCH_TYPE;
		String compVal = DEFAULT_WMI_COMP_VAL;
		String compOp = DEFAULT_WMI_COMP_OP;
		String wmiClass = DEFAULT_WMI_CLASS;
		String wmiObject = DEFAULT_WMI_OBJECT;
        String wmiWqlStr = DEFAULT_WMI_WQL;

        if (qualifiers != null) {
            if (qualifiers.get("timeout") != null) {
            	int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", agentConfig.getTimeout());
                agentConfig.setTimeout(timeout);
            }
            
            if (qualifiers.get("retry") != null) {
            	int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", agentConfig.getRetries());
                agentConfig.setRetries(retries);
            }

            if (qualifiers.get("username") != null) {
                String user = ParameterMap.getKeyedString(qualifiers, "username", agentConfig.getUsername());
                agentConfig.setUsername(user);
            }
            
            if (qualifiers.get("password") != null) {
                String pass = ParameterMap.getKeyedString(qualifiers, "password", agentConfig.getPassword());
                agentConfig.setUsername(pass);
            }
            
            if (qualifiers.get("domain") != null) {
                String domain = ParameterMap.getKeyedString(qualifiers, "domain", agentConfig.getDomain());
                agentConfig.setUsername(domain);
            }
            
            matchType = ParameterMap.getKeyedString(qualifiers, "matchType",
					DEFAULT_WMI_MATCH_TYPE);
			compVal = ParameterMap.getKeyedString(qualifiers, "compareValue",
					DEFAULT_WMI_COMP_VAL);
			compOp = ParameterMap.getKeyedString(qualifiers, "compareOp", DEFAULT_WMI_COMP_OP);
            wmiWqlStr = ParameterMap.getKeyedString(qualifiers, "wql", DEFAULT_WMI_WQL);
            wmiClass = ParameterMap.getKeyedString(qualifiers, "wmiClass",
					DEFAULT_WMI_CLASS);
			wmiObject = ParameterMap.getKeyedString(qualifiers, "wmiObject",
					DEFAULT_WMI_OBJECT);
		}

        WmiParams clientParams = null;

        if(wmiWqlStr.equals(DEFAULT_WMI_WQL)) {
            // Create the check parameters holder.
		    clientParams = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF,
                                         compVal, compOp, wmiClass, wmiObject);
        } else {
            // Define the WQL Query.
            clientParams = new WmiParams(WmiParams.WMI_OPERATION_WQL,
                                         compVal, compOp, wmiWqlStr, wmiObject);
        }


		// Perform the operation specified in the parameters.
		WmiResult result = isServer(address, agentConfig.getUsername(), agentConfig.getPassword(), agentConfig.getDomain(), matchType,
				agentConfig.getRetries(), agentConfig.getTimeout(), clientParams);

		// Only fail on critical and unknown returns.
		if (result != null && result.getResultCode() != WmiResult.RES_STATE_CRIT
				&& result.getResultCode() != WmiResult.RES_STATE_UNKNOWN) {

			return true;
		} else {
			return false;
		}

	}

	/**
	 * <P>
	 * Test to see if the passed host-port pair is an endpoint for a TCP server.
	 * If there is a TCP server at the destination value then a connection is
	 * made using the params variable data and a check is requested from the
	 * remote WMI service.
	 * </P>
	 * 
	 * @param host
	 *            The remote host to connect to.
	 * @param retries
	 *            The number of retries to attempt when connecting.
	 * @param timeout
	 *            The TCP socket timeout to use.
	 * @param params
	 *            The WMI parameters used to validate the response.
	 * @return The WmiResult the server sent, updated by WmiManager to
	 *         contain the proper result code based on the params passed.
	 */
	private WmiResult isServer(InetAddress host, String user, String pass,
			String domain, String matchType, int retries, int timeout,
			WmiParams params) {
		boolean isAServer = false;

		WmiResult result = null;
		for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
		    WmiManager mgr = null;
			try {
				// Create the WMI Manager
				mgr = new WmiManager(host.getHostAddress(), user,
						pass, domain, matchType);

				// Connect to the WMI server.
				mgr.init();

				// Perform the operation specified in the parameters.
				result = mgr.performOp(params);
                if(params.getWmiOperation().equals(WmiParams.WMI_OPERATION_WQL)) {
                    log().debug(
						"WmiPlugin: "
								+ params.getWql()								
								+ " : "
								+ WmiResult.convertStateToString(result
										.getResultCode()));
                } else {
				    log().debug(
						"WmiPlugin: \\\\"
								+ params.getWmiClass()
								+ "\\"
								+ params.getWmiObject()
								+ " : "
								+ WmiResult.convertStateToString(result
										.getResultCode()));
                }

                isAServer = true;
			} catch (WmiException e) {
				StringBuffer message = new StringBuffer();
				message.append("WmiPlugin: Check failed... : ");
				message.append(e.getMessage());
				message.append(" : ");
				message.append((e.getCause() == null ? "" : e.getCause().getMessage()));
				log().info(message.toString());
				isAServer = false;
			} finally {
			    if (mgr != null) {
			        try {
			            mgr.close();
			        } catch (WmiException e) {
			            log().warn("an error occurred closing the WMI Manager", e);
			        }
			    }
			}
		}
		return result;
	}

    private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}
}
