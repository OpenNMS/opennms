/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/************************************************************************
 * Change history
 *
 * 2013-04-18 Updated package names to match new XMP JAR (jeffg@opennms.org)
 *
 ************************************************************************/

package org.opennms.netmgt.protocols.xmp.capsd;

import java.net.InetAddress;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.krupczak.xmp.SocketOpts;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpSession;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;
import org.opennms.netmgt.protocols.xmp.XmpUtil;
import org.opennms.netmgt.protocols.xmp.XmpUtilException;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existence of an XMP (XML Management Protocol) daemon on remote interfaces,
 * optionally also checking for the presence and value of specified MIB objects.
 * The class implements the Plugin interface that allows it to be used along
 * with other plugins by the daemon.
 * </P>
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @version $Id: $
 */
public final class XmpPlugin extends AbstractPlugin {

    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "XMP";
    
    /**
     * The default port to use for XMP
     */
    private final static int DEFAULT_PORT = Xmp.XMP_PORT;
    
    
    /**
     * Default number of retries for TCP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for XMP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds
    
    /**
     * Default XMP user for performing requests
     */
    private final static String DEFAULT_AUTHEN_USER = "xmpUser";

    /**
     * Default type of request to perform
     */
    private final static String DEFAULT_REQUEST_TYPE = "GetRequest";
    
    /**
     * Default MIB from which to make request
     */
    private final static String DEFAULT_REQUEST_MIB = "core";
    
    /**
     * Default table from which to make request
     */
    private final static String DEFAULT_REQUEST_TABLE = "";
    
    /**
     * Default object name to request
     */
    private final static String DEFAULT_REQUEST_OBJECT = "sysObjectID";
    
    /**
     * Default instance to request (for SelectTableRequest only)
     */
    private final static String DEFAULT_REQUEST_INSTANCE = "*";
    
    /**
     * Default string against which to match the returned value(s)
     */
    private final static String DEFAULT_VALUE_MATCH = null;
    
    /**
     * Default string against which to match the returned instance(s)
     */
    private final static String DEFAULT_INSTANCE_MATCH = null;
    
    /**
     * Default integer denoting minimum number of
     * matches allowed
     */
    private final static int DEFAULT_MIN_MATCHES = 1;
    
    /**
     * Default integer denoting maximum number of
     * matches allowed.
     */
    private final static int DEFAULT_MAX_MATCHES = 1;
    
    /**
     * Default boolean indicating whether maximum number
     * of matches is actually unbounded
     */
    private final static boolean DEFAULT_MAX_MATCHES_UNBOUNDED = true;

    private static final boolean DEFAULT_VALUE_CASE_SENSITIVE = false;

    private static final String DEFAULT_VALUE_OPERAND = XmpUtil.EQUALS;


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
        throw new UnsupportedOperationException("Undirected XMP checking not supported");
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
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        XmpConfig protoConfig = XmpConfigFactory.getInstance().getXmpConfig();
        XmpSession session;
        SocketOpts sockopts = new SocketOpts();
        // TODO how to apply timeout and retry to XMP operations?
        int retry = protoConfig.hasRetry() ? protoConfig.getRetry() : DEFAULT_RETRY;
        int timeout = protoConfig.hasTimeout() ? protoConfig.getTimeout() : DEFAULT_TIMEOUT;
        int port = DEFAULT_PORT;
        String authenUser = DEFAULT_AUTHEN_USER;
        String requestType = DEFAULT_REQUEST_TYPE;
        String mib = DEFAULT_REQUEST_MIB;
        String table = DEFAULT_REQUEST_TABLE;
        String object = DEFAULT_REQUEST_OBJECT;
        String instance = DEFAULT_REQUEST_INSTANCE;
        String instanceMatch = null;
        String valueOperator = XmpUtil.EQUALS;
        String valueOperand = DEFAULT_VALUE_OPERAND;
        int minMatches = DEFAULT_MIN_MATCHES;
        int maxMatches = DEFAULT_MAX_MATCHES;
        boolean maxMatchesUnbounded = DEFAULT_MAX_MATCHES_UNBOUNDED;
        boolean valueCaseSensitive = DEFAULT_VALUE_CASE_SENSITIVE;

        if (qualifiers != null) {
            retry = ParameterMap.getKeyedInteger(qualifiers, "retry", protoConfig.hasRetry() ? protoConfig.getRetry() : DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", protoConfig.hasTimeout() ? protoConfig.getTimeout() : DEFAULT_TIMEOUT);
            port = ParameterMap.getKeyedInteger(qualifiers, "port", DEFAULT_PORT);
            authenUser = ParameterMap.getKeyedString(qualifiers, "authenUser", DEFAULT_AUTHEN_USER);
            requestType = ParameterMap.getKeyedString(qualifiers, "request-type", DEFAULT_REQUEST_TYPE);
            mib = ParameterMap.getKeyedString(qualifiers, "mib", DEFAULT_REQUEST_MIB);
            table = ParameterMap.getKeyedString(qualifiers, "table", DEFAULT_REQUEST_TABLE);
            object = ParameterMap.getKeyedString(qualifiers, "object", DEFAULT_REQUEST_OBJECT);
            instance = ParameterMap.getKeyedString(qualifiers, "instance", DEFAULT_REQUEST_INSTANCE);
            instanceMatch = ParameterMap.getKeyedString(qualifiers, "instance-match", DEFAULT_INSTANCE_MATCH);
            valueOperator = ParameterMap.getKeyedString(qualifiers, "value-operator", "==");
            valueOperand = ParameterMap.getKeyedString(qualifiers, "value-match", DEFAULT_VALUE_MATCH);
            valueCaseSensitive = ParameterMap.getKeyedBoolean(qualifiers, "value-case-sensitive", DEFAULT_VALUE_CASE_SENSITIVE);
            minMatches = ParameterMap.getKeyedInteger(qualifiers, "min-matches", DEFAULT_MIN_MATCHES);
            maxMatches = ParameterMap.getKeyedInteger(qualifiers, "max-matches", DEFAULT_MAX_MATCHES);
            String maxMatchesUnboundedStr = ParameterMap.getKeyedString(qualifiers, "max-matches", "unbounded");
            maxMatchesUnbounded = (maxMatchesUnboundedStr.equalsIgnoreCase("unbounded"));
        }
        
        // Set the SO_TIMEOUT so that this thing has a prayer of working over a WAN
        sockopts.setConnectTimeout(timeout);
        
        // If this is a SelectTableRequest, then you can't use the defaults
        // for Table and Object.
        if (requestType.equalsIgnoreCase("SelectTableRequest")) {
            if (table.equals(DEFAULT_REQUEST_TABLE)) {
                throw new IllegalArgumentException("When performing a SelectTableRequest, table must be specified");
            }
            if (object.equals(DEFAULT_REQUEST_OBJECT)) {
                throw new IllegalArgumentException("When performing a SelectTableRequest, object must be specified and must be tabular");
            }
        }
        
        // If this is a GetRequest, then you can't specify a table or
        // an instance
        else if (requestType.equalsIgnoreCase("GetRequest")) {
            if (! table.equals(DEFAULT_REQUEST_TABLE)) {
                throw new IllegalArgumentException("When performing a GetRequest, table must not be specified");
            }
            if (! instance.equals(DEFAULT_REQUEST_INSTANCE)) {
                throw new IllegalArgumentException("When performing a GetRequest, instance must not be specified");
            }
        } else {
            throw new IllegalArgumentException("Unknown request type " + requestType + ", only GetRequest and SelectTableRequest are supported");
        }
        
        RE instanceRegex = null;
        try {
            if (instanceMatch == null) {
                instanceRegex = null;
            } else if (instanceMatch != null) {
                instanceRegex = new RE(instanceMatch);                
            }
        } catch (RESyntaxException e) {
            throw new java.lang.reflect.UndeclaredThrowableException(e);
        }

        boolean result = false;
        session = new XmpSession(sockopts, address, port, authenUser);
        /*
        if (session == null) {
            log.info("XMP connection failed to " + address + ":" + port + " with user " + authenUser + " and " + sockopts);
            return false;
        }
        */
        if (requestType.equalsIgnoreCase("SelectTableRequest")) {
            try {
                result = XmpUtil.handleTableQuery(session, mib, table, object, instance, instanceRegex, valueOperator, valueOperand, minMatches, maxMatches, maxMatchesUnbounded, log, valueCaseSensitive);
            } catch (XmpUtilException e) {
                result = false;
            }
        } else if (requestType.equalsIgnoreCase("GetRequest")) {
            try {
                result = XmpUtil.handleScalarQuery(session, mib, object, valueOperator, valueOperand, log, valueCaseSensitive);
            } catch (XmpUtilException e) {
                result = false;
            }
        }        
        return result;
    }
}
