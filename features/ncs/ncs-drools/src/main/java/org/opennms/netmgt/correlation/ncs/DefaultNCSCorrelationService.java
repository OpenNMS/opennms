package org.opennms.netmgt.correlation.ncs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.opennms.netmgt.xml.event.Event;
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

    private boolean matches(NCSComponent component, Event e, AttrParmMap[] parameterMap) {
        for(AttrParmMap map : parameterMap) {
            if (!map.matches(component, e)) {
                return false;
            }
        }
        
        return true;
    }


}
