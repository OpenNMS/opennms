package org.opennms.netmgt.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.model.EntityVisitor;

public interface FilterDao {
    /**
     * This method returns a map of all node IDs and node labels that match
     * the rule that is passed in, sorted by node ID.
     * 
     * @param rule an expression rule to be parsed and executed.
     * 
     * @return SortedMap containing all node IDs and node labels selected by the rule.
     * 
     * @exception FilterParseException if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     */
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException;
    
    public Map<String, Set<String>> getIPServiceMap(String rule) throws FilterParseException;
    public List<String> getIPList(String rule) throws FilterParseException;
    public boolean isValid(String addr, String rule) throws FilterParseException;
    
    /**
     * Does this rule match anything in the database?  In particular, does it
     * return at least one record from the database?
     * 
     * @param rule rule to match on
     * @return true if there is at least one match, false otherwise
     */
    public boolean isRuleMatching(String rule);

    public void validateRule(String rule) throws FilterParseException;
    
    public void walkMatchingNodes(String rule, EntityVisitor visitor);

}
