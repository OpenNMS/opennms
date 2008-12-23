package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;

public class MatchingInterfacePolicy extends BasePolicy<OnmsIpInterface> {
    public OnmsIpInterface apply(OnmsIpInterface iface) {
        if (getParameter("ipaddress") != null) {
            if (!match(iface.getIpAddress(), getParameter("ipaddress"))) {
                return null;
            }
        }
        if (getParameter("hostname") != null) {
            if (!match(iface.getIpHostName(), getParameter("hostname"))) {
                return null;
            }
        }
        return iface;
    }

    private boolean match(String s, String matcher) {
        if (matcher.startsWith("~")) {
            matcher = matcher.replaceFirst("~", "");
            return s.matches(matcher);
        } else {
            return s.equals(matcher);
        }
    }
}
