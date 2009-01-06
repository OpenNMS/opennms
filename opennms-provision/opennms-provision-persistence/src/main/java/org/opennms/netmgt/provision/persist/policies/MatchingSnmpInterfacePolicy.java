package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;

public class MatchingSnmpInterfacePolicy extends BasePolicy<OnmsSnmpInterface> {
    public OnmsSnmpInterface apply(OnmsSnmpInterface iface) {
        if (getParameter("ifdescr") != null) {
            if (!match(iface.getIfDescr(), getParameter("ifdescr"))) {
                return null;
            }
        }
        if (getParameter("ifname") != null) {
            if (!match(iface.getIfName(), getParameter("ifname"))) {
                return null;
            }
        }
        if (getParameter("iftype") != null) {
            if (iface.getIfType() == null) {
                return null;
            }
            if (!match(iface.getIfType().toString(), getParameter("iftype"))) {
                return null;
            }
        }
        if (getParameter("ipaddress") != null) {
            if (!match(iface.getIpAddress(), getParameter("ipaddress"))) {
                return null;
            }
        }
        if (getParameter("netmask") != null) {
            if (!match(iface.getIpAddress(), getParameter("netmask"))) {
                return null;
            }
        }
        if (getParameter("physaddr") != null) {
            if (!match(iface.getPhysAddr(), getParameter("physaddr"))) {
                return null;
            }
        }
        if (getParameter("ifindex") != null) {
            if (iface.getIfIndex() == null) {
                return null;
            }
            if (!match(iface.getIfIndex().toString(), getParameter("ifindex"))) {
                return null;
            }
        }
        if (getParameter("ifspeed") != null) {
            if (iface.getIfSpeed() == null) {
                return null;
            }
            if (!match(iface.getIfSpeed().toString(), getParameter("ifspeed"))) {
                return null;
            }
        }
        if (getParameter("ifadminstatus") != null) {
            if (iface.getIfAdminStatus() == null) {
                return null;
            }
            if (!match(iface.getIfAdminStatus().toString(), getParameter("ifadminstatus"))) {
                return null;
            }
        }
        if (getParameter("ifoperstatus") != null) {
            if (iface.getIfOperStatus() == null) {
                return null;
            }
            if (!match(iface.getIfOperStatus().toString(), getParameter("ifoperstatus"))) {
                return null;
            }
        }
        if (getParameter("ifalias") != null) {
            if (!match(iface.getIfAlias(), getParameter("ifalias"))) {
                return null;
            }
        }
        return iface;
    }

}
