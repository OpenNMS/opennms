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

package org.opennms.protocols.radius.springsecurity;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
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


import org.opennms.core.utils.InetAddressUtils;
import org.opennms.web.springframework.security.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An org.springframework.security.providers.AuthenticationProvider implementation that provides integration with a Radius server.
 *
 * @author Paul Donohue
 */
public class RadiusAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	
	private static final Logger LOG = LoggerFactory.getLogger(RadiusAuthenticationProvider.class);


    private String server, secret;
    private int port = 1812, timeout = 5, retries = 3;

    /**
     * There is a bug in {@link net.jradius.client.auth.PAPAuthenticator#processRequest(RadiusPacket)} that prevents 
     * instances of the class from being reused. Set the authenticator class to null to work 
     * around this problem (while still using the {@link net.jradius.client.auth.PAPAuthenticator}
     * class).
     * 
     * @see net.jradius.client.RadiusClient#authenticate(AccessRequest, RadiusAuthenticator, int)
     */
    private RadiusAuthenticator authTypeClass = null;

    private String defaultRoles = Authentication.ROLE_USER, rolesAttribute;

    /**
     * Create an instance using the supplied server and shared secret.
     *
     * @param server a {@link java.lang.String} object.
     * @param sharedSecret a {@link java.lang.String} object.
     */
    public RadiusAuthenticationProvider(String server, String sharedSecret) {
        Assert.hasLength(server, "A server must be specified");
        this.server = server;
        Assert.hasLength(sharedSecret, "A shared secret must be specified");
        this.secret = sharedSecret;
    }

    /**
     * <p>doAfterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(this.port, "A port number must be specified");
        Assert.notNull(this.timeout, "A timeout must be specified");
        Assert.notNull(this.retries, "A retry count must be specified");
        //Assert.notNull(this.authTypeClass, "A RadiusAuthenticator object must be supplied in authTypeClass");
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
        if (authTypeClass instanceof PAPAuthenticator) {
            // There is a bug in PAPAuthenticator() that prevents instances of the class
            // from being reused. Set the authenticator class to null to work around this
            // problem.
            this.authTypeClass = null;
        } else {
            this.authTypeClass = authTypeClass;
        }
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
    @Override
    protected UserDetails retrieveUser(String username,
            UsernamePasswordAuthenticationToken token)
            throws AuthenticationException {
        if (!StringUtils.hasLength(username)) {
            LOG.info("Authentication attempted with empty username");
            throw new BadCredentialsException(messages.getMessage("RadiusAuthenticationProvider.emptyUsername",
                "Username cannot be empty"));
        }
        String password = (String) token.getCredentials();
        if (!StringUtils.hasLength(password)) {
            LOG.info("Authentication attempted with empty password");
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        InetAddress serverIP = null;
        serverIP = InetAddressUtils.addr(server);
        if (serverIP == null) {
            LOG.error("Could not resolve radius server address {}", server);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.unknownServer",
                "Could not resolve radius server address"));
        }
        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        AttributeList attributeList = new AttributeList();
        attributeList.add(new Attr_UserName(username));
        attributeList.add(new Attr_UserPassword(password));
        RadiusPacket reply;
        try {
            RadiusClient radiusClient = new RadiusClient(serverIP, secret, port, port+1, timeout);
            AccessRequest request = new AccessRequest(radiusClient, attributeList);

            LOG.debug("Sending AccessRequest message to {}:{} using {} protocol with timeout = {}, retries = {}, attributes:\n{}", InetAddressUtils.str(serverIP), port, (authTypeClass == null ? "PAP" : authTypeClass.getAuthName()), timeout, retries, attributeList.toString());
            reply = radiusClient.authenticate(request, authTypeClass, retries);
        } catch (RadiusException e) {
            LOG.error("Error connecting to radius server {} : {}", server, e);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.radiusError",
                new Object[] {e},
                "Error connecting to radius server: "+e));
        } catch (IOException e) {
            LOG.error("Error connecting to radius server {} : {}", server, e);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.radiusError",
                new Object[] {e},
                "Error connecting to radius server: "+e));
        }
        if (reply == null) {
            LOG.error("Timed out connecting to radius server {}", server);
            throw new AuthenticationServiceException(messages.getMessage("RadiusAuthenticationProvider.radiusTimeout",
                "Timed out connecting to radius server"));
        }
        if (!(reply instanceof AccessAccept)) {
            LOG.info("Received a reply other than AccessAccept from radius server {} for user {} :\n{}", server, username, reply.toString());
            throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        LOG.debug("Received AccessAccept message from {}:{} for user {} with attributes:\n{}", InetAddressUtils.str(serverIP), port, username, reply.getAttributes().toString());

        String roles = null;
        if (!StringUtils.hasLength(rolesAttribute)) {
            LOG.debug("rolesAttribute not set, using default roles ({}) for user {}", defaultRoles, username);
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
                LOG.info("Radius attribute {} not found, using default roles ({}) for user {}", rolesAttribute, defaultRoles, username);
                roles = new String(defaultRoles);
            }
        }

        String[] rolesArray = roles.replaceAll("\\s*","").split(",");
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(rolesArray.length);
        for (String role : rolesArray) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
            StringBuffer readRoles = new StringBuffer();
            for (GrantedAuthority authority : authorities) {
                readRoles.append(authority.toString()+", ");
            }
            if (readRoles.length() > 0) {
                readRoles.delete(readRoles.length()-2, readRoles.length());
            }
            LOG.debug("Parsed roles {} for user {}", readRoles, username);

        return new User(username, password, true, true, true, true, authorities);
    }

}
