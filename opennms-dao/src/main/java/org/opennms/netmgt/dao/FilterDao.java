/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created February 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.model.EntityVisitor;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
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
