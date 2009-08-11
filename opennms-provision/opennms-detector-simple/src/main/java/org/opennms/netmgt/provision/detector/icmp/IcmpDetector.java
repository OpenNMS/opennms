/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.icmp;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.ping.PingConstants;
import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.support.AbstractDetector;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class IcmpDetector extends AbstractDetector {
    
    @SuppressWarnings("deprecation")
    public IcmpDetector() {
        init();
    }
    
    public void init() {
        setTimeout(PingConstants.DEFAULT_TIMEOUT);
        setRetries(PingConstants.DEFAULT_RETRIES);
    }
    
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectorMonitor) {
        StringBuffer sb = new StringBuffer();

        sb.delete(0, sb.length());
        sb.append("isServiceDetected: Testing ICMP based service for address: ");
        sb.append(address);
        sb.append("...");
        log().info(sb.toString());
        
        try {
            for(int i = 0; i < getRetries(); i++) {
                Long retval = Pinger.ping(address, getTimeout(), getRetries());
                
                sb.delete(0, sb.length());
                sb.append("isServiceDetected: Response time for address: ");
                sb.append(address);
                sb.append(" is: ");
                sb.append(retval);
                sb.append('.');
                log().debug(retval);
                
                if (retval != null) {
                    sb.delete(0, sb.length());
                    sb.append("isServiceDetected: ICMP based service for address: ");
                    sb.append(address);
                    sb.append(" is detected: ");
                    sb.append(true);
                    sb.append('.');
                    log().debug(sb.toString());
                    return true;
                }
            }
            
//        } catch (Exception e) {
//            sb.delete(0, sb.length());
//            sb.append("isServiceDetected: ICMP based protocol for address: ");
//            sb.append(address);
//            sb.append(" is detected: ");
//            sb.append(false);
//            sb.append('.');
//            log().debug(sb.toString());
//            detectorMonitor.failure(this, "%s: Failed to detect %s on address %s", getServiceName(), getServiceName(), address.getHostAddress());
        } catch (InterruptedException e) {
          sb.delete(0, sb.length());
          sb.append("isServiceDetected: ICMP based protocol for address: ");
          sb.append(address);
          sb.append(" is detected: ");
          sb.append(false);
          sb.append(". Received an Interrupted exception.");
          log().debug(sb.toString());
        } catch (Exception e) {
            sb.delete(0, sb.length());
            sb.append("isServiceDetected: ICMP based protocol for address: ");
            sb.append(address);
            sb.append(" is detected: ");
            sb.append(false);
            sb.append(". Received an Exception.");
            log().debug(sb.toString());
        } finally {
            
        }
        
        return false;
    }

    
    @Override
    protected void onInit() {
        
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
