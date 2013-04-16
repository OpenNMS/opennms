/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.ncs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opennms.core.utils.LogUtils;

import org.opennms.netmgt.correlation.ncs.NCSCorrelationService.AttrParmMap;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultNCSCorrelationService implements NCSCorrelationService {
	
	@Autowired
	NCSComponentRepository m_componentRepo;
	
	@Autowired
	NodeDao m_nodeDao;

	@Override
	public List<NCSComponent> findComponentsThatDependOn(Long componentId) {
		
		NCSComponent comp = m_componentRepo.get(componentId);
		
		List<NCSComponent> parents = m_componentRepo.findComponentsThatDependOn(comp);
		
		for(NCSComponent parent : parents) {
			m_componentRepo.initialize(parent);
			
		}
		
		return parents;

	}
	
    @Override
	public List<NCSComponent> findComponentsByNodeIdAndEventParameters(Event e, String... parameterNames) {
		
		assert e.getNodeid() != null;
		assert e.getNodeid() != 0;
		
		
		List<NCSComponent> components = m_componentRepo.findComponentsByNodeId(e.getNodeid().intValue());

		List<NCSComponent> matching = new LinkedList<NCSComponent>();
		for(NCSComponent component : components)
		{
			if (matches(component, e, parameterNames)) {
				matching.add(component);
			}
		}

		return matching;
	}
	
	private boolean matches(NCSComponent component, Event e, String... parameters) {
		
		for(String key : parameters) {
			if (!component.getAttributes().containsKey(key)) {
				return false;
			}
			
			String val = component.getAttributes().get(key);
			
			if (!val.equals(EventUtils.getParm(e, key))) {
				return false;
			}
		}
		
		return true;
		
	}

    @Override
    public List<NCSComponent> findSubComponents(Long componentId) {

                 
            NCSComponent comp = m_componentRepo.get(componentId);
            
            Set<NCSComponent> subcomponents = comp.getSubcomponents();
            
            for(NCSComponent subcomponent : subcomponents) {
                m_componentRepo.initialize(subcomponent);
                
            }
            
            return new ArrayList<NCSComponent>(subcomponents);

    }

    @Override
    public List<NCSComponent> findComponentsByNodeIdAndAttrParmMaps(Event e, AttrParmMap... parameterMap) {
        assert e.getNodeid() != null;
        assert e.getNodeid() != 0;
        
        
        List<NCSComponent> components = m_componentRepo.findComponentsByNodeId(e.getNodeid().intValue());

        List<NCSComponent> matching = new LinkedList<NCSComponent>();
        for(NCSComponent component : components)
        {
            if (matches(component, e, parameterMap)) {
                matching.add(component);
            }
        }

        return matching;
    }
    
    @Override
    public List<NCSComponent> findComponentsByNodeIdAndByParmName(Event e, String Name, int index) {
        assert e.getNodeid() != null;
        assert e.getNodeid() != 0;
        
        LogUtils.tracef(this,"Node Id '%s' in the Event '%s'",e.getNodeid().intValue(), e.getDbid());
        List<NCSComponent> components = m_componentRepo.findComponentsByNodeId(e.getNodeid().intValue());
        LogUtils.tracef(this,"Node Id '%s' in the Event '%s' - Total NCS component identified '%d'",
        		e.getNodeid().intValue(), 
        		e.getDbid(),
        		components.size());

        List<NCSComponent> matching = new LinkedList<NCSComponent>();
        for(NCSComponent component : components)
        {
            if (matches(component, e, index, Name)) {
            	LogUtils.tracef(this,"Node Id '%s' in the Event '%s' - NCS component is added '%s'",
            			e.getNodeid().intValue(), 
                		e.getDbid(),
            			component.getId());
                matching.add(component);
            }
        }

        LogUtils.tracef(this,"Node Id '%s' in the Event '%s' - Total component impacted '%s'",
    			e.getNodeid().intValue(), 
        		e.getDbid(),
        		matching.size());
        return matching;
    }

    private boolean matches(NCSComponent component, Event e, AttrParmMap[] parameterMap) {
        for(AttrParmMap map : parameterMap) {
            if (!map.matches(component, e)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean matches(NCSComponent component, Event event, int index,String Name) {
    	
    	List<Parm> parms = event.getParmCollection();
    	Parm parm = parms.get(index - 1); // get the second index
        
        LogUtils.tracef(this,"%s parameter name in event db %s " + event.getDbid(), parm.getParmName()); 
        String attrVal = component.getAttributes().get(Name);
        String parmName = parm.getParmName();
        
        if (parmName == null ) {
        	LogUtils.tracef(this,"Event parameter is null. No matching NCS component found for event %s " + event.getDbid());
        	return false;
        } else if (attrVal == null) {
        	LogUtils.tracef(this,"%s No matching NCS component found for parameter name %s " + event.getDbid(), parmName);
        	return false;
        } else if (attrVal.equals(parmName)) {
        	LogUtils.tracef(this,"%s Matching NCS component found for parameter name %s " + event.getDbid(), parm.getParmName());
        	return true;
        }
       
        return false;
        
      }


}
