/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.monitor;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.protocols.radius.utils.RadiusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.CHAPAuthenticator;
import net.jradius.client.auth.EAPMD5Authenticator;
import net.jradius.client.auth.EAPMSCHAPv2Authenticator;
import net.jradius.client.auth.EAPTLSAuthenticator;
import net.jradius.client.auth.EAPTTLSAuthenticator;
import net.jradius.client.auth.MSCHAPv1Authenticator;
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_Password;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;


/**
 * This Monitor is used to poll hosts supporting Radius Authentication.
 * This is done by sending a radius auth packet to the host.
 * If a valid radius ACCEPT response is received.
 * then the Radius service is considered available.
 *
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable
public final class RadiusAuthMonitor extends AbstractServiceMonitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(RadiusAuthMonitor.class);

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
     * Default pasword
     */
    public static final String DEFAULT_PASSWORD = "OpenNMS";

    /**
     * Default secret
     */
    public static final String DEFAULT_SECRET = "secret";

    /**
     * Default NAS-ID
     */
    
    public static final String DEFAULT_NASID ="opennms";

    /**
     * Default inner user identity (ttls / peap)
     */
    public static final String DEFAULT_INNER_USER= "Inner-OpenNMS";

    /**
     * Default Inner Auth Type (ttls)
     */
    public static final String DEFAULT_TTLS_INNER_AUTH_TYPE= "pap";

    static {
        RadiusUtils.loadSecurityProvider();
    }

    /**
     * Class constructor.
     *
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     */
    public RadiusAuthMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        LOG.info("RadiusAuthMonitor class loaded");
    }


    /**
     * {@inheritDoc}
     *
     * Radius Authentication Poller
     *
     * Note that the poller will return SERVICE_AVAILABLE only if the
     * authentication Request actually succeeds. A failed authentication
     * request will result in SERVICE_UNAVILABLE, although the radius
     * server may actually be up.
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
    	// Assume that the service is down
        PollStatus status = PollStatus.unavailable();

        if (parameters == null) {
            throw new NullPointerException();
        }
        
        final TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        int authport = ParameterMap.getKeyedInteger(parameters, "authport", DEFAULT_AUTH_PORT);
        int acctport = ParameterMap.getKeyedInteger(parameters, "acctport", DEFAULT_ACCT_PORT);
        String user = ParameterMap.getKeyedString(parameters, "user", DEFAULT_USER);
        String password = ParameterMap.getKeyedString(parameters, "password", DEFAULT_PASSWORD);
        String secret = ParameterMap.getKeyedString(parameters, "secret", DEFAULT_SECRET);
        String authType = ParameterMap.getKeyedString(parameters, "authtype", DEFAULT_AUTH_TYPE);
        String nasid = ParameterMap.getKeyedString(parameters, "nasid", DEFAULT_NASID);
        String innerProtocol = ParameterMap.getKeyedString(parameters, "inner-protocol", DEFAULT_TTLS_INNER_AUTH_TYPE);
        String innerUser = ParameterMap.getKeyedString(parameters, "inner-user", DEFAULT_INNER_USER);
        String certFile = ParameterMap.getKeyedString(parameters, "certificate", null);
        InetAddress addr = svc.getAddress();

        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        int timeout = convertTimeoutToSeconds(ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT));
        try {
            final RadiusClient rc = new RadiusClient(addr, secret, authport, acctport, timeout);

            for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
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
                } else if (authType.equalsIgnoreCase("eapmd5") || authType.equalsIgnoreCase("eap-md5")) {
                    auth = new EAPMD5Authenticator();
                } else if (authType.equalsIgnoreCase("eapmschapv2") || authType.equalsIgnoreCase("eap-mschapv2")) {
                    auth = new EAPMSCHAPv2Authenticator();
                } else if (RadiusUtils.isTunneling(authType)) { 
                    if (innerUser == null){
                        String reason = "TLS AAA type requested but no inner user defined. Authtype: '" + authType + "'";
                        RadiusAuthMonitor.LOG.debug(reason);
                        return PollStatus.unavailable(reason);
                    }
                    EAPTLSAuthenticator tlsAuth = null;
                    if (RadiusUtils.isEAPTTLS(authType)){
                        tlsAuth = new EAPTTLSAuthenticator();
                        final EAPTTLSAuthenticator ttlsAuth = (EAPTTLSAuthenticator) tlsAuth;
                        if (innerProtocol != DEFAULT_TTLS_INNER_AUTH_TYPE){
                            String reason = "RadiusMonitor can only use 'pap' as inner auth protocol, not " + innerProtocol;
                            LOG.debug(reason);
                            return PollStatus.unavailable(reason);
                        } else {
                            ttlsAuth.setInnerProtocol(innerProtocol);
                        }
                        AttributeList attrs = new AttributeList();
                        attrs.add(new Attr_UserName(innerUser));
                        attrs.add(new Attr_Password(password));
                        ttlsAuth.setTunneledAttributes(attrs);
                    } else if (authType.equalsIgnoreCase("peap")){
                        String reason = "Support for eap peap is not ready yet";
                        LOG.debug(reason);
                        return PollStatus.unavailable(reason);
                    }
                    /* Cert. processing is common to EAPTLS protocols */
                    /* We trust any certificate for now */
                    LOG.warn("Server certificate will be trusted");
                    if (certFile==null) tlsAuth.setTrustAll(true);
                    auth = tlsAuth;
                } else{
                    String reason = "Unknown authenticator type '" + authType + "'";
                    RadiusAuthMonitor.LOG.debug(reason);
                    return PollStatus.unavailable(reason);
                }

                tracker.startAttempt();

                // The retry should be handled by the RadiusClient because otherwise it will thrown an exception.
                RadiusPacket reply = rc.authenticate(accessRequest, auth, ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY));
                if (reply instanceof AccessAccept) {
                    double responseTime = tracker.elapsedTimeInMillis();
                    status = PollStatus.available(responseTime);
                    LOG.debug("Radius service is AVAILABLE on: {}", addr.getCanonicalHostName());
                    LOG.debug("poll: responseTime= {}", responseTime);
                    break;
                } else if (reply != null) {
                    LOG.debug("response returned, but request was not accepted: {}", reply);
                }
                String reason = "Invalid RADIUS reply: " + reply;
                RadiusAuthMonitor.LOG.debug(reason);
                status = PollStatus.unavailable(reason);
            }
        } catch (final Throwable e) {
            String reason = "Error while attempting to connect to the RADIUS service on " + addr.getCanonicalHostName();
            RadiusAuthMonitor.LOG.debug(reason, e);
            status = PollStatus.unavailable(reason);
        }

        return status;
    }

	private int convertTimeoutToSeconds(int timeout) {
		return timeout/1000 > 0 ? timeout/1000 : 1;
	}

} 
