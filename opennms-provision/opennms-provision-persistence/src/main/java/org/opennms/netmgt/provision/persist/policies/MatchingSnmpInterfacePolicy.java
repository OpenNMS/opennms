/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.persist.policies;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>MatchingSnmpInterfacePolicy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Match SNMP Interface")
public class MatchingSnmpInterfacePolicy extends BasePolicy<OnmsSnmpInterface> implements SnmpInterfacePolicy {
    private static final Logger LOG = LoggerFactory.getLogger(MatchingSnmpInterfacePolicy.class);
    
    public static enum Action { ENABLE_COLLECTION, DISABLE_COLLECTION, DO_NOT_PERSIST, ENABLE_POLLING, DISABLE_POLLING };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    /**
     * <p>getAction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require({"ENABLE_COLLECTION", "DISABLE_COLLECTION", "DO_NOT_PERSIST", "ENABLE_POLLING", "DISABLE_POLLING"})
    public String getAction() {
        return m_action.toString();
    }
    
    /**
     * <p>setAction</p>
     *
     * @param action a {@link java.lang.String} object.
     */
    public void setAction(String action) {
        if (action != null && action.equalsIgnoreCase("ENABLE_COLLECTION")) {
            m_action = Action.ENABLE_COLLECTION;
        } else if (action != null && action.equalsIgnoreCase("DISABLE_COLLECTION")) {
            m_action = Action.DISABLE_COLLECTION;
        } else if (action != null && action.equalsIgnoreCase("ENABLE_POLLING")) {
            m_action = Action.ENABLE_POLLING;
        } else if (action != null && action.equalsIgnoreCase("DISABLE_POLLING")) {
            m_action = Action.DISABLE_POLLING;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsSnmpInterface act(OnmsSnmpInterface iface, Map<String, Object> attributes) {
        switch (m_action) {
        case DO_NOT_PERSIST: 
            LOG.debug("NOT Persisting {} according to policy", iface);
            return null;
        case DISABLE_COLLECTION:
            iface.setCollectionEnabled(false, OnmsSnmpInterface.CollectDefinitionSource.POLICY);
            LOG.debug("Disabled collection for {} according to policy", iface);
            return iface;
        case ENABLE_COLLECTION:
            iface.setCollectionEnabled(true, OnmsSnmpInterface.CollectDefinitionSource.POLICY);
            LOG.debug("Enabled collection for {} according to policy", iface);
            return iface;
        case ENABLE_POLLING:
            iface.setPoll("P");
            LOG.debug("Enabled polling for {} according to policy", iface);
            return iface;
        case DISABLE_POLLING:
            iface.setPoll("N");
            LOG.debug("Disabled polling for {} according to policy", iface);
            return iface;
        default:
            return iface;    
        }
    }
    
    /**
     * <p>getIfDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr() {
        return getCriteria("ifDescr");
    }

    /**
     * <p>setIfDescr</p>
     *
     * @param ifDescr a {@link java.lang.String} object.
     */
    public void setIfDescr(String ifDescr) {
        putCriteria("ifDescr", ifDescr);
    }

    /**
     * <p>getIfName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfName() {
        return getCriteria("ifName");
    }

    /**
     * <p>setIfName</p>
     *
     * @param ifName a {@link java.lang.String} object.
     */
    public void setIfName(String ifName) {
        putCriteria("ifName", ifName);
    }

    /**
     * <p>getIfType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfType() {
        return getCriteria("ifType");
    }

    /**
     * <p>setIfType</p>
     *
     * @param ifType a {@link java.lang.String} object.
     */
    public void setIfType(String ifType) {
        putCriteria("ifType", ifType);
    }

    /**
     * <p>getPhysAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPhysAddr() {
        return getCriteria("physAddr");
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param physAddr a {@link java.lang.String} object.
     */
    public void setPhysAddr(String physAddr) {
        putCriteria("physAddr", physAddr);
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfIndex() {
        return getCriteria("ifIndex");
    }

    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.String} object.
     */
    public void setIfIndex(String ifIndex) {
        putCriteria("ifIndex", ifIndex);
    }

    /**
     * <p>getIfSpeed</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfSpeed() {
        return getCriteria("ifSpeed");
    }

    /**
     * <p>setIfSpeed</p>
     *
     * @param ifSpeed a {@link java.lang.String} object.
     */
    public void setIfSpeed(String ifSpeed) {
        putCriteria("ifSpeed", ifSpeed);
    }

    /**
     * <p>getIfAdminStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAdminStatus() {
        return getCriteria("ifAdminStatus");
    }

    /**
     * <p>setIfAdminStatus</p>
     *
     * @param ifAdminStatus a {@link java.lang.String} object.
     */
    public void setIfAdminStatus(String ifAdminStatus) {
        putCriteria("ifAdminStatus", ifAdminStatus);
    }

    /**
     * <p>getIfOperStatus</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfOperStatus() {
        return getCriteria("ifOperStatus");
    }

    /**
     * <p>setIfOperStatus</p>
     *
     * @param ifOperStatus a {@link java.lang.String} object.
     */
    public void setIfOperStatus(String ifOperStatus) {
        putCriteria("ifOperStatus", ifOperStatus);
    }

    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return getCriteria("ifAlias");
    }

    /**
     * <p>setIfAlias</p>
     *
     * @param ifAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String ifAlias) {
        putCriteria("ifAlias", ifAlias);
    }
    
    
}
