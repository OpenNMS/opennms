package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class InclusiveInterfacePolicy extends BasePolicy implements IpInterfacePolicy {

    public OnmsIpInterface apply(OnmsIpInterface iface) {
        return iface;
    }


}
