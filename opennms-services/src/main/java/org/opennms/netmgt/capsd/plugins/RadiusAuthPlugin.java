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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import net.sourceforge.jradiusclient.RadiusAttribute;
import net.sourceforge.jradiusclient.RadiusAttributeValues;
import net.sourceforge.jradiusclient.RadiusClient;
import net.sourceforge.jradiusclient.RadiusPacket;
import net.sourceforge.jradiusclient.exception.InvalidParameterException;
import net.sourceforge.jradiusclient.exception.RadiusException;
import net.sourceforge.jradiusclient.util.ChapUtil;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * This plugin is used to check a host for Radius Authentication support.
 * This is done by sending a radius auth packet to the host.
 * If a valid radius response is received (ACCEPT, REJECT or CHALLENGE)
 * then the host is considered a Radius server.
 *
 * uses the <A HREF="http://jradius.sourceforge.net/">JRadius</A>
 * class library.
 *
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jonathan@opennms.org">Jonathan Sartin</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class RadiusAuthPlugin extends AbstractPlugin {
    /**
     * </P>
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
     * @return True if server, false if not.
     */
    private boolean isRadius(InetAddress host, int authport, int acctport, String authType,
				String user, String password, String secret,
				int retry, int timeout) {

        boolean isRadiusServer = false;
        Category log = ThreadCategory.getInstance(getClass());

        RadiusClient rc = null;

        try {
            rc = new RadiusClient(host.getCanonicalHostName(), authport ,acctport, secret, timeout);
        } catch(RadiusException rex) {
            log.info(getClass().getName() + ": Radius Exception: " + rex.getMessage());
            return isRadiusServer;
        } catch(InvalidParameterException ivpex) {
            log.error(getClass().getName() + ": Radius parameter exception: " + ivpex.getMessage());
            return isRadiusServer;
        }

        for (int attempts = 0; attempts <= retry; attempts++) {
            try {
                ChapUtil chapUtil = new ChapUtil();
                RadiusPacket accessRequest = new RadiusPacket(RadiusPacket.ACCESS_REQUEST);
                RadiusAttribute userNameAttribute;
                userNameAttribute = new RadiusAttribute(RadiusAttributeValues.USER_NAME,user.getBytes());
                accessRequest.setAttribute(userNameAttribute);
                if(authType.equalsIgnoreCase("chap")){
                    byte[] chapChallenge = chapUtil.getNextChapChallenge(16);
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_PASSWORD, chapEncrypt(password, chapChallenge, chapUtil)));
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.CHAP_CHALLENGE, chapChallenge));
                }else{
                    accessRequest.setAttribute(new RadiusAttribute(RadiusAttributeValues.USER_PASSWORD,password.getBytes()));
                }
                RadiusPacket accessResponse = rc.authenticate(accessRequest);
                if ( ( accessResponse.getPacketType() == RadiusPacket.ACCESS_ACCEPT ) |
                     ( accessResponse.getPacketType() == RadiusPacket.ACCESS_CHALLENGE ) |
                     ( accessResponse.getPacketType() == RadiusPacket.ACCESS_REJECT )  ){
		    isRadiusServer = true;
                    if (log.isDebugEnabled()) {
                        log.debug(getClass().getName() + ": Discovered Radius service on: " + host.getCanonicalHostName());
                    }
                    break;
                }
            } catch (InvalidParameterException ivpex){
                log.error(getClass().getName() + ": Invalid Radius Parameter: " + ivpex);
            } catch (RadiusException radex){
                log.info(getClass().getName() + ": Radius Exception : " + radex);
            }
	}

        return isRadiusServer;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    public boolean isProtocolSupported(InetAddress address) {
        return isRadius(address, DEFAULT_AUTH_PORT, DEFAULT_ACCT_PORT, DEFAULT_AUTH_TYPE,
			DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_SECRET,
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
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int authport = DEFAULT_AUTH_PORT;
        int acctport = DEFAULT_ACCT_PORT;
        String authType = DEFAULT_AUTH_TYPE;
        int timeout = DEFAULT_TIMEOUT;
        int retry = DEFAULT_RETRY;
	String user = DEFAULT_USER;
	String password = DEFAULT_PASSWORD;
	String secret = DEFAULT_SECRET;
        if (qualifiers != null) {
            authport = ParameterMap.getKeyedInteger(qualifiers, "authport", DEFAULT_AUTH_PORT);
            acctport = ParameterMap.getKeyedInteger(qualifiers, "acctport", DEFAULT_ACCT_PORT);
            authType = ParameterMap.getKeyedString(qualifiers, "authtype", DEFAULT_AUTH_TYPE);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            retry = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            user = ParameterMap.getKeyedString(qualifiers, "user", DEFAULT_USER);
            password = ParameterMap.getKeyedString(qualifiers, "password", DEFAULT_PASSWORD);
            secret = ParameterMap.getKeyedString(qualifiers, "secret", DEFAULT_SECRET);
        }

        return isRadius(address, authport, acctport, authType, 
			user, password, secret,
			retry, timeout);
    }

    /**
     * Encrypt password using chap challenge
     * 
     * @param plainText 
     *		plain text password
     * @param chapChallenge
     *		chap challenge
     * @param chapUtil
     *		ref ChapUtil
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
