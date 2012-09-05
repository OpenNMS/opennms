/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.icmp;

import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>IcmpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class IcmpDetector extends SyncAbstractDetector {
    
    /**
     * <p>Constructor for IcmpDetector.</p>
     */
    public IcmpDetector() {
        super("ICMP", -1);
        init();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isServiceDetected(InetAddress address) {
        
        LogUtils.debugf(this, "isServiceDetected: Testing ICMP based service for address: %s...", address);

        boolean found = false;
        try {
            for(int i = 0; i < getRetries() && !found; i++) {
                Number retval = PingerFactory.getInstance().ping(address, getTimeout(), getRetries());
                
                LogUtils.debugf(this, "isServiceDetected: Response time for address: %s is: %d.", address, retval);
                
                if (retval != null) {
                    found = true;
                }
            }
            
            LogUtils.infof(this, "isServiceDetected: ICMP based service for address: %s is detected: %s.", address, found);

        } catch (final InterruptedException e) {
            LogUtils.infof(this, "isServiceDetected: ICMP based service for address: %s is detected: %s. Received an InterruptedException.", address, false);
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            LogUtils.infof(this, "isServiceDetected: ICMP based service for address: %s is detected: %s. Received an Exception %s.", address, false, e);
        }
        
        return found;
    }

    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        setTimeout(PingConstants.DEFAULT_TIMEOUT);
        setRetries(PingConstants.DEFAULT_RETRIES);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
