/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.smoketest.utils;

import java.net.InetSocketAddress;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;

import jersey.repackaged.com.google.common.base.Throwables;

/**
 * Quick access to Hibernate DAOs.
 *
 * @author jwhite
 */
public class HibernateDaoFactory {

    private final SessionFactory m_sessionFactory;
    private final HibernateTemplate m_hibernateTemplate;

    public HibernateDaoFactory(InetSocketAddress pgsqlAddr) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setPortNumber(pgsqlAddr.getPort());
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        dataSource.setServerName(pgsqlAddr.getAddress().getHostAddress());
        dataSource.setDatabaseName("opennms");

        AnnotationSessionFactoryBean sfb = new AnnotationSessionFactoryBean();
        sfb.setDataSource(dataSource);
        sfb.setPackagesToScan("org.opennms.netmgt.model");
        try {
            sfb.afterPropertiesSet();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        m_sessionFactory = sfb.getObject();
        m_hibernateTemplate = new HibernateTemplate(m_sessionFactory);
    }

    public <T extends AbstractDaoHibernate<?, ?>> T getDao(Class<T> clazz) {
        try {
            T dao = clazz.newInstance();
            dao.setHibernateTemplate(m_hibernateTemplate);
            dao.setSessionFactory(m_sessionFactory);
            return dao;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
