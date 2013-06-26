/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.annotations.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.PropertyAccessorFactory;


/**
 * <p>Abstract BasePolicy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class BasePolicy<T> {
    
    private static final Logger LOG = LoggerFactory.getLogger(BasePolicy.class);
    
    public static enum Match { ANY_PARAMETER, ALL_PARAMETERS, NO_PARAMETERS }


    private Match m_match = Match.ANY_PARAMETER;
    private final LinkedHashMap<String, String> m_criteria = new LinkedHashMap<String, String>();;


    /**
     * <p>match</p>
     *
     * @param s a {@link java.lang.String} object.
     * @param matcher a {@link java.lang.String} object.
     * @param <T> a T object.
     * @return a boolean.
     */
    protected boolean match(final String s, final String matcher) {
        if (s == null) {
            return false;
        }
        if (matcher.startsWith("~")) {
            return s.matches(matcher.replaceFirst("~", ""));
        } else {
            return s.equals(matcher);
        }
    }


    /**
     * <p>getMatchBehavior</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require( { "ANY_PARAMETER", "ALL_PARAMETERS", "NO_PARAMETERS" })
    public String getMatchBehavior() {
        return getMatch().toString();
    }


    /**
     * <p>setMatchBehavior</p>
     *
     * @param matchBehavior a {@link java.lang.String} object.
     */
    public void setMatchBehavior(final String matchBehavior) {
        final String upperMatchBehavior = matchBehavior.toUpperCase();
        if (matchBehavior != null && upperMatchBehavior.contains("ALL")) {
            setMatch(Match.ALL_PARAMETERS);
        } else if (matchBehavior != null && upperMatchBehavior.contains("NO")) {
            setMatch(Match.NO_PARAMETERS);
        } else {
            setMatch(Match.ANY_PARAMETER);
        }
    }


    /**
     * <p>setMatch</p>
     *
     * @param match the match to set
     */
    protected void setMatch(final Match match) {
        m_match = match;
    }


    /**
     * <p>getMatch</p>
     *
     * @return the match
     */
    protected Match getMatch() {
        return m_match;
    }


    /**
     * <p>getCriteria</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getCriteria(final String key) {
        return getCriteria().get(key);
    }


    /**
     * <p>putCriteria</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param expression a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected String putCriteria(final String key, final String expression) {
        return getCriteria().put(key, expression);
    }


    /**
     * <p>getCriteria</p>
     *
     * @return the criteria
     */
    protected LinkedHashMap<String, String> getCriteria() {
        return m_criteria;
    }


    /**
     * <p>matches</p>
     *
     * @param iface a T object.
     * @return a boolean.
     */
    protected boolean matches(final T iface) {
        
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

    private boolean matchAll(final T iface) {
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(iface);

        for(final Entry<String, String> term : getCriteria().entrySet()) {
            
            final String val = getPropertyValueAsString(bean, term.getKey());
            final String matchExpression = term.getValue();
            
            if (!match(val, matchExpression)) {
                return false;
            }
        }
        
        return true;
        
    
    }


    private boolean matchAny(final T iface) {
        final BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(iface);
        
        for(final Entry<String, String> term : getCriteria().entrySet()) {
            
            final String val = getPropertyValueAsString(bean, term.getKey());
            final String matchExpression = term.getValue();
            
            if (match(val, matchExpression)) {
                return true;
            }
        }
        
        return false;
    }


    private boolean matchNone(final T iface) {
        return !matchAny(iface);
    }


    private static String getPropertyValueAsString(final BeanWrapper bean, final String propertyName) {
        Object value = null;
        try {
            value = bean.getPropertyValue(propertyName);
        } catch (BeansException e) {
            LOG.warn("Could not find property \"{}\" on object of type {}, returning null", propertyName, bean.getWrappedClass().getName(), e);
            return null;
        }
        try {
            return bean.convertIfNecessary(value, String.class);
        } catch (ConversionNotSupportedException e) {
            if (value instanceof InetAddress) {
                return InetAddressUtils.toIpAddrString((InetAddress) value);
            } else {
                throw e;
            }
        }
    }


    /**
     * <p>act</p>
     *
     * @param iface a T object.
     * @return a T object.
     */
    public abstract T act(final T iface);


    /**
     * <p>apply</p>
     *
     * @param iface a T object.
     * @return a T object.
     */
    public T apply(final T iface) {
        if (iface == null) {
            return null;
        }
        
        if (matches(iface)) {
            // TODO add MDC log info for resource at hand
            LOG.debug("Found Match {} for {}", iface, this);
            return act(iface);
        }
        
        // TODO add MDC log info for resource at hand
        LOG.debug("No Match Found: {} for {}", iface, this);
        return iface;
    }
}
