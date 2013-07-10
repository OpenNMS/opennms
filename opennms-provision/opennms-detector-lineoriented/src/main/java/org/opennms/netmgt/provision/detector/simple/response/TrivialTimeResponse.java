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

package org.opennms.netmgt.provision.detector.simple.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>TrivialTimeResponse class.</p>
 *
 * @author Alejandro Galue <agalue@sync.com.ve>
 * @version $Id: $
 */
public class TrivialTimeResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(TrivialTimeResponse.class);
    boolean available;

    public TrivialTimeResponse() {
        available = false;
    }

    public TrivialTimeResponse(int remoteTime, int localTime, int allowedSkew) {
        available = false;
        LOG.debug("qualifyTime: checking remote time {} against local time {} with max skew of {}", remoteTime, localTime, allowedSkew);
        if ((localTime - remoteTime > allowedSkew) || (remoteTime - localTime > allowedSkew)) {
            if (localTime > remoteTime) {
                LOG.debug("Remote time is {} seconds slow", (localTime-remoteTime));
            } else {
                LOG.debug("Remote time is {} seconds fast", (remoteTime-localTime));
            }
        }
        if ((localTime > remoteTime) && (localTime - remoteTime > allowedSkew)) {
            LOG.debug("Remote time is {} seconds behind local, more than the allowable {}", (localTime - remoteTime), allowedSkew);
        } else if ((remoteTime > localTime) && (remoteTime - localTime > allowedSkew)) {
            LOG.debug("Remote time is {} seconds ahead of local, more than the allowable {}", (remoteTime - localTime), allowedSkew);
        } else {
            available = true;
        }
    }

    public boolean isAvailable() {
        return available;
    }

}
