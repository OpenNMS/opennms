/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.ncs;

import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;

public class Component {
    long m_id;
    String m_foreignId;
    String m_foreignSource;
    String m_type;
    String m_name;
    DependencyRequirements m_dependenciesRequired;
    
    
    public Component( NCSComponent ncsComponent) {
        m_id = ncsComponent.getId();
        m_foreignId = ncsComponent.getForeignId();
        m_foreignSource = ncsComponent.getForeignSource();
        m_name = ncsComponent.getName();
        m_type = ncsComponent.getType();
        if ( ncsComponent.getDependenciesRequired() == null ) {
            m_dependenciesRequired = DependencyRequirements.ALL;
        }
        else {
            m_dependenciesRequired = ncsComponent.getDependenciesRequired();
        } 
    }
    
    
    
    public Component(long id, String type, String name, String foreignSource,
			String foreignId, DependencyRequirements dependenciesRequired) {
		m_id = id;
		m_type = type;
		m_name = name;
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;
		m_dependenciesRequired = dependenciesRequired;
	}



	public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public long getId() {
        return m_id;
    }
    public void setId(long id) {
        m_id = id;
    }
    public String getForeignId() {
        return m_foreignId;
    }
    public void setForeignId(String foreignId) {
        m_foreignId = foreignId;
    }
    public String getForeignSource() {
        return m_foreignSource;
    }
    public void setForeignSource(String foreignSource) {
        m_foreignSource = foreignSource;
    }
    public String getType() {
        return m_type;
    }
    public void setType(String type) {
        m_type = type;
    }
    public DependencyRequirements getDependenciesRequired() {
        return m_dependenciesRequired;
    }
    public void setDependenciesRequired(DependencyRequirements dependenciesRequired) {
        m_dependenciesRequired = dependenciesRequired;
    }
    
    @Override
    public int hashCode(){
        return Long.valueOf(m_id).hashCode();
    }

    @Override
    public boolean equals(final Object obj){
        if ( obj instanceof Component ) {
            return m_id == (( Component) obj).m_id;
        }
        return false;
    }



	@Override
	public String toString() {
		return "Component [name=" + m_name + "]";
	}
    
    

}
