/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.policies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>MatchingIpInterfacePolicy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Match IP Interface")
public class MatchingIpInterfacePolicy extends BasePolicy<OnmsIpInterface> implements IpInterfacePolicy {
    private static final Logger LOG = LoggerFactory.getLogger(MatchingIpInterfacePolicy.class);
    
    

    public static enum Action { MANAGE, UNMANAGE, DO_NOT_PERSIST, ENABLE_SNMP_POLL,DISABLE_SNMP_POLL, ENABLE_COLLECTION, DISABLE_COLLECTION };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    /**
     * <p>getAction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require({"MANAGE", "UNMANAGE", "DO_NOT_PERSIST", "ENABLE_SNMP_POLL", "DISABLE_SNMP_POLL", "ENABLE_COLLECTION", "DISABLE_COLLECTION"})
    public String getAction() {
        return m_action.toString();
    }
    
    /**
     * <p>setAction</p>
     *
     * @param action a {@link java.lang.String} object.
     */
    public void setAction(String action) {
        if (Action.MANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.MANAGE;
        } else if (Action.UNMANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.UNMANAGE;
        } else if (Action.ENABLE_SNMP_POLL.toString().equalsIgnoreCase(action)) {
            m_action = Action.ENABLE_SNMP_POLL;
        } else if (Action.DISABLE_SNMP_POLL.toString().equalsIgnoreCase(action)) {
            m_action = Action.DISABLE_SNMP_POLL;
        } else if (action != null && action.toUpperCase().equals("ENABLE_COLLECTION")) {
            m_action = Action.ENABLE_COLLECTION;
        } else if (action != null && action.toUpperCase().equals("DISABLE_COLLECTION")) {
            m_action = Action.DISABLE_COLLECTION;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface act(OnmsIpInterface iface) {
        OnmsSnmpInterface snmpiface = iface.getSnmpInterface();
        switch (m_action) {
        case DO_NOT_PERSIST: 
            LOG.debug("NOT Persisting {} according to policy", iface);
            return null;
        case MANAGE:
            LOG.debug("Managing {} according to policy", iface);
            iface.setIsManaged("M");
            return iface;
        case UNMANAGE:
            LOG.debug("Unmanaging {} according to policy", iface);
            iface.setIsManaged("U");
            return iface;
        case ENABLE_SNMP_POLL:
            LOG.debug("SNMP polling {} according to policy", iface);
            snmpiface.setPoll("P");
            iface.setSnmpInterface(snmpiface);
            return iface;
        case DISABLE_SNMP_POLL:
            LOG.debug("Disable SNMP polling {} according to policy", iface);
            snmpiface.setPoll("N");
            iface.setSnmpInterface(snmpiface);
            return iface;
        case DISABLE_COLLECTION:
            LOG.debug("Disabled collection for {} according to policy", iface);
            snmpiface.setCollectionEnabled(false);
            iface.setSnmpInterface(snmpiface);
            return iface;
        case ENABLE_COLLECTION:
            LOG.debug("Enabled collection for {} according to policy", iface);
            snmpiface.setCollectionEnabled(true);
            iface.setSnmpInterface(snmpiface);
            return iface;
        default:
            return iface;    
        }
    }
    
    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return getCriteria("ipAddress");
    }
    /**
     * <p>setHostName</p>
     *
     * @param hostName a {@link java.lang.String} object.
     */
    public void setHostName(String hostName) {
        putCriteria("ipHostName", hostName);
    }
    /**
     * <p>getHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostName() {
        return getCriteria("ipHostName");
    }
}
