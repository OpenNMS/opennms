/**
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2009 Jan 16: Created file - jeffg@opennms.org
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.asterisk.agi.scripts;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;

/**
 * An AGI script that reads the node ID and service name of
 * an OpenNMS notice to the called party
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 */
public class ReadNoticeDetailsAgiScript extends BaseOnmsAgiScript {

    /** {@inheritDoc} */
    public void service(AgiRequest req, AgiChannel chan) throws AgiException {
        authenticateUser();
        sayNode();
        sayServiceName();
        streamFile("silence/1");
        sayIpAddr();
    }
    
    /**
     * <p>authenticateUser</p>
     *
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    public void authenticateUser() throws AgiException {
        String actualPin = getVariable(VAR_OPENNMS_USER_PIN);
        if (actualPin == null || "".equals(actualPin)) {
            log().info("User has no TUI PIN, so proceeding without authentication");
            return;
        }
        String inputPin = null;
        int attempts = 0;
        while (! String.valueOf(inputPin).equals(String.valueOf(actualPin)) && attempts < 3) { 
            if (attempts > 0) {
                streamFile("auth-incorrect");
            }
            inputPin = getData("enter-password");
            attempts++;
        }
        if (String.valueOf(inputPin).equals(String.valueOf(actualPin))) {
            return;
        } else {
            log().warn("User " + getVariable(VAR_OPENNMS_USERNAME) + " failed authentication");
            streamFile("auth-incorrect");
            streamFile("goodbye");
            hangup();
        }
    }
    
    /**
     * <p>sayNode</p>
     *
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    public void sayNode() throws AgiException {
        String nodeID = null;
        String nodeLabel = null;
        nodeID = getVariable(VAR_OPENNMS_NODEID);
        nodeLabel = getVariable(VAR_OPENNMS_NODELABEL);
        
        
        if (! "".equals(nodeLabel)) {
            log().debug("Reading node label to user: " + nodeLabel);
            streamFileInterruptible("node");
            try {
                streamFileInterruptible(nodeLabel.toLowerCase());
            } catch (AgiException e) {
                sayAlphaInterruptible(nodeLabel);
            }
        } else if (!"".equals(nodeID)) {
            log().debug("Reading node ID to user: " + nodeID);
            streamFileInterruptible("node");
            streamFileInterruptible("number");
            sayDigitsInterruptible(nodeID);
        } else {
            log().debug("No node label or node ID available");
        }
    }
    
    /**
     * <p>sayIpAddr</p>
     *
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    public void sayIpAddr() throws AgiException {
        String ipAddr = null;
        ipAddr = getVariable(VAR_OPENNMS_INTERFACE);
        
        if ((ipAddr != null) && (!"".equals(ipAddr))) {
            log().debug("Reading IP address to user: " + ipAddr);
            streamFileInterruptible("letters/i");
            streamFileInterruptible("letters/p");
            streamFileInterruptible("address");
            sayIpAddressInterruptible(ipAddr);
        }
    }
    
    /**
     * <p>sayServiceName</p>
     *
     * @throws org.asteriskjava.fastagi.AgiException if any.
     */
    public void sayServiceName() throws AgiException {
        String svcName = null;
        svcName = getVariable("OPENNMS_SERVICE");
        
        if ((svcName != null) && (!"".equals(svcName))) {
            log().debug("Reading service name to user: " + svcName);
            streamFileInterruptible("service");
            try {
                streamFileInterruptible(svcName.toLowerCase());
            } catch (AgiException e) {
                sayAlphaInterruptible(svcName);
            }
        } else {
            log().debug("No service name available");
        }
    }

}
