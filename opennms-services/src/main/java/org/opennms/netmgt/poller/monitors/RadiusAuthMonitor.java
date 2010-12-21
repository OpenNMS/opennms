//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.Map;

import net.sourceforge.jradiusclient.RadiusAttribute;
import net.sourceforge.jradiusclient.RadiusAttributeValues;
import net.sourceforge.jradiusclient.RadiusClient;
import net.sourceforge.jradiusclient.RadiusPacket;
import net.sourceforge.jradiusclient.exception.InvalidParameterException;
import net.sourceforge.jradiusclient.exception.RadiusException;
import net.sourceforge.jradiusclient.util.ChapUtil;

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;


/**
 * This Monitor is used to poll hosts supporting Radius Authentication.
 * This is done by sending a radius auth packet to the host.
 * If a valid radius ACCEPT response is received.
 * then the Radius service is considered available.
 *
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */

@Distributable
final public class RadiusAuthMonitor extends IPv4Monitor {
    /**
     * Number of miliseconds to wait before timing out a radius AUTH request
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
     * Class constructor.
     *
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     */
    public RadiusAuthMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        log.info(getClass().getName() + ": RadiusAuthMonitor class loaded");
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
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        ThreadCategory log = ThreadCategory.getInstance(getClass());

        // Asume that the service is down
        PollStatus status = PollStatus.unavailable();

        if (iface.getType() != NetworkInterface.TYPE_INET) {
            log.error(getClass().getName() + ": Unsupported interface type, only TYPE_INET currently supported");
            throw new NetworkInterfaceNotSupportedException(getClass().getName() + ": Unsupported interface type, only TYPE_INET currently supported");
        }

        if (parameters == null) {
            throw new NullPointerException();
        }
        
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        int authport = ParameterMap.getKeyedInteger(parameters, "authport", DEFAULT_AUTH_PORT);
        int acctport = ParameterMap.getKeyedInteger(parameters, "acctport", DEFAULT_ACCT_PORT);
        String user = ParameterMap.getKeyedString(parameters, "user", DEFAULT_USER);
        String password = ParameterMap.getKeyedString(parameters, "password", DEFAULT_PASSWORD);
        String secret = ParameterMap.getKeyedString(parameters, "secret", DEFAULT_SECRET);
        String authType = ParameterMap.getKeyedString(parameters, "authtype", DEFAULT_AUTH_TYPE);
        String nasid = ParameterMap.getKeyedString(parameters, "nasid", DEFAULT_NASID);
	InetAddress ipv4Addr = (InetAddress) iface.getAddress();


        RadiusClient rc = null;
        try {
            rc = new RadiusClient(ipv4Addr.getCanonicalHostName(), authport ,acctport, secret, tracker.getConnectionTimeout());
        } catch(RadiusException rex) {
        	return logDown(Level.ERROR, "Radius Exception: " + rex.getMessage());
        } catch(InvalidParameterException ivpex) {
        	return logDown(Level.ERROR, "Radius parameter exception: " + ivpex.getMessage());
        }
       

        for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
            try {
                tracker.startAttempt();
                
                ChapUtil chapUtil = new ChapUtil();
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                RadiusAttribute nasIdAttribute;
                nasIdAttribute = new RadiusAttribute(RadiusAttributeValues.NAS_IDENTIFIER,nasid.getBytes());
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,user.getBytes());
                log.debug(getClass().getName() + ": attempting Radius auth with authType: " + authType);
                accessRequest.setAttribute(userNameAttribute);
                accessRequest.setAttribute(nasIdAttribute);
                if(authType.equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(password, chapChallenge, chapUtil)));
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,password.getBytes()));
                }
                RadiusPacket accessResponse = rc.authenticate(accessRequest);
                
                if ( accessResponse.getPacketType() == RadiusPacket.ACCESS_ACCEPT ){
                    double responseTime = tracker.elapsedTimeInMillis();
                    status = PollStatus.available(responseTime);
                    if (log.isDebugEnabled()) {
                        log.debug(getClass().getName() + ": Radius service is AVAILABLE on: " + ipv4Addr.getCanonicalHostName());
                        log.debug("poll: responseTime= " + responseTime + "ms");
                    }
                    break;
                }
            } catch (InvalidParameterException ivpex){
            	status = logDown(Level.ERROR, "Invalid Radius Parameter: " + ivpex);
            } catch (RadiusException radex){
            	status = logDown(Level.ERROR, "Radius Exception : " + radex);
	    }
        }
        return status;
    }

    /**
     * Encrypt password using chap challenge
     *
     * @param plainText
     *          plain text password
     * @param chapChallenge
     *          chap challenge
     * @param chapUtil
     *          ref ChapUtil
     *
     * @return encrypted chap password
     */
    private static byte[] chapEncrypt(final String plainText,
                                      final byte[] chapChallenge,
                                      final ChapUtil chapUtil){
        byte chapIdentifier = chapUtil.getNextChapIdentifier();
        byte[] chapPassword = new byte[17];
        chapPassword[0] = chapIdentifier;
        System.arraycopy(ChapUtil.chapEncrypt(chapIdentifier, plainText.getBytes(),chapChallenge),
                         0, chapPassword, 1, 16);
        return chapPassword;
    }

} 
