//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact: 
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
//Tab Size = 8

package org.opennms.netmgt.importer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.SelfDescribing;
import org.jmock.core.constraint.And;
import org.opennms.netmgt.config.modelimport.Interface;
import org.opennms.netmgt.config.modelimport.MonitoredService;
import org.opennms.netmgt.config.modelimport.Node;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.importer.operations.ImportOperationsManager;
import org.opennms.netmgt.importer.specification.AbstractImportVisitor;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
public class AbstractMockTestCase extends MockObjectTestCase {
    
    private NodeDao m_nodeDao;
    private DistPollerDao m_distPollerDao;
    private ServiceTypeDao m_svcTypeDao;
    private CategoryDao m_categoryDao;
    private HashMap m_nodeCache = new HashMap();
    private Mock m_mockNodeDao;
    private Mock m_mockCategoryDao;
    private Map m_svcTypes = new HashMap();
    private int m_ids;
    private Mock m_svcTypeMock;
    
    protected void setUp() throws Exception {
        m_mockNodeDao = mock(NodeDao.class);
        m_nodeDao = (NodeDao)m_mockNodeDao.proxy();

        Mock dpMock =  mock(DistPollerDao.class);
        m_distPollerDao = (DistPollerDao)dpMock.proxy();
        dpMock.stubs().method("get").with(eq("localhost")).will(returnValue(new OnmsDistPoller("localhost", "127.0.0.1")));

        m_svcTypeMock = mock(ServiceTypeDao.class);
        m_svcTypeDao = (ServiceTypeDao)m_svcTypeMock.proxy();
        setupServiceType(m_svcTypeMock, "ICMP");
        setupServiceType(m_svcTypeMock, "SNMP");
        
        m_mockCategoryDao = mock(CategoryDao.class);
        m_categoryDao = (CategoryDao)m_mockCategoryDao.proxy();
        
    }
    
    public void testNothing() {
        
    }
    
    protected DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }
    
    protected NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    protected ServiceTypeDao getServiceTypeDao() {
        return m_svcTypeDao;
    }
    
    protected OnmsNode createNode(int nodeId) {
        Integer key = new Integer(nodeId);
        OnmsNode node = (OnmsNode)m_nodeCache.get(key);
        if (node == null) {
            node = new OnmsNode();
            node.setId(key);
            m_nodeCache.put(key, node);
        }
        return node;
    }
    
    private Constraint hasInterfaceCount(int ifaceCount) {
        Association ifaces = new Association("ipInterfaces", "getIpInterfaces");
        return new HasAssociationOfSize(ifaces, ifaceCount);
    }
    
    protected OnmsServiceType getInternedServiceType(String svcName) {
        OnmsServiceType svcType = new OnmsServiceType(svcName);
        svcType.setId(new Integer(++m_ids));
        m_svcTypes.put(svcName, svcType);
        return svcType;
    }
    
    protected void expectServiceTypeCreate(String svcName) {
        m_svcTypeMock.expects(once()).method("findByName").with(eq("HTTP")).will(returnValue(null));
        m_svcTypeMock.expects(once()).method("save").with(hasPropertyWithValue("name", svcName)).isVoid();
        m_svcTypeMock.stubs().method("findByName").with(eq("HTTP")).after("save").will(returnValue(getInternedServiceType(svcName)));
        
    }
    
    protected void setDeleteExpectations(int nodeId) {
        expectGetForNode(nodeId);
        expectDeleteForNode(nodeId);
    }
    
    private Constraint hasPropertyWithValue(final String propertyName, final Object propertyValue) {
        
        Constraint c = new Constraint() {
            
            public boolean eval(Object arg0) {
                try {
                    return propertyValue.equals(PropertyUtils.getProperty(arg0, propertyName));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            public StringBuffer describeTo(StringBuffer buffer) {
                buffer.append("has "+propertyName+" of value ");
                buffer.append(propertyValue);
                return buffer;
            }    
        };
        return c;
    }
    
    private Constraint hasInterfaceSuchThat(Constraint ifaceConstraint) {
        Association ifaces = new Association("ipInterfaces", "getIpInterfaces");
        return new ExistsSuchThat(ifaces, and(isA(OnmsIpInterface.class), ifaceConstraint));
    }
    
    private Constraint hasServiceOfType(String svcName) {
        Association svc = new Association("monitoredServices", "getMonitoredServices");
        return new ExistsSuchThat(svc, hasPropertyWithValue("serviceType.name", svcName));
    }
    
    private Constraint hasServiceCount(int svcCount) {
        Association svc = new Association("monitoredServices", "getMonitoredServices");
        return new HasAssociationOfSize(svc, svcCount);
    }
    
    private void expectDeleteForNode(int nodeId) {
        m_mockNodeDao.expects(once()).method("delete").with(same(createNode(nodeId)));
    }
    
    private void expectGetForNode(int nodeId) {
        m_mockNodeDao.expects(once()).method("get").with(eq(new Long(nodeId))).will(returnValue(createNode(nodeId)));
    }
    
    protected Map getAssetNumberMap() {
        Map assetNumberMap = new HashMap();
        assetNumberMap.put(AssetRecordDao.IMPORTED_ID+"1", new Long(1));
        assetNumberMap.put(AssetRecordDao.IMPORTED_ID+"2", new Long(2));
        assetNumberMap.put(AssetRecordDao.IMPORTED_ID+"3", new Long(3));
        assetNumberMap.put(AssetRecordDao.IMPORTED_ID+"4", new Long(4));
        return assetNumberMap;
    }
    
    protected ModelImporter getModelImporter() {
        ModelImporter mi = new ModelImporter();
        mi.setDistPollerDao(getDistPollerDao());
        mi.setNodeDao(getNodeDao());
        mi.setServiceTypeDao(getServiceTypeDao());
        mi.setCategoryDao(getCategoryDao());
        return mi;
    }
    
    protected void setupServiceType(Mock svcTypeMock, String svcName) {
        
        OnmsServiceType svcType = getInternedServiceType(svcName);
        svcTypeMock.stubs().method("findByName").with(eq(svcName)).will(returnValue(svcType));
        
    }
    
    public class Association implements SelfDescribing {
        
        private String m_methodName;
        private String m_assocName;
        
        Association(String assocName, String methodName) {
            m_assocName = assocName;
            m_methodName = methodName;
        }
        
        public Collection getAssociated(Object o) {
            try {
                return (Collection)MethodUtils.invokeMethod(o, m_methodName, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append(m_assocName);
            return buffer;
        }
        
    }
    
    public class ExistsSuchThat implements Constraint {
        
        Association m_association;
        Constraint m_existsSuchThat;
        
        ExistsSuchThat(Association assocation, Constraint existsSuchThat) {
            m_association = assocation;
            m_existsSuchThat = existsSuchThat;
        }
        
        public boolean eval(Object o) {
            Collection associated = m_association.getAssociated(o);
            for (Iterator it = associated.iterator(); it.hasNext();) {
                Object assocItem = (Object) it.next();
                if (m_existsSuchThat.eval(assocItem))
                    return true;
            }
            return false;
        }
        
        public StringBuffer describeTo(StringBuffer buffer) {
            
            buffer.append("there exists a member of ");
            m_association.describeTo(buffer);
            buffer.append(" such that ");
            m_existsSuchThat.describeTo(buffer);
            return buffer;
            
        }
        
    }
    
    public class ExpectationsVisitor extends AbstractImportVisitor {
        
        List m_currentNodeConstraints;
        int m_ifaceCount;
        List m_currentIfaceConstraints;
        Map m_assetNumToNodeMap;
        private int m_svcCount;
        
        ExpectationsVisitor(Map assetNumToNodeMap) {
            m_assetNumToNodeMap = assetNumToNodeMap;
        }
        
        public void visitNode(Node node) {
            m_currentNodeConstraints = new LinkedList();
            
            m_currentNodeConstraints.add(isA(OnmsNode.class));
            m_currentNodeConstraints.add(hasPropertyWithValue("assetRecord.assetNumber", ImportOperationsManager.getAssetNumber(node.getForeignId())));
            m_currentNodeConstraints.add(hasPropertyWithValue("label", node.getNodeLabel()));
            
            m_ifaceCount = 0;
            
        }
        
        public void visitInterface(Interface iface) {
            m_currentIfaceConstraints = new LinkedList();
            m_ifaceCount++;
            m_svcCount = 0;
            
            m_currentIfaceConstraints.add(hasPropertyWithValue("isManaged", iface.getStatus() == 3 ? "U" : "M"));
            m_currentIfaceConstraints.add(hasPropertyWithValue("ipAddress", iface.getIpAddr()));
            m_currentIfaceConstraints.add(hasPropertyWithValue("isSnmpPrimary", iface.getSnmpPrimary()));
            m_currentIfaceConstraints.add(hasPropertyWithValue("ipStatus", (iface.getStatus() == 3 ? new Integer(3) : new Integer(1))));
            
        }
        
        public void visitMonitoredService(MonitoredService svc) {
            m_svcCount++;
            m_currentIfaceConstraints.add(hasServiceOfType(svc.getServiceName()));
        }
        
        public void completeInterface(Interface iface) {
            m_currentIfaceConstraints.add(hasServiceCount(m_svcCount));
            Constraint ifaceConstraint = andAll(m_currentIfaceConstraints);
            m_currentNodeConstraints.add(hasInterfaceSuchThat(ifaceConstraint));
            m_currentIfaceConstraints = null;
            m_svcCount = 0;
            
        }
        
        public void completeNode(Node node) {
            m_currentNodeConstraints.add(hasInterfaceCount(m_ifaceCount));
            m_ifaceCount = 0;
            if (isExisting(node)) {
                //m_mockNodeDao.expects(once()).method("update").with(getNodeConstraints());
            } else {
                m_mockNodeDao.expects(once()).method("save").with(getNodeConstraints());
            }
        }
        
        private Constraint getNodeConstraints() {
            LinkedList constraints = new LinkedList(m_currentNodeConstraints);
            return andAll(constraints);
        }
        
        private Constraint andAll(List constraints) {
            if (constraints.isEmpty())
                return ANYTHING;
            
            if (constraints.size() == 1)
                return (Constraint)constraints.get(0);
            
            Constraint first = (Constraint)constraints.get(0);
            Constraint rest = andAll(constraints.subList(1, constraints.size()));
            return new And(first, rest);
        }
        
        private boolean isExisting(Node node) {
            return m_assetNumToNodeMap.containsKey(ImportOperationsManager.getAssetNumber(node.getForeignId()));
        }
        
        
    }
    
    public class HasAssociationOfSize implements Constraint {
        Association m_association;
        int m_size;
        HasAssociationOfSize(Association association, int size) {
            m_association = association;
            m_size = size;
        }
        public boolean eval(Object o) {
            return m_association.getAssociated(o).size() == m_size;
        }
        public StringBuffer describeTo(StringBuffer buffer) {
            buffer.append("has ");
            m_association.describeTo(buffer);
            buffer.append(" of size ");
            buffer.append(m_size);
            return buffer;
        }
    }

	public CategoryDao getCategoryDao() {
		return m_categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		m_categoryDao = categoryDao;
	}
}
