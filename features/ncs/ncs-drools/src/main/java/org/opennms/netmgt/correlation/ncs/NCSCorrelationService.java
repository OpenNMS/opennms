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
