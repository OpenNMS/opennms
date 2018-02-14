/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/


package org.opennms.netmgt.protocols.xmp.detector;

import java.net.InetAddress;
import java.util.Date;

import org.krupczak.xmp.SocketOpts;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpMessage;
import org.krupczak.xmp.XmpSession;
import org.krupczak.xmp.XmpVar;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenNMS XMP Detector allows for discovery of service/protocols via
 * provisiond.  Our detector is pretty simple right now.  All it does is
 * attempt to establish an XmpSession with a system and if it succeeds,
 * it queries a few core MIB variables and returns success.
 * Future enhancements could include more capability information like
 * supported MIBs, modules/plugins, and various dependencies to
 * determine if something is a server for some service (noted by an
 * inbound dependency relationship).
 *
 * @author rdk <rdk@krupczak.org>
 * @version $Id: $
 */

public class XmpDetector implements SyncServiceDetector {
    private static final String DEFAULT_SERVICE_NAME = "XMP";
    private static final String XMP_DEFAULT_AUTH_USER = "xmpUser";
    private static final Logger LOG = LoggerFactory.getLogger(XmpDetector.class);
    private static int XMP_DEFAULT_TIMEOUT = 3000; /* millseconds */
    private static int XMP_DEFAULT_RETRIES = 1;

    private SocketOpts sockopts;
    private int xmpPort;
    private int xmpTimeout;
    private int xmpRetries;
    private String xmpAuthenUser;
    private String xmpServiceName;
    private String ipMatch;
    private Date createTimeDate;

    /**
     * @param serviceName a {@link java.lang.String} object
     * @param port        an int specifying tcp port number
     */
    public XmpDetector(String serviceName, int port) {
        // set default config
        xmpPort = port;
        xmpAuthenUser = XMP_DEFAULT_AUTH_USER;
        xmpTimeout = XMP_DEFAULT_TIMEOUT;
        xmpRetries = XMP_DEFAULT_RETRIES;

        // get socket opts
        sockopts = new SocketOpts();

        xmpServiceName = serviceName;

        // very important to set to null, not 0-len string
        // as provisiond's ip range matching functionality
        // will not correctly match if 0-len instead of null
        //ipMatch = new String("");

        ipMatch = null;
        createTimeDate = new Date();
        LOG.debug("XmpDetector created, service " + xmpServiceName + " at " + createTimeDate);
    } /* XmpDetector */

    public XmpDetector() {
        this(DEFAULT_SERVICE_NAME, Xmp.XMP_PORT);
    } /* XmpDetector */

    @Override
    public String getServiceName() {
        LOG.debug("XmpDetector: getServiceName");
        return xmpServiceName;
    }

    @Override
    public void setServiceName(String newServiceName) {
        LOG.debug("XmpDetector: setServiceName to " + newServiceName);
        xmpServiceName = newServiceName;
    }

    @Override
    public void init() {
        onInit();
    }

    public void onInit() {
        LOG.debug("XmpDetector: onInit starting");

        // try to get configuration
        try {
            XmpConfig protoConfig;

            XmpConfigFactory.init();
            protoConfig = XmpConfigFactory.getInstance().getXmpConfig();
            if (protoConfig.hasPort())
                xmpPort = protoConfig.getPort();
            if (protoConfig.hasTimeout())
                xmpTimeout = protoConfig.getTimeout();
            if (protoConfig.getAuthenUser() != null)
                xmpAuthenUser = protoConfig.getAuthenUser();

            sockopts.setConnectTimeout(xmpTimeout);

        } catch (Throwable e) {
            LOG.error("XmpDetector: no config factory, using defaults");
        }
    }

    @Override
    public int getPort() {
        LOG.trace("XmpDetector: getPort");
        return xmpPort;
    }

    @Override
    public void setPort(int newPort) {
        LOG.trace("XmpDetector: setPort to " + newPort);
        xmpPort = newPort;
    }

    @Override
    public void setIpMatch(String newIpMatch) {
        LOG.trace("XmpDetector: setIpMatch to " + newIpMatch);
        ipMatch = newIpMatch;
    }

    @Override
    public String getIpMatch() {
        LOG.trace("XmpDetector: getIpMatch returning '" + ipMatch + "'");
        return ipMatch;
    }

    @Override
    public int getTimeout() {
        LOG.trace("XmpDetector: getTimeout returning " + xmpTimeout);
        return xmpTimeout;
    }

    @Override
    public void setTimeout(int newTimeout) {
        LOG.trace("XmpDetector: setTimeout to " + newTimeout);
        xmpTimeout = newTimeout;
        sockopts.setConnectTimeout(xmpTimeout);
    }

    @Override
    public void dispose() {
        LOG.debug("XmpDetector: dispose invoked");
        // dispose of anything like sessions, etc.
        // no need to dispose SocketOpts
    }

    @Override
    public final DetectResults detect(DetectRequest request) {
        final InetAddress address = request.getAddress();
        XmpSession aSession;
        XmpMessage aReply;
        XmpVar[] vars, replyVars;

        LOG.debug("XmpDetector: isServiceDetected starting to query " + address);

        // try to establish session
        aSession = new XmpSession(sockopts, address, xmpPort, xmpAuthenUser);
        if (aSession.isClosed()) {
            LOG.debug("XmpDetector: null session to " + address);
            return new DetectResultsImpl(false);
        }

        LOG.debug("XmpDetector: isServiceDetected session established with " + address);
        // query for core.sysName, core.sysDescr, 
        // core.sysUpTime, core.xmpdVersion
        vars = new XmpVar[]{
                new XmpVar("core", "sysName", "", "", Xmp.SYNTAX_NULLSYNTAX),
                new XmpVar("core", "sysDescr", "", "", Xmp.SYNTAX_NULLSYNTAX),
                new XmpVar("core", "sysUpTime", "", "", Xmp.SYNTAX_NULLSYNTAX),
                new XmpVar("core", "xmpdVersion", "", "", Xmp.SYNTAX_NULLSYNTAX),
        };

        if ((aReply = aSession.queryVars(vars)) == null) {
            LOG.debug("XmpDetector: isServiceDetected no vars from " + address);
            aSession.closeSession();
            return new DetectResultsImpl(false);
        }

        aSession.closeSession();

        // log what we retrieved
        if ((replyVars = aReply.getMIBVars()) == null) {
            LOG.debug("XmpDetector: isServiceDetected no replyVars for " + address);
            return new DetectResultsImpl(false);

        } /* if replyVars == null */

        LOG.debug("XmpDetector: isServiceDetected " + address + " reports " +
                replyVars[0].getValue() + "," +
                replyVars[1].getValue());
        LOG.debug("XmpDetector: isServiceDetected true for " + address);

        return new DetectResultsImpl(true);

    } /* isServiceDetected */

} /* class XmpDetector */
