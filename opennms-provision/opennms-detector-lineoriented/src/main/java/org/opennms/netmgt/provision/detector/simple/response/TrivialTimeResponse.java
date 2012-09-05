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

package org.opennms.netmgt.provision.detector.simple.response;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>TrivialTimeResponse class.</p>
 *
 * @author Alejandro Galue <agalue@sync.com.ve>
 * @version $Id: $
 */
public class TrivialTimeResponse {

    boolean available;

    public TrivialTimeResponse() {
        available = false;
    }

    public TrivialTimeResponse(int remoteTime, int localTime, int allowedSkew) {
        available = false;
        log().debug("qualifyTime: checking remote time " + remoteTime + " against local time " + localTime + " with max skew of " + allowedSkew);
        if ((localTime - remoteTime > allowedSkew) || (remoteTime - localTime > allowedSkew)) {
            log().debug("Remote time is " + (localTime > remoteTime ? ""+(localTime-remoteTime)+" seconds slow" : ""+(remoteTime-localTime)+" seconds fast"));
        }
        if ((localTime > remoteTime) && (localTime - remoteTime > allowedSkew)) {
            log().debug("Remote time is " + (localTime - remoteTime) + " seconds behind local, more than the allowable " + allowedSkew);
        } else if ((remoteTime > localTime) && (remoteTime - localTime > allowedSkew)) {
            log().debug("Remote time is " + (remoteTime - localTime) + " seconds ahead of local, more than the allowable " + allowedSkew);
        } else {
            available = true;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
