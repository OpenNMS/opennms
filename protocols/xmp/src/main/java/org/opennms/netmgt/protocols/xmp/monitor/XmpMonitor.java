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

package org.opennms.netmgt.protocols.xmp.monitor;

import java.net.InetAddress;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.krupczak.xmp.SocketOpts;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpSession;
import org.opennms.core.utils.ParameterMap;

import org.opennms.netmgt.config.xmpConfig.XmpConfig;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.opennms.netmgt.protocols.xmp.XmpUtil;
import org.opennms.netmgt.protocols.xmp.XmpUtilException;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>XmpMonitor class.</p>
 *
 * @author jeffg
 * @version $Id: $
 */
public class XmpMonitor extends AbstractServiceMonitor {
	

    
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


    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String,Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();
        
        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();

        
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
        String valueOperand = null;
        int minMatches = DEFAULT_MIN_MATCHES;
        int maxMatches = DEFAULT_MAX_MATCHES;
        boolean maxMatchesUnbounded = DEFAULT_MAX_MATCHES_UNBOUNDED;
        boolean valueCaseSensitive = DEFAULT_VALUE_CASE_SENSITIVE;

        if (parameters != null) {
            retry = ParameterMap.getKeyedInteger(parameters, "retry", protoConfig.hasRetry() ? protoConfig.getRetry() : DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(parameters, "timeout", protoConfig.hasTimeout() ? protoConfig.getTimeout() : DEFAULT_TIMEOUT);
            port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
            authenUser = ParameterMap.getKeyedString(parameters, "authenUser", DEFAULT_AUTHEN_USER);
            requestType = ParameterMap.getKeyedString(parameters, "request-type", DEFAULT_REQUEST_TYPE);
            mib = ParameterMap.getKeyedString(parameters, "mib", DEFAULT_REQUEST_MIB);
            table = ParameterMap.getKeyedString(parameters, "table", DEFAULT_REQUEST_TABLE);
            object = ParameterMap.getKeyedString(parameters, "object", DEFAULT_REQUEST_OBJECT);
            instance = ParameterMap.getKeyedString(parameters, "instance", DEFAULT_REQUEST_INSTANCE);
            instanceMatch = ParameterMap.getKeyedString(parameters, "instance-match", DEFAULT_INSTANCE_MATCH);
            valueOperator = ParameterMap.getKeyedString(parameters, "value-operator", "==");
            valueOperand = ParameterMap.getKeyedString(parameters, "value-match", DEFAULT_VALUE_MATCH);
            valueCaseSensitive = ParameterMap.getKeyedBoolean(parameters, "value-case-sensitive", DEFAULT_VALUE_CASE_SENSITIVE);
            minMatches = ParameterMap.getKeyedInteger(parameters, "min-matches", DEFAULT_MIN_MATCHES);
            maxMatches = ParameterMap.getKeyedInteger(parameters, "max-matches", DEFAULT_MAX_MATCHES);
            String maxMatchesUnboundedStr = ParameterMap.getKeyedString(parameters, "max-matches", "unbounded");
            maxMatchesUnbounded = (maxMatchesUnboundedStr.equalsIgnoreCase("unbounded"));
        }

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
        
        long startTime = System.currentTimeMillis();
        
        // Set the SO_TIMEOUT.  What a concept!
        sockopts.setConnectTimeout(timeout);
        
        session = new XmpSession(sockopts, ipaddr, port, authenUser);

        boolean result = false;
        if (requestType.equalsIgnoreCase("SelectTableRequest")) {
            try {
                result = XmpUtil.handleTableQuery(session, mib, table, object, instance, instanceRegex, valueOperator, valueOperand, minMatches, maxMatches, maxMatchesUnbounded, valueCaseSensitive);
            } catch (XmpUtilException e) {
                status = PollStatus.unavailable(e.getMessage());
            }
        } else if (requestType.equalsIgnoreCase("GetRequest")) {
            try {
                result = XmpUtil.handleScalarQuery(session, mib, object, valueOperator, valueOperand, valueCaseSensitive);
            } catch (XmpUtilException e) {
                status = PollStatus.unavailable(e.getMessage());
            }
        }
        if (result == true) {
            Double responseTime = new Double(System.currentTimeMillis() - startTime);
            status = PollStatus.available(responseTime);
        }
        return status;
    }

}
