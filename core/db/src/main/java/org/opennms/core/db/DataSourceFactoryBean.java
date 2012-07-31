/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import javax.sql.DataSource;

import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import org.opennms.core.resource.Vault;

/**
 * <p>DataSourceFactoryBean class.</p>
 */
public class DataSourceFactoryBean implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

    /**
     * <p>getObject</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public DataSource getObject() throws Exception {
        return DataSourceFactory.getDataSource();
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    @Override
    public Class<? extends DataSource> getObjectType() {
        return (DataSourceFactory.getDataSource() == null ? DataSource.class : DataSourceFactory.getDataSource().getClass());
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        DataSourceFactory.init();
        Vault.setDataSource(DataSourceFactory.getInstance()); // Fix for Bug 4117
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
        log().info("Closing DataSourceFactory!!!");
        DataSourceFactory.close();
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
