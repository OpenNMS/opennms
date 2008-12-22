package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.Policy;

public class IPManagementMatchPolicy implements Policy<OnmsIpInterface> {

    public OnmsIpInterface apply(OnmsIpInterface iface) {
        return iface;
    }

}
