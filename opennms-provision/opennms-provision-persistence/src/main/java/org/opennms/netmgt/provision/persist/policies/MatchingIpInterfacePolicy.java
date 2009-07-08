/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("Match IP Interface")
public class MatchingIpInterfacePolicy extends BasePolicy<OnmsIpInterface> implements IpInterfacePolicy {
    
    

    public static enum Action { MANAGE, UNMANAGE, DO_NOT_PERSIST };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    @Require({"MANAGE", "UNMANAGE", "DO_NOT_PERSIST"})
    public String getAction() {
        return m_action.toString();
    }
    
    public void setAction(String action) {
        if (Action.MANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.MANAGE;
        } else if (Action.UNMANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.UNMANAGE;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    @Override
    public OnmsIpInterface act(OnmsIpInterface iface) {
        switch (m_action) {
        case DO_NOT_PERSIST: 
            info("NOT Peristing %s according to policy", iface);
            return null;
        case MANAGE:
            info("Managing %s according to policy", iface);
            iface.setIsManaged("M");
            return iface;
        case UNMANAGE:
            info("Unmanaging %s according to policy", iface);
            iface.setIsManaged("U");
            return iface;
        default:
            return iface;    
        }
    }
    
    public void setIpAddress(String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }
    public String getIpAddress() {
        return getCriteria("ipAddress");
    }
    public void setHostName(String hostName) {
        putCriteria("ipHostName", hostName);
    }
    public String getHostName() {
        return getCriteria("ipHostName");
    }
}
