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
package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Arrays;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.opennms.netmgt.model.FilterManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * <p>HibernateFilterManager class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class HibernateFilterManager implements FilterManager {
    
    private HibernateTemplate m_template;
    private String[] m_authorizationGroups;
    
    
    /**
     * <p>setSessionFactory</p>
     *
     * @param sessionFactory a {@link org.hibernate.SessionFactory} object.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        m_template = new HibernateTemplate(sessionFactory);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.model.FilterManager#disableAuthorizationFilter()
     */
    /**
     * <p>disableAuthorizationFilter</p>
     */
    @Override
    public void disableAuthorizationFilter() {
        m_authorizationGroups = null;
        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.disableFilter(AUTH_FILTER_NAME);
                return null;
            }
            
        };
        
        m_template.execute(cb);
    }

    @Override
    public String[] getAuthorizationGroups() {
        return m_authorizationGroups;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isEnabled() {
        return m_authorizationGroups != null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.model.FilterManager#enableAuthorizationFilter(java.lang.String[])
     */
    /**
     * <p>enableAuthorizationFilter</p>
     *
     * @param authorizationGroups an array of {@link java.lang.String} objects.
     */
    @Override
    public void enableAuthorizationFilter(final String[] authorizationGroups) {
        m_authorizationGroups = Arrays.copyOf(authorizationGroups, authorizationGroups.length);
        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.enableFilter(AUTH_FILTER_NAME).setParameterList("userGroups", m_authorizationGroups);
                return null;
            }
            
        };
        
        m_template.execute(cb);
    }

}
