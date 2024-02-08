/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.asterisk.agi.scripts;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(ReadNoticeDetailsAgiScript.class);

    /** {@inheritDoc} */
    @Override
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
            LOG.info("User has no TUI PIN, so proceeding without authentication");
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
            LOG.warn("User {} failed authentication", getVariable(VAR_OPENNMS_USERNAME));
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
            LOG.debug("Reading node label to user: {}", nodeLabel);
            streamFileInterruptible("node");
            try {
                streamFileInterruptible(nodeLabel.toLowerCase());
            } catch (AgiException e) {
                sayAlphaInterruptible(nodeLabel);
            }
        } else if (!"".equals(nodeID)) {
            LOG.debug("Reading node ID to user: {}", nodeID);
            streamFileInterruptible("node");
            streamFileInterruptible("number");
            sayDigitsInterruptible(nodeID);
        } else {
            LOG.debug("No node label or node ID available");
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
            LOG.debug("Reading IP address to user: {}", ipAddr);
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
            LOG.debug("Reading service name to user: {}", svcName);
            streamFileInterruptible("service");
            try {
                streamFileInterruptible(svcName.toLowerCase());
            } catch (AgiException e) {
                sayAlphaInterruptible(svcName);
            }
        } else {
            LOG.debug("No service name available");
        }
    }

}
