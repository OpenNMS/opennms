//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.secret.dao;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.opennms.secret.dao.impl.MemberDAOHibernate;
import org.opennms.secret.model.OGPMember;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class MemberDAOTest extends TestCase {

    private class RunTestInTransaction implements TransactionCallback {
        public Object doInTransaction(TransactionStatus ts) {
            try {
                MemberDAOTest.super.runTest();
            } catch (Throwable e) {
                ts.setRollbackOnly();
            }
            return null;
        }
    }


    private MemberDAO m_dao;
    private SessionFactory m_factory;
    private LocalSessionFactoryBean m_lsfb;
    private BasicDataSource m_dataSource;
    private HibernateTransactionManager m_transMgr;
    private TransactionTemplate m_transTemplate;

    protected void FIXMEsetUp() throws Exception {
        m_dataSource = new BasicDataSource();
        m_dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        m_dataSource.setUrl("jdbc:hsqldb:database/toolset.db");
        m_dataSource.setUsername("SA");
        m_dataSource.setPassword("");
        
        m_lsfb = new LocalSessionFactoryBean();
        m_lsfb.setDataSource(m_dataSource);
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        m_lsfb.setHibernateProperties(props);
        m_lsfb.setMappingResources(new String[] {"org/opennms/secret/model/OGPMember.hbm.xml"});
        m_lsfb.afterPropertiesSet();
        
        m_lsfb.createDatabaseSchema();
        
        m_factory = (SessionFactory)m_lsfb.getObject();
        
        m_transMgr = new HibernateTransactionManager();
        m_transMgr.setSessionFactory(m_factory);
        m_transMgr.afterPropertiesSet();
        
        m_transTemplate = new TransactionTemplate();
        m_transTemplate.setTransactionManager(m_transMgr);
        
        MemberDAOHibernate dao = new MemberDAOHibernate();
        dao.setSessionFactory(m_factory);
        
        m_dao = dao;
    }

    protected void tearDown() throws Exception {
    }
    
    protected void FIXMErunTest() throws Throwable {
        m_transTemplate.execute(new RunTestInTransaction());
    }

    public void testBogus() {
        // Empty test so that JUnit doesn't complain about not having any tests
    }

    public void FIXMEtestCreate() throws InterruptedException {
        OGPMember member = new OGPMember();
        member.setFirstName("David");
        member.setLastName("Hustace");
        m_dao.createMember(member);
        assertNotNull(member.getId());
        
        OGPMember lookup = m_dao.getMember(member.getId());
        assertEquals(member.getFirstName(), lookup.getFirstName());
        assertEquals(member.getLastName(), lookup.getLastName());
    }

    
    

}
