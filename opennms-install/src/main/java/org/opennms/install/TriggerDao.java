package org.opennms.install;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TriggerDao {
    private Map<String, Trigger> m_nameMap =
        new LinkedHashMap<String, Trigger>();
    private Map<String, List<Trigger>> m_tableMap =
        new HashMap<String, List<Trigger>>();

    public TriggerDao() {
        
    }
    
    public void add(Trigger t) {
        String lowerName = t.getName().toLowerCase();
        if (m_nameMap.containsKey(lowerName)) {
            throw new IllegalArgumentException("Trigger with name of '"
                                               + lowerName
                                               + "' already exists.");
        }
        
        m_nameMap.put(lowerName, t);
        
        getTriggersForTableCreateIfEmpty(t.getTable().toLowerCase()).add(t);
    }
    
    private List<Trigger> getTriggersForTableCreateIfEmpty(String table) {
        if (!m_tableMap.containsKey(table)) {
            m_tableMap.put(table, new LinkedList<Trigger>());
        }
        return m_tableMap.get(table);
    }
    
    public List<Trigger> getTriggersForTable(String table) {
        String lowerName = table.toLowerCase();
        if (!m_tableMap.containsKey(lowerName)) {
            return new LinkedList<Trigger>();
        }
        return m_tableMap.get(lowerName);
    }
}
