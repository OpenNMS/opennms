/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.NoAgentNodePolicy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NoAgentNodeCategorySettingPolicy class.</p>
 *
 * @author ayres
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Set Node Category (no agent)")
public class NoAgentNodeCategorySettingPolicy extends BasePolicy<OnmsNode> implements NoAgentNodePolicy {
    
    private String m_category; 
    
    /** {@inheritDoc} */
    @Override
    public OnmsNode act(OnmsNode node) {
        if (getCategory() == null) {
            return node;
        }

        OnmsCategory category = new OnmsCategory(getCategory());
        
        node.addCategory(category);
        
        return node;
        
    }

    
    /**
     * <p>getCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require(value = { }) 
    public String getCategory() {
        return m_category;
    }

    /**
     * <p>setCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(String category) {
        m_category = category;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return getCriteria("type");
    }

    /**
     * <p>setType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        putCriteria("type", type);
    }

    /**
     * <p>getLabelSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabelSource() {
        return getCriteria("labelSource");
    }

    /**
     * <p>setLabelSource</p>
     *
     * @param labelSource a {@link java.lang.String} object.
     */
    public void setLabelSource(String labelSource) {
        putCriteria("labelSource", labelSource);
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return getCriteria("label");
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        putCriteria("label", label);
    }

    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        return getCriteria("foreignId");
    }

    /**
     * <p>setForeignId</p>
     *
     * @param foreignId a {@link java.lang.String} object.
     */
    public void setForeignId(String foreignId) {
        putCriteria("foreignId", foreignId);
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        return getCriteria("foreignSource");
    }

    /**
     * <p>setForeignSource</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public void setForeignSource(String foreignSource) {
        putCriteria("foreignSource", foreignSource);
    }

}
