/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("Set Node Category")
public class NodeCategorySettingPolicy extends BasePolicy<OnmsNode> implements NodePolicy {
    
    private String m_category; 
    
    @Override
    public OnmsNode act(OnmsNode node) {
        if (getCategory() == null) {
            return node;
        }

        OnmsCategory category = new OnmsCategory(getCategory());
        
        node.addCategory(category);
        
        return node;
        
    }

    
    @Require(value = { }) 
    public String getCategory() {
        return m_category;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    public String getType() {
        return getCriteria("type");
    }

    public void setType(String type) {
        putCriteria("type", type);
    }

    public String getSysObjectId() {
        return getCriteria("sysObjectId");
    }

    public void setSysObjectId(String sysObjectId) {
        putCriteria("sysObjectId", sysObjectId);
    }

    public String getSysName() {
        return getCriteria("sysName");
    }

    public void setSysName(String sysName) {
        putCriteria("sysName", sysName);
    }

    public String getSysDescription() {
        return getCriteria("sysDescription");
    }

    public void setSysDescription(String sysDescription) {
        putCriteria("sysDescription", sysDescription);
    }

    public String getSysLocation() {
        return getCriteria("sysLocation");
    }

    public void setSysLocation(String sysLocation) {
        putCriteria("sysLocation", sysLocation);
    }

    public String getSysContact() {
        return getCriteria("sysContact");
    }

    public void setSysContact(String sysContact) {
        putCriteria("sysContact", sysContact);
    }

    public String getLabel() {
        return getCriteria("label");
    }

    public void setLabel(String label) {
        putCriteria("label", label);
    }

    public String getLabelSource() {
        return getCriteria("labelSource");
    }

    public void setLabelSource(String labelSource) {
        putCriteria("labelSource", labelSource);
    }

    public String getNetBiosName() {
        return getCriteria("netBiosName");
    }

    public void setNetBiosName(String netBiosName) {
        putCriteria("netBiosName", netBiosName);
    }

    public String getNetBiosDomain() {
        return getCriteria("netBiosDomain");
    }

    public void setNetBiosDomain(String netBiosDomain) {
        putCriteria("netBiosDomain", netBiosDomain);
    }

    public String getOperatingSystem() {
        return getCriteria("operatingSystem");
    }

    public void setOperatingSystem(String operatingSystem) {
        putCriteria("operatingSystem", operatingSystem);
    }

    public String getForeignId() {
        return getCriteria("foreignId");
    }

    public void setForeignId(String foreignId) {
        putCriteria("foreignId", foreignId);
    }

    public String getForeignSource() {
        return getCriteria("foreignSource");
    }

    public void setForeignSource(String foreignSource) {
        putCriteria("foreignSource", foreignSource);
    }

}
