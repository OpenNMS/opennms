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

import java.util.List;

import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

public interface NCSCorrelationService {
    
    public static class AttrParmMap {
        String m_attributeName;
        int m_paramterIndex;
        
        public AttrParmMap(String attributeName, int parameterIndex) {
            m_attributeName = attributeName;
            m_paramterIndex = parameterIndex;
        }
        
        public boolean matches(NCSComponent component, Event e) {
            if (!component.getAttributes().containsKey(m_attributeName)) return false;
            List<Parm> parms = e.getParmCollection();
            if (m_paramterIndex > parms.size()) return false;
            
            Parm parm = parms.get(m_paramterIndex - 1);
            Value val = parm.getValue();
            if (val == null) return false;
            
            String attrVal = component.getAttributes().get(m_attributeName);
            String eventVal = val.getContent();
            
            return attrVal == null ? eventVal == null : attrVal.equals(eventVal);
            
        }
        
    }
	
	List<NCSComponent> findComponentsThatDependOn(Long componentId);
	
	List<NCSComponent> findSubComponents(Long componentId);
	
	List<NCSComponent> findComponentsByNodeIdAndAttrParmMaps(Event e, AttrParmMap... parameterMap);
	
	List<NCSComponent> findComponentsByNodeIdAndEventParameters(Event e, String... parameterNames);

}
