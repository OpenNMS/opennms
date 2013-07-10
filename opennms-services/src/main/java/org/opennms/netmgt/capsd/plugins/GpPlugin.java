/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of a generic service by calling an external script or program. The
 * external script or program will be passed two options: --hostname, the IP
 * address of the host to be tested, and --timeout, the timeout in seconds.
 * Additional options or arguments can be specified in the capsd configuration.
 * </P>
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="mailto:weave@oculan.com">Weaver</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 * @author <a href="mailto:ayres@net.orst.edu">Bill Ayres</a>
 */
public final class GpPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(GpPlugin.class);
    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "GP";

    /**
     * Default number of retries for GP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for GP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * <P>
     * Test to see if the passed script-host-argument combination is the
     * endpoint for a GP server. If there is a GP server at that destination
     * then a value of true is returned from the method. Otherwise a false value
     * is returned to the caller. In order to return true the script must
     * generate a banner line which contains the text from the banner or match
     * argument.
     * </P>
     * 
     * @param host
     *            The host to pass to the script
     * @param retry
     *            The number of retry attempts to make
     * @param timeout
     *            The timeout value for each retry
     * @param script
     *            The external script or program to call
     * @param args
     *            The arguments to pass to the script
     * @param regex
     *            The regular expression used to determine banner match
     * @param bannerResult
     * @param hoption
     *            The option string passed to the exec for the IP address (hostname)
     * @param toption
     *            The option string passed to the exec for the timeout
     * 
     * @return True if a connection is established with the script and the
     *         banner line returned by the script matches the regular expression
     *         regex.
     */
    private boolean isServer(InetAddress host, int retry, int timeout, String script, String args, RE regex, StringBuffer bannerResult, String hoption, String toption) {

        boolean isAServer = false;

        LOG.debug("poll: address = {}, script = {}, arguments = {}, timeout(seconds) = {}, retry = {}", retry, InetAddressUtils.str(host), script, args, timeout);

        for (int attempts = 0; attempts <= retry && !isAServer; attempts++) {
            try {
                int exitStatus = 100;
                ExecRunner er = new ExecRunner();
                er.setMaxRunTimeSecs(timeout);
                if (args == null)
                    exitStatus = er.exec(script + " " + hoption + " " + InetAddressUtils.str(host) + " " + toption + " " + timeout);
                else
                    exitStatus = er.exec(script + " " + hoption + " " + InetAddressUtils.str(host) + " " + toption + " " + timeout + " " + args);
                if (exitStatus != 0) {
                    LOG.debug("{} failed with exit code {}", script, exitStatus);
                    isAServer = false;
                }
                if (er.isMaxRunTimeExceeded()) {
                    LOG.debug("{} failed. Timeout exceeded", script);
                    isAServer = false;
                } else {
                    if (exitStatus == 0) {
                        String response = "";
                        String error = "";
                        response = er.getOutString();
                        error = er.getErrString();
                        if (response.equals(""))
                            LOG.debug("{} returned no output", script);
                        if (!error.equals(""))
                            LOG.debug("{} error = {}", script, error);
                        if (regex == null || regex.match(response)) {

                            LOG.debug("isServer: matching response = {}", response);
                            isAServer = true;
                            if (bannerResult != null)
                                bannerResult.append(response);
                        } else {
                            isAServer = false;

                            LOG.debug("isServer: NON-matching response = {}", response);
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                isAServer = false;
                e.fillInStackTrace();
                LOG.debug("{} ArrayIndexOutOfBoundsException", script);
            } catch (InterruptedIOException e) {
                // This is an expected exception
                //
                isAServer = false;
            } catch (IOException e) {
                isAServer = false;
                e.fillInStackTrace();
                LOG.debug("IOException occurred. Check for proper operation of {}", script);
            } catch (Throwable e) {
                isAServer = false;
                e.fillInStackTrace();
                LOG.debug("{} Exception occurred", script);
            }
        }

        //
        // return the status of the server
        //
        LOG.debug("poll: GP - isAServer = {} {}", InetAddressUtils.str(host), isAServer);
        return isAServer;
    }

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
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        throw new UnsupportedOperationException("Undirected GP checking not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int retry = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;
        String banner = null;
        String match = null;
        String script = null;
        String args = null;
        String hoption = "--hostname";
        String toption = "--timeout";
        if (qualifiers != null) {
            retry = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            script = ParameterMap.getKeyedString(qualifiers, "script", null);
            args = ParameterMap.getKeyedString(qualifiers, "args", null);
            banner = ParameterMap.getKeyedString(qualifiers, "banner", null);
            match = ParameterMap.getKeyedString(qualifiers, "match", null);
	    hoption = ParameterMap.getKeyedString(qualifiers, "hoption", "--hostname");
	    toption = ParameterMap.getKeyedString(qualifiers, "toption", "--timeout");
        }
        if (script == null) {
            throw new RuntimeException("GpPlugin: required parameter 'script' is not present in supplied properties.");
        }

        //
        // convert timeout to seconds for ExecRunner
        //
        if (0 < timeout && timeout < 1000)
            timeout = 1;
        else
            timeout = timeout / 1000;

        try {
            StringBuffer bannerResult = null;
            RE regex = null;
            if (match == null && (banner == null || banner.equals("*"))) {
                regex = null;
            } else if (match != null) {
                regex = new RE(match);
                bannerResult = new StringBuffer();
            } else if (banner != null) {
                regex = new RE(banner);
                bannerResult = new StringBuffer();
            }

            boolean result = isServer(address, retry, timeout, script, args, regex, bannerResult, hoption, toption);
            if (result && qualifiers != null) {
                if (bannerResult != null && bannerResult.length() > 0)
                    qualifiers.put("banner", bannerResult.toString());
            }

            return result;
        } catch (RESyntaxException e) {
            throw new java.lang.reflect.UndeclaredThrowableException(e);
        }
    }
}
