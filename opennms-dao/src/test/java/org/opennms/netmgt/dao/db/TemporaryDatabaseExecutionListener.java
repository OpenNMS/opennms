/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.dao.db;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.opennms.netmgt.config.DataSourceFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * TemporaryDatabaseExecutionListener
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class TemporaryDatabaseExecutionListener extends AbstractTestExecutionListener {

    public void afterTestMethod(TestContext testContext) throws Exception {
        System.err.printf("TemporaryDatabaseExecutionListener.afterTestMethod(%s)\n", testContext);
        
        DataSource dataSource = DataSourceFactory.getInstance();
        TemporaryDatabase tempDb = findTemporaryDatabase(dataSource);
        if (tempDb != null) {
            tempDb.drop();
        }
        
        testContext.markApplicationContextDirty();
        testContext.setAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE, Boolean.TRUE);

    }
    
    private TemporaryDatabase findTemporaryDatabase(DataSource dataSource) {
        if (dataSource instanceof TemporaryDatabase) {
            return (TemporaryDatabase) dataSource;
        } else if (dataSource instanceof DelegatingDataSource) {
            return findTemporaryDatabase(((DelegatingDataSource)dataSource).getTargetDataSource());
        } else {
            return null;
        }
        
    }

    private JUnitTemporaryDatabase findAnnotation(TestContext testContext) {
        JUnitTemporaryDatabase jtd = null;
        Method testMethod = testContext.getTestMethod();
        if (testMethod != null) {
            jtd = testMethod.getAnnotation(JUnitTemporaryDatabase.class);
        }
        if (jtd == null) {
            Class<?> testClass = testContext.getTestClass();
            jtd = testClass.getAnnotation(JUnitTemporaryDatabase.class);
        }
        return jtd;
    }
    
    public void beforeTestMethod(TestContext testContext) throws Exception {
        System.err.printf("TemporaryDatabaseExecutionListener.beforeTestMethod(%s)\n", testContext);
    }

    public void prepareTestInstance(TestContext testContext) throws Exception {
        System.err.printf("TemporaryDatabaseExecutionListener.prepareTestInstance(%s)\n", testContext);
        JUnitTemporaryDatabase jtd = findAnnotation(testContext);
        String dbName = getDatabaseName(testContext);
        TemporaryDatabase dataSource = (jtd == null ? new TemporaryDatabase(dbName) : (jtd.tempDbClass()).getConstructor(String.class).newInstance(dbName));
        dataSource.setPopulateSchema(jtd == null? true : jtd.populate());
        dataSource.create();
        
        LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy(dataSource);
        
        DataSourceFactory.setInstance(proxy);
    }

    private String getDatabaseName(TestContext testContext) {
        return String.format("opennms_test_%s", System.currentTimeMillis());
    }

}
