/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.springframework.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.web.api.Authentication;
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
        
        NodeDao dao = ctx.getBean("nodeDao", NodeDao.class);
        
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
