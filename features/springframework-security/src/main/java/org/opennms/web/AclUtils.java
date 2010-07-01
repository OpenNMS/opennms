/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.opennms.netmgt.dao.NodeDao;
import org.springframework.context.ApplicationContext;
import org.springframework.security.util.AuthorityUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;



/**
 * AclUtils
 *
 * @author brozow
 * @version $Id: $
 */
public class AclUtils {
    
    /**
     * <p>shouldFilter</p>
     *
     * @return a boolean.
     */
    public static boolean shouldFilter() {
        return System.getProperty("org.opennms.web.aclsEnabled", "false").equalsIgnoreCase("true") 
            && !AuthorityUtils.userHasAuthority("ROLE_ADMIN");
    }
    
    public static interface NodeAccessChecker {
        public boolean isNodeAccessible(int nodeId);
    }
    
    /**
     * <p>getNodeAccessChecker</p>
     *
     * @param sc a {@link javax.servlet.ServletContext} object.
     * @return a {@link org.opennms.web.AclUtils.NodeAccessChecker} object.
     */
    public static NodeAccessChecker getNodeAccessChecker(ServletContext sc) {
        
        if (!shouldFilter()) return new NonFilteringNodeAccessChecker();
        
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

        public boolean isNodeAccessible(int nodeId) {
            return true;
        }

    }
    
    private static class SetBasedNodeAccessChecker implements NodeAccessChecker {
        private Set<Integer> m_nodeIds;
        
        public SetBasedNodeAccessChecker(Collection<Integer> nodeIds) {
            m_nodeIds = nodeIds == null ? Collections.<Integer>emptySet() : new HashSet<Integer>(nodeIds);
        }
        
        public boolean isNodeAccessible(int nodeId) {
            return m_nodeIds.contains(nodeId);
        }
            
    }



}
