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

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

import net.sourceforge.jradiusclient.*;
import net.sourceforge.jradiusclient.exception.*;
import net.sourceforge.jradiusclient.util.*;


/**
 * This Monitor is used to poll hosts supporting Radius Authentication.
 * This is done by sending a radius auth packet to the host.
 * If a valid radius ACCEPT response is received.
 * then the Radius service is considered available.
 *
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 *
 */

final public class RadiusAuthMonitor extends IPv4LatencyMonitor {
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
    public static final int DEFAULT_ACCT_PORT = 1812;

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
     * Class constructor.
     */

    public RadiusAuthMonitor() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Category log = ThreadCategory.getInstance(getClass());
        log.info(getClass().getName() + ": RadiusAuthMonitor class loaded");
    }


    /**
     * Radius Authentication Poller
     * 
     * Note that the poller will return SERVICE_AVAILABLE only if the
     * authentication Request actually succeeds. A failed authentication 
     * request will result in SERVICE_UNAVILABLE, although the radius 
     * server may actually be up. 
     *
     * @param iface
     *            The interface to poll
     * @param parameters
     *            Parameters to pass when polling the interface Currently
     *            recognized Map keys:
     *            <ul>
     *            <li>user - Radius user
     *            <li>password - Radius password
     *		  <li>secret - Radius shared secret
     *            <li>port - Radius auth port
     *            <li>timeout - Number of miliseconds to wait before sending a
     *            timeout
     *            <li>authtype - authentication type to use (pap or chap)
     *            <li>authport - port to poll for radius authentication
     *            <li>acctport - radius accounting port - used by
     *            </ul>
     * @return int An status code that shows the status of the service
     *
     * @see org.opennms.netmgt.poller.ServiceMonitor#SURPRESS_EVENT_MASK
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_AVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNAVAILABLE
     * @see org.opennms.netmgt.poller.ServiceMonitor#SERVICE_UNRESPONSIVE
     *
     */
    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
        Category log = ThreadCategory.getInstance(getClass());

        // Asume that the service is down
        int status = SERVICE_UNAVAILABLE;

        if (iface.getType() != NetworkInterface.TYPE_IPV4) {
            log.error(getClass().getName() + ": Unsupported interface type, only TYPE_IPV4 currently supported");
            throw new NetworkInterfaceNotSupportedException(getClass().getName() + ": Unsupported interface type, only TYPE_IPV4 currently supported");
        }

        if (parameters == null) {
            throw new NullPointerException();
        }

        int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
        int authport = ParameterMap.getKeyedInteger(parameters, "authport", DEFAULT_AUTH_PORT);
        int acctport = ParameterMap.getKeyedInteger(parameters, "acctport", DEFAULT_ACCT_PORT);
        String user = ParameterMap.getKeyedString(parameters, "user", DEFAULT_USER);
        String password = ParameterMap.getKeyedString(parameters, "password", DEFAULT_PASSWORD);
        String secret = ParameterMap.getKeyedString(parameters, "secret", DEFAULT_SECRET);
        String authType = ParameterMap.getKeyedString(parameters, "authtype", DEFAULT_AUTH_TYPE);
        String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name", null);

	InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        if (rrdPath == null) {
            log.info("poll: RRD repository not specified in parameters, latency data will not be stored.");
        }
        if (dsName == null) {
            dsName = DS_NAME;
        }

        RadiusClient rc = null;
        try {
            rc = new RadiusClient(ipv4Addr.getCanonicalHostName(), authport ,acctport, secret, timeout);
        } catch(RadiusException rex) {
            log.error(getClass().getName() + ": Radius Exception: " + rex.getMessage());
            return status;
        } catch(InvalidParameterException ivpex) {
            log.error(getClass().getName() + ": Radius parameter exception: " + ivpex.getMessage());
            return status;
        }


        for (int attempts = 0; attempts <= retry; attempts++) {
            try {
                long responseTime = -1;
                long sentTime = System.currentTimeMillis();
                ChapUtil chapUtil = new ChapUtil();
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,user.getBytes());
		log.debug(getClass().getName() + ": attempting Radius auth with authType: " + authType);
                accessRequest.setAttribute(userNameAttribute);
                if(authType.equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(password, chapChallenge, chapUtil)));
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,password.getBytes()));
                }
                RadiusPacket accessResponse = rc.authenticate(accessRequest);
                if ( accessResponse.getPacketType() == RadiusPacket.ACCESS_ACCEPT ){
                    responseTime = System.currentTimeMillis() - sentTime;
                    status = SERVICE_AVAILABLE;
                    if (log.isDebugEnabled()) {
                        log.debug(getClass().getName() + ": Radius service is AVAILABLE on: " + ipv4Addr.getCanonicalHostName());
                        log.debug("poll: responseTime= " + responseTime + "ms");
                    }
                    // Update response time
                    if (responseTime >= 0 && rrdPath != null) {
                        try {
                            this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                        } catch (RuntimeException rex) {
                            log.debug("There was a problem writing the RRD:" + rex);
                        }
                    }
                    break;
                }
            } catch (InvalidParameterException ivpex){
                log.error(getClass().getName() + ": Invalid Radius Parameter: " + ivpex);
            } catch (RadiusException radex){
                log.error(getClass().getName() + ": Radius Exception : " + radex);
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
