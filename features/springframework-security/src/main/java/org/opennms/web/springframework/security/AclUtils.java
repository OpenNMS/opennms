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

package org.opennms.web.springframework.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.opennms.netmgt.dao.api.NodeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * AclUtils
 */
public abstract class AclUtils {
    
    /**
     * <p>shouldFilter</p>
     *
     * @return a boolean.
     */
    public static boolean shouldFilter(Collection<? extends GrantedAuthority> authorities) {
    	for (GrantedAuthority authority : authorities) {
    		if (Authentication.ROLE_ADMIN.equals(authority.getAuthority())) {
    			// If the user is in an admin role, then do not filter
    			return false;
    		}
    	}
    	return System.getProperty("org.opennms.web.aclsEnabled", "false").equalsIgnoreCase("true");
    }
    
    public static interface NodeAccessChecker {
        public boolean isNodeAccessible(int nodeId);
    }
    
    /**
     * <p>getNodeAccessChecker</p>
     *
     * @param sc a {@link javax.servlet.ServletContext} object.
     * @return a {@link org.opennms.web.springframework.security.AclUtils.NodeAccessChecker} object.
     */
    public static NodeAccessChecker getNodeAccessChecker(ServletContext sc) {
        
        if (!shouldFilter(SecurityContextHolder.getContext().getAuthentication().getAuthorities())) return new NonFilteringNodeAccessChecker();
        
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
        
        NodeDao dao = (NodeDao) ctx.getBean("nodeDao", NodeDao.class);
        
        return new SetBasedNodeAccessChecker(dao.getNodeIds());
        
    }
    
    /**
     * NonFilteringNodeAccessChecker
     *
     * @author brozow
     */
    private static class NonFilteringNodeAccessChecker implements NodeAccessChecker {

        @Override
        public boolean isNodeAccessible(int nodeId) {
            return true;
        }

    }
    
    private static class SetBasedNodeAccessChecker implements NodeAccessChecker {
        private Set<Integer> m_nodeIds;
        
        public SetBasedNodeAccessChecker(Collection<Integer> nodeIds) {
            m_nodeIds = nodeIds == null ? Collections.<Integer>emptySet() : new HashSet<Integer>(nodeIds);
        }
        
        @Override
        public boolean isNodeAccessible(int nodeId) {
            return m_nodeIds.contains(nodeId);
        }
            
    }



}
