/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.icmp;

import java.net.InetAddress;

import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(IcmpDetector.class);
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
        LOG.debug("isServiceDetected: Testing ICMP based service for address: {}...", address);

        boolean found = false;
        try {
            for(int i = 0; i < getRetries() && !found; i++) {
                Number retval = PingerFactory.getInstance().ping(address, getTimeout(), getRetries());
                
                LOG.debug("isServiceDetected: Response time for address: {} is: {}.", address, retval);
                
                if (retval != null) {
                    found = true;
                }
            }
            
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}.", address, found);

        } catch (final InterruptedException e) {
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}. Received an InterruptedException.", address, false);
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            LOG.info("isServiceDetected: ICMP based service for address: {} is detected: {}. Received an Exception {}.", address, false, e);
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
