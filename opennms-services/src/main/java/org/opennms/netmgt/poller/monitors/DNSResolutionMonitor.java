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

package org.opennms.netmgt.poller.monitors;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 * DNSResolutionMonitor
 *
 * @author brozow
 */
@Distributable
public class DNSResolutionMonitor extends AbstractServiceMonitor {
    
    public static final String RESOLUTION_TYPE_PARM = "resolution-type";
    public static final String RT_V4 = "v4";
    public static final String RT_V6 = "v6";
    public static final String RT_BOTH = "both";
    public static final String RT_EITHER = "either";
    public static final String RESOLUTION_TYPE_DEFAULT = RT_EITHER;

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        String nodeLabel = svc.getNodeLabel();
        
        String resolutionType = ParameterMap.getKeyedString(parameters, RESOLUTION_TYPE_PARM, RESOLUTION_TYPE_DEFAULT);
        boolean requireV4 = RT_V4.equalsIgnoreCase(resolutionType) || RT_BOTH.equals(resolutionType);
        boolean requireV6 = RT_V6.equalsIgnoreCase(resolutionType) || RT_BOTH.equals(resolutionType);
        

        try {
            long start = System.currentTimeMillis();
            InetAddress[] addrs = resolve(nodeLabel);
            long end = System.currentTimeMillis();

            boolean v4found = false;
            boolean v6found = false;
            for(InetAddress addr : addrs) {
                log().debug("Resolved " + nodeLabel + " to " + addr);
                if (addr instanceof Inet4Address) {
                    v4found = true;
                } else if(addr instanceof Inet6Address) {
                    v6found = true;
                }
            }

            if (!v4found && !v6found) {
                return logDown(Level.INFO, "Unable to resolve " + nodeLabel);
            } 
            if (requireV4 && !v4found) {
                return logDown(Level.INFO, nodeLabel + " could only be resolved to an IPv6 address");
            }
            if (requireV6 && !v6found) {
                return logDown(Level.INFO, nodeLabel + " could only be resolved to an IPv4 address");
            }
            return logUp(Level.INFO, (double)(end - start), "Resolved " + nodeLabel + " correctly!");

        } catch (TextParseException e) {
            return logDown(Level.INFO,"Unable to resolve "+nodeLabel+": "+e.getMessage());
        }

    }
    
    InetAddress[] resolve(String hostname) throws TextParseException {
        Record[] aaaaRecords = new Lookup(hostname, Type.AAAA).run();
        Record[] aRecords = new Lookup(hostname, Type.A).run();
        
        InetAddress[] addrs = new InetAddress[(aaaaRecords == null ? 0 : aaaaRecords.length)+(aRecords == null ? 0 : aRecords.length)];
        
        int index = 0;
        if (aaaaRecords != null) {
            for(Record r : aaaaRecords) {
                addrs[index++] = ((AAAARecord)r).getAddress();
            }
        }
        
        if (aRecords != null) {
            for(Record r : aRecords) {
                addrs[index++] = ((ARecord)r).getAddress();
            }
        }
        
        return addrs;
    }

}
