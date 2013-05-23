/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client.data;

import java.util.HashSet;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class ApplicationFilter implements LocationFilter {
    
    private final Set<ApplicationInfo> m_applications = new HashSet<ApplicationInfo>();

    public Set<ApplicationInfo> getApplications() {
        return m_applications;
    }

    @Override
    public boolean matches(final LocationInfo location) {
        if(getApplications().size() == 0) {
            return true;
        }else {
            for (final ApplicationInfo app : getApplications()) {
                if (app.getLocations().contains(location.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeApplication(ApplicationInfo appInfo) {
        getApplications().remove(appInfo);
    }

    public boolean addApplication(final ApplicationInfo app) {
        return getApplications().add(app);
    }
}
