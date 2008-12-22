//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Dec 18, 2008: Created file. - jeffg@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.FileAnticipator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import junit.framework.TestCase;

/**
 * @author jeffg
 *
 */
public abstract class AbstractCollectorTest extends OpenNMSTestCase {

    protected FileAnticipator m_fileAnticipator;
    File m_snmpRrdDirectory;
    protected PlatformTransactionManager m_transMgr;

    protected void persistCollectionSet(CollectionSpecification spec, CollectionSet collectionSet) {
        RrdRepository repository=spec.getRrdRepository("default");
        ServiceParameters params=new ServiceParameters(spec.getReadOnlyPropertyMap());
        BasePersister persister;
        if (Boolean.getBoolean("org.opennms.rrd.storeByGroup")) {
            persister= new GroupPersister(params, repository);
        } else {
            persister= new OneToOnePersister(params, repository);
        }
        collectionSet.visit(persister);
    }
    
    CollectionSpecification createCollectionSpec(String svcName, ServiceCollector svcCollector, String collectionName) {
        Package pkg = new Package();
        Filter filter = new Filter();
        filter.setContent("IPADDR IPLIKE *.*.*.*");
        pkg.setFilter(filter);
        Service service = new Service();
        service.setName(svcName);
        Parameter collectionParm = new Parameter();
        collectionParm.setKey("collection");
        collectionParm.setValue(collectionName);
        service.addParameter(collectionParm);
        pkg.addService(service);

        CollectdPackage wpkg = new CollectdPackage(pkg, "foo", false);
        CollectionSpecification spec = new CollectionSpecification(wpkg,
                                                                   svcName,
                                                                   svcCollector);
        return spec;
    }


    protected void collectNTimes(CollectionSpecification spec, CollectionAgent agent, int numUpdates)
            throws InterruptedException, CollectionException {
                for(int i = 0; i < numUpdates; i++) {
            
                    // now do the actual collection
                    CollectionSet collectionSet=spec.collect(agent);
                    assertEquals("collection status",
                            ServiceCollector.COLLECTION_SUCCEEDED,
                            collectionSet.getStatus());
                    
                    persistCollectionSet(spec, collectionSet);
                
                    System.err.println("COLLECTION "+i+" FINISHED");
                
                    //need a one second time elapse to update the RRD
                    Thread.sleep(1000);
                }
            }

    protected String rrd(String file) {
        return file + RrdUtils.getExtension();
    }

    protected void anticipateFiles(File baseDir, String... fileNames) {
        for (String fileName : fileNames) {
            m_fileAnticipator.expecting(baseDir, fileName);
        }
    }

    protected void anticipateRrdFiles(File baseDir, String... rrdBaseNames) {
        for(String rrdBaseName : rrdBaseNames) {
            m_fileAnticipator.expecting(baseDir, rrd(rrdBaseName));
        }
    }

    protected File anticipatePath(File rootDir, String... pathElements) {
        File parent = rootDir;
        assertTrue(pathElements.length > 0);
        for (String pathElement : pathElements) {
            parent = m_fileAnticipator.expecting(parent, pathElement);
        }
        return parent;
        
    }

    protected CollectionAgent createCollectionAgent(OnmsIpInterface iface) {
        IpInterfaceDao ifDao = EasyMock.createMock(IpInterfaceDao.class);
        EasyMock.expect(ifDao.load(iface.getId())).andReturn(iface).anyTimes();
        EasyMock.replay(ifDao);
        CollectionAgent agent = DefaultCollectionAgent.create(iface.getId(), ifDao, m_transMgr);
        return agent;
    }

    protected File getSnmpRrdDirectory() throws IOException {
        if (m_snmpRrdDirectory == null) {
            m_snmpRrdDirectory = m_fileAnticipator.tempDir("snmp");
        }
        return m_snmpRrdDirectory;
    }

    protected void setFileAnticipator() throws Exception {
        m_fileAnticipator = new FileAnticipator();
    }

    protected void setTransMgr() {
        m_transMgr = new DataSourceTransactionManager(m_db) {
            private static final long serialVersionUID = 1L;
    
            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                super.doCommit(status);
                System.err.println("call to commit a transaction");
            }
    
            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                super.doBegin(transaction, definition);
                System.err.println("Call to begin a transaction");
            }
            
        };
    }
    
    protected void initializeDatabaseSchemaConfig(String databaseSchemaConfig) throws MarshalException, ValidationException, IOException {
        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, databaseSchemaConfig);
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
        rdr.close();
    }

    protected void initializeDataCollectionConfig(String dataCollectionConfig)
            throws IOException, MarshalException, ValidationException,
            RrdException {
                
                Reader rdr = getDataCollectionConfigReader(dataCollectionConfig);
                DataCollectionConfigFactory.setInstance(new DataCollectionConfigFactory(rdr));
                rdr.close();
                
    }

    protected Reader getDataCollectionConfigReader(String classPathLocation)
            throws IOException {
                return ConfigurationTestUtils.getReaderForResourceWithReplacements(this, classPathLocation, new String[] { "%rrdRepository%", getSnmpRrdDirectory().getAbsolutePath() });
    }

}
