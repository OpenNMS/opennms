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

package org.opennms.protocols.radius.capsd;

import java.net.InetAddress;
import java.util.Map;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin is used to check a host for Radius Authentication support.
 * This is done by sending a radius auth packet to the host.
 * If a valid radius response is received (ACCEPT, REJECT or CHALLENGE)
 * then the host is considered a Radius server.
 *
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class RadiusAuthPlugin extends AbstractPlugin {
	
	private static final Logger LOG = LoggerFactory.getLogger(RadiusAuthPlugin.class);

    /**
     * <P>
     * The protocol name that is tested by this plugin.
     * </P>
     */
    private final static String PROTOCOL_NAME = "RadiusAuth";

    /**
     * Number of milliseconds to wait before timing out a radius AUTH request
     */
    public static final int DEFAULT_TIMEOUT = 5000;

    /**
     * Default number of times to retry a test
     */
    public static final int DEFAULT_RETRY = 0;

    /**
     * Default radius authentication port
     */
    public static final int DEFAULT_AUTH_PORT = 1812;

    /**
     * Default radius accounting port
     */
    public static final int DEFAULT_ACCT_PORT = 1813;

    /**
     * Default radius authentication type
     */
    public static final String DEFAULT_AUTH_TYPE = "pap";

    /**
     * Default user
     */
    public static final String DEFAULT_USER = "OpenNMS";

    /**
     * Default password
     */
    public static final String DEFAULT_PASSWORD = "OpenNMS";

    /**
     * Default secret
     */
    public static final String DEFAULT_SECRET = "secret";

    /**
     * 
     * Default NAS_ID
     */
    public static final String DEFAULT_NAS_ID = "opennms";
    /**
     * 
     * @param host
     *            The address for the radius server test.
     * @param authport
     *            Radius authentication port
     * @param acctport
     *            Radius accounting port - required by jradius
     *            but not explicitly checked
     * @param authType
     *            authentication type - pap or chap
     * @param user
     *            user for Radius authentication
     * @param password
     *            password for Radius authentication
     * @param secret
     *            Radius shared secret
     * @param timeout
     *            Timeout in milliseconds
     * @param retry 
     *		  Number of times to retry 
     *
     * @param nasid
     *            NAS Identifier to use
     *
     * @return True if server, false if not.
     */
    private boolean isRadius(final InetAddress host, final int authport, final int acctport, final String authType,
            final String user, final String password, final String secret, final String nasid, final int retry, final int timeout) {

        boolean isRadiusServer = false;

        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        try {
            final RadiusClient rc = new RadiusClient(host, secret, authport, acctport, convertTimeoutToSeconds(timeout));

            final AttributeList attributes = new AttributeList();
            attributes.add(new Attr_UserName(user));
            attributes.add(new Attr_NASIdentifier(nasid));
            attributes.add(new Attr_UserPassword(password));

            final AccessRequest accessRequest = new AccessRequest(rc, attributes);
            final RadiusAuthenticator auth;
            if (authType.equalsIgnoreCase("chap")) {
                auth = new CHAPAuthenticator();
            } else if (authType.equalsIgnoreCase("pap")) {
                auth = new PAPAuthenticator();
            } else if (authType.equalsIgnoreCase("mschapv1")) {
                auth = new MSCHAPv1Authenticator();
            } else if (authType.equalsIgnoreCase("mschapv2")) {
                auth = new MSCHAPv2Authenticator();
            } else if (authType.equalsIgnoreCase("eapmd5")) {
                auth = new EAPMD5Authenticator();
            } else if (authType.equalsIgnoreCase("eapmschapv2")) {
                auth = new EAPMSCHAPv2Authenticator();
            } else {
                LOG.warn("Unknown authenticator type '{}'", authType);
                return isRadiusServer;
            }

            RadiusPacket reply = rc.authenticate(accessRequest, auth, retry);
            isRadiusServer = reply instanceof AccessAccept;
            LOG.debug("Discovered RADIUS service on {}", host.getCanonicalHostName());
        } catch (final Throwable e) {
            LOG.debug("Error while attempting to discover RADIUS service on {}", host.getCanonicalHostName(), e);
            isRadiusServer = false;
        }

        return isRadiusServer;
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
        return isRadius(address, DEFAULT_AUTH_PORT, DEFAULT_ACCT_PORT, DEFAULT_AUTH_TYPE,
			DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_SECRET, DEFAULT_NAS_ID,
			DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * </p>
     *
     * <p>
     * In addition, the input qualifiers map also provides information about how
     * the plugin should contact the remote server. The plugin may check the
     * qualifier map for specific elements and then adjust its behavior as
     * necessary
     * </p>
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int authport = DEFAULT_AUTH_PORT;
        int acctport = DEFAULT_ACCT_PORT;
        String authType = DEFAULT_AUTH_TYPE;
        int timeout = DEFAULT_TIMEOUT;
        int retry = DEFAULT_RETRY;
        String user = DEFAULT_USER;
        String password = DEFAULT_PASSWORD;
        String secret = DEFAULT_SECRET;
        String nasid = DEFAULT_NAS_ID;
        if (qualifiers != null) {
            authport = ParameterMap.getKeyedInteger(qualifiers, "authport", DEFAULT_AUTH_PORT);
            acctport = ParameterMap.getKeyedInteger(qualifiers, "acctport", DEFAULT_ACCT_PORT);
            authType = ParameterMap.getKeyedString(qualifiers, "authtype", DEFAULT_AUTH_TYPE);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            retry = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            user = ParameterMap.getKeyedString(qualifiers, "user", DEFAULT_USER);
            password = ParameterMap.getKeyedString(qualifiers, "password", DEFAULT_PASSWORD);
            secret = ParameterMap.getKeyedString(qualifiers, "secret", DEFAULT_SECRET);
            nasid = ParameterMap.getKeyedString(qualifiers, "nasid", DEFAULT_NAS_ID);
        }

        return isRadius(address, authport, acctport, authType, user, password, secret, nasid, retry, timeout);
    }

    private int convertTimeoutToSeconds(int timeout) {
        return timeout/1000 > 0 ? timeout/1000 : 1;
    }
}
