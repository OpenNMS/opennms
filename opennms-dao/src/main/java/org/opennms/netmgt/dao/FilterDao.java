package org.opennms.netmgt.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.netmgt.filter.FilterParseException;

public interface FilterDao {
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException;
    public Map<String, Set<String>> getIPServiceMap(String rule) throws FilterParseException;
    public List<String> getIPList(String rule) throws FilterParseException;
    public boolean isValid(String addr, String rule) throws FilterParseException;
    
    public String getInterfaceWithServiceStatement(String rule);
    
    public void validateRule(String rule) throws FilterParseException;

}
