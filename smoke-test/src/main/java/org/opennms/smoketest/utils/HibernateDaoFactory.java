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
package org.opennms.smoketest.utils;

import java.net.InetSocketAddress;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Quick access to Hibernate DAOs.
 *
 * @author jwhite
 */
public class HibernateDaoFactory {

    private final SessionFactory m_sessionFactory;
    private final HibernateTemplate m_hibernateTemplate;

    public HibernateDaoFactory(InetSocketAddress pgsqlAddr) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + pgsqlAddr.getHostString() + ":" + pgsqlAddr.getPort() + "/opennms");
        config.setUsername("opennms");
        config.setPassword("opennms");
        HikariDataSource ds = new HikariDataSource(config);

        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(ds);
        sfb.setPackagesToScan("org.opennms.netmgt.model",
                              "org.opennms.features.deviceconfig.persistence.api");
        try {
            sfb.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }
}
