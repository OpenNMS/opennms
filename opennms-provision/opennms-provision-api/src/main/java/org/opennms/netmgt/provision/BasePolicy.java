/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


public abstract class BasePolicy<T> {
    
    public static enum Match { ANY_PARAMETER, ALL_PARAMETERS, NO_PARAMETERS }


    private Match m_match = Match.ANY_PARAMETER;
    private final LinkedHashMap<String, String> m_criteria = new LinkedHashMap<String, String>();;


    protected boolean match(String s, String matcher) {
        if (s == null) {
            return false;
        }
        if (matcher.startsWith("~")) {
            matcher = matcher.replaceFirst("~", "");
            return s.matches(matcher);
        } else {
            return s.equals(matcher);
        }
    }


    @Require( { "ANY_PARAMETER", "ALL_PARAMETERS", "NO_PARAMETERS" })
    public String getMatchBehavior() {
        return getMatch().toString();
    }


    public void setMatchBehavior(String matchBehavior) {
        if (matchBehavior != null && matchBehavior.toUpperCase().contains("ALL")) {
            setMatch(Match.ALL_PARAMETERS);
        } else if (matchBehavior != null && matchBehavior.toUpperCase().contains("NO")) {
            setMatch(Match.NO_PARAMETERS);
        } else {
            setMatch(Match.ANY_PARAMETER);
        }
    }


    /**
     * @param match the match to set
     */
    protected void setMatch(Match match) {
        m_match = match;
    }


    /**
     * @return the match
     */
    protected Match getMatch() {
        return m_match;
    }


    protected String getCriteria(String key) {
        return getCriteria().get(key);
    }


    protected String putCriteria(String key, String expression) {
        return getCriteria().put(key, expression);
    }


    /**
     * @return the criteria
     */
    protected LinkedHashMap<String, String> getCriteria() {
        return m_criteria;
    }


    protected boolean matches(T iface) {
        
        switch (getMatch()) {
        case ALL_PARAMETERS: 
            return matchAll(iface);
        case NO_PARAMETERS:
            return matchNone(iface);
        case ANY_PARAMETER:
        default:
            return matchAny(iface);
        }                
    
    }


    private boolean matchAll(T iface) {
        BeanWrapper bean = new BeanWrapperImpl(iface);
        
        for(Entry<String, String> term : getCriteria().entrySet()) {
            
            String val = getPropertyValueAsString(bean, term.getKey());
            String matchExpression = term.getValue();
            
            if (!match(val, matchExpression)) {
                return false;
            }
        }
        
        return true;
        
    
    }


    private boolean matchAny(T iface) {
        BeanWrapper bean = new BeanWrapperImpl(iface);
        
        for(Entry<String, String> term : getCriteria().entrySet()) {
            
            String val = getPropertyValueAsString(bean, term.getKey());
            String matchExpression = term.getValue();
            
            if (match(val, matchExpression)) {
                return true;
            }
        }
        
        return false;
    }


    private boolean matchNone(T iface) {
        return !matchAny(iface);
    }


    private String getPropertyValueAsString(BeanWrapper bean, String propertyName) {
        return (String) bean.convertIfNecessary(bean.getPropertyValue(propertyName), String.class);
    }


    public abstract T act(T iface);


    public T apply(T iface) {
        if (iface == null) {
            return null;
        }
        
        if (matches(iface)) {
            debug("Found Match %s for %s", iface, this);
            return act(iface);
        }
        
        debug("No Match Found: %s for %s", iface, this);
        return iface;
    }
    
    protected void debug(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
    protected void info(String format, Object... args) {
        Logger log = ThreadCategory.getInstance(getClass());
        
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args));
        }
    }
}
