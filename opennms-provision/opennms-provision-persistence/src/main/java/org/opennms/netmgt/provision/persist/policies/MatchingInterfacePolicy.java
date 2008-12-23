package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;

public class MatchingInterfacePolicy extends BasePolicy<OnmsIpInterface> {

    public OnmsIpInterface apply(OnmsIpInterface iface) {
        return null;
    }
}
