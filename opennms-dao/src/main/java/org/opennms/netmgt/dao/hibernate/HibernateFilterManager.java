/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;

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
        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.disableFilter(AUTH_FILTER_NAME);
                return null;
            }
            
        };
        
        m_template.execute(cb);
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
        HibernateCallback<Object> cb = new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.enableFilter(AUTH_FILTER_NAME).setParameterList("userGroups", authorizationGroups);
                return null;
            }
            
        };
        
        m_template.execute(cb);
    }

}
