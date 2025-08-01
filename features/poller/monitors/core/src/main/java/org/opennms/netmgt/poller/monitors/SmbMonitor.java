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
package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.CIFSContext;
import jcifs.NameServiceClient;
import jcifs.context.BaseContext;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.netbios.NbtAddress;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SMB service on remote interfaces. Poll the specified address
 * for response to NetBIOS name queries.
 * 
 * The class implements the ServiceMonitor interface that allows it to be used along
 * with other plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 */
final public class SmbMonitor extends AbstractServiceMonitor {
    
    public static final Logger LOG = LoggerFactory.getLogger(SmbMonitor.class);
    
    /**
     * Do a node-status request before checking name?
     * First appears in OpenNMS 1.10.10. Default is true.
     */
    private static final String DO_NODE_STATUS = "do-node-status";
    private static final boolean DO_NODE_STATUS_DEFAULT = true;
    
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        // Extract the address
        //
        InetAddress ipAddr = svc.getAddress();

        // Default is a failed status
        //
        PollStatus serviceStatus = PollStatus.unavailable();

        // Attempt to retrieve NetBIOS name of this interface in order
        // to determine if SMB is supported.
        //
        NbtAddress nbtAddr = null;
        CIFSContext base = SingletonContext.getInstance();
        NameServiceClient nsc = new NameServiceClientImpl(base);
        
        /*
         * This try block was updated to reflect the behavior of the plugin.
         */
        final String hostAddress = InetAddressUtils.str(ipAddr);

        final boolean doNodeStatus = ParameterMap.getKeyedBoolean(parameters, DO_NODE_STATUS, DO_NODE_STATUS_DEFAULT);

        try {
            nbtAddr = (NbtAddress) nsc.getNbtByName(hostAddress);
            
            if (doNodeStatus) {
                nbtAddr.getNodeType(base);
            }
            
            if (!nbtAddr.getHostName().equals(hostAddress))
                serviceStatus = PollStatus.available();

        } catch (UnknownHostException uhE) {
        	String reason = "Unknown host exception generated for " + hostAddress + ", reason: " + uhE.getLocalizedMessage();
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        } catch (RuntimeException rE) {
        	LOG.debug("Unexpected runtime exception", rE);
            serviceStatus = PollStatus.unavailable("Unexpected runtime exception");
        } catch (Throwable e) {
        	LOG.debug("Unexpected exception", e);
            serviceStatus = PollStatus.unavailable("Unexpected exception");
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
