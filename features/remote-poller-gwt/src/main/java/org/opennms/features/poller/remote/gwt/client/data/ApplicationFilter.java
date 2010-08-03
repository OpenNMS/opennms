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
