//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Sep 20: Don't use Java 6 String.isEmpty(). - dj@opennms.org
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
package org.opennms.web.springframework.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.PAPAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.exception.RadiusException;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An org.springframework.security.providers.AuthenticationProvider implementation that provides integration with a Radius server.
 *
 * @author Paul Donohue
 * @version $Id: $
 */
public class RadiusAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final Log logger = LogFactory.getLog(RadiusAuthenticationProvider.class);
    private String server, secret;
    private int port = 1812, timeout = 5, retries = 3;
    private RadiusAuthenticator authTypeClass = new PAPAuthenticator();
    private String defaultRoles = "ROLE_USER", rolesAttribute;

    /**
     * Create an instance using the supplied server and shared secret.
     *
     * @param server a {@link java.lang.String} object.
     * @param secret a {@link java.lang.String} object.
     */
    public RadiusAuthenticationProvider(String server, String secret) {
        Assert.hasLength(server, "A server must be specified");
        this.server = server;
        Assert.hasLength(server, "A shared secret must be specified");
        this.secret = secret;
    }

    /**
     * <p>doAfterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(this.port, "A port number must be specified");
        Assert.notNull(this.timeout, "A timeout must be specified");
        Assert.notNull(this.retries, "A retry count must be specified");
        Assert.notNull(this.authTypeClass, "A RadiusAuthenticator object must be supplied in authTypeClass");
        Assert.notNull(this.defaultRoles, "Default Roles must be supplied in defaultRoles");
    }

    /**
     * Sets the port number the radius server is listening on
     *
     * @param port (defaults to 1812)
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the authentication timeout (in seconds)
     *
     * @param timeout (defaults to 5)
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Sets the number of times to retry a timed-out authentication request
     *
     * @param retries (defaults to 3)
     */
    public void setRetries(int retries) {
        this.retries = retries;
    }

    /**
     * Sets the authenticator, which determines the authentication type (PAP, CHAP, etc)
     *
     * @param authTypeClass An instance of net.jradius.client.auth.RadiusAuthenticator (defaults to PAPAuthenticator)
     */
    public void setAuthTypeClass(RadiusAuthenticator authTypeClass) {
        this.authTypeClass = authTypeClass;
    }

    /**
     * Sets the default authorities (roles) that should be assigned to authenticated users
     *
     * @param defaultRoles comma-separated list of roles (defaults to "ROLE_USER")
     */
    public void setDefaultRoles(String defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    /**
     * Sets the name of a radius attribute to be returned by the radius server
     * with a comma-separated list of authorities (roles) to be assigned to the user
     *
     * If this is not set, or if the specified attribute is not found in the reply
     * from the radius server, defaultRoles will be used to assign roles
     *
     * If JRadius's built-in attribute dictionary does not contain the desired
     * attribute name, use "Unknown-VSAttribute(<Vendor ID>:<Attribute Number>)"
     *
     * @param rolesAttribute a {@link java.lang.String} object.
     */
    public void setRolesAttribute(String rolesAttribute) {
        this.rolesAttribute = rolesAttribute;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider#additionalAuthenticationChecks(org.springframework.security.userdetails.UserDetails, org.springframework.security.providers.UsernamePasswordAuthenticationToken)
     */
    /** {@inheritDoc} */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken token)
            throws AuthenticationException {
        if (!userDetails.getPassword().equals(token.getCredentials().toString())) {
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"),
                userDetails);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider#retrieveUser(java.lang.String, org.springframework.security.providers.UsernamePasswordAuthenticationToken)
     */
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    protected UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken token)
            throws AuthenticationException {
        if (!StringUtils.hasLength(username)) {
            logger.info("Authentication attempted with empty username");
            throw new BadCredentialsException(messages.getMessage("RadiusAuthenticationProvider.emptyUsername",
                "Username cannot be empty"));
        }
        String password = (String) token.getCredentials();
        if (!StringUtils.hasLength(password)) {
            logger.info("Authentication attempted with empty password");
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        InetAddress serverIP = null;
        try {
            serverIP = InetAddress.getByName(server);
        } catch (UnknownHostException e) {
            logger.error("Could not resolve radius server address "+server+" : "+e);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.unknownServer",
                "Could not resolve radius server address"));
        }
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        AttributeList attributeList = new AttributeList();
        attributeList.add(new Attr_UserName(username));
        attributeList.add(new Attr_UserPassword(password));
        RadiusClient radiusClient = new RadiusClient(serverIP, secret, port, port+1, timeout);
        AccessRequest request = new AccessRequest(radiusClient, attributeList);

        RadiusPacket reply;
        try {
            logger.debug("Sending AccessRequest message to "+serverIP.getHostAddress()+":"+port+" using "+authTypeClass.getAuthName()+" protocol with timeout = "+timeout+", retries = "+retries+", attributes:\n"+attributeList.toString());
            reply = radiusClient.authenticate(request, authTypeClass, retries);
        } catch (RadiusException e) {
            logger.error("Error connecting to radius server "+server+" : "+e);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.radiusError",
                new Object[] {e},
                "Error connecting to radius server: "+e));
        }
        if (reply == null) {
            logger.error("Timed out connecting to radius server "+server);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.radiusTimeout",
                "Timed out connecting to radius server"));
        }
        if (!(reply instanceof AccessAccept)) {
            logger.info("Received a reply other than AccessAccept from radius server "+server+" for user "+username+" :\n"+reply.toString());
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        logger.debug("Received AccessAccept message from "+serverIP.getHostAddress()+":"+port+" for user "+username+" with attributes:\n"+reply.getAttributes().toString());

        String roles = null;
        if (!StringUtils.hasLength(rolesAttribute)) {
            logger.debug("rolesAttribute not set, using default roles ("+defaultRoles+") for user "+username);
            roles = new String(defaultRoles);
        } else {
            Iterator<RadiusAttribute> attributes = reply.getAttributes().getAttributeList().iterator();
            while (attributes.hasNext()) {
                RadiusAttribute attribute = attributes.next();
                if (rolesAttribute.equals(attribute.getAttributeName())) {
                    roles = new String(attribute.getValue().getBytes());
                    break;
                }
            }
            if (roles == null) {
                logger.info("Radius attribute "+rolesAttribute+" not found, using default roles ("+defaultRoles+") for user "+username);
                roles = new String(defaultRoles);
            }
        }

        String[] rolesArray = roles.replaceAll("\\s*","").split(",");
        GrantedAuthority[] authorities = new GrantedAuthority[rolesArray.length];
        for (int i = 0; i < rolesArray.length; i++) {
            authorities[i] = new GrantedAuthorityImpl(rolesArray[i]);
        }
        if(logger.isDebugEnabled()) {
            StringBuffer readRoles = new StringBuffer();
            for (GrantedAuthority authority : authorities) {
                readRoles.append(authority.toString()+", ");
            }
            if (readRoles.length() > 0) {
                readRoles.delete(readRoles.length()-2, readRoles.length());
            }
            logger.debug("Parsed roles "+readRoles+" for user "+username);
        }

        return new User(username, password, true, true, true, true, authorities);
    }

}
