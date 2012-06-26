package org.opennms.netmgt.dao.hibernate;

import java.util.Date;

import junit.framework.Assert;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.InventoryAssetDao;
import org.opennms.netmgt.dao.InventoryAssetPropertyDao;
import org.opennms.netmgt.dao.InventoryCategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryAssetProperty;
import org.opennms.netmgt.model.inventory.OnmsInventoryCategory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class InventoryDatabasePopulator {
    private NodeDao m_nodeDao;
    private EventDao m_eventDao;
    private CategoryDao m_categoryDao;
    private DistPollerDao m_distPollerDao;
    private ServiceTypeDao m_serviceTypeDao;
    
    private InventoryCategoryDao m_inventoryCategoryDao;
    private InventoryAssetDao m_inventoryAssetDao;	
    private InventoryAssetPropertyDao m_inventoryAssetPropertyDao;
	
    private OnmsNode m_node1;
    private OnmsInventoryAsset m_invAsset1;
    OnmsInventoryCategory m_inventoryCategory1;

    private TransactionTemplate m_transTemplate;

    private static boolean POPULATE_DATABASE_IN_SEPARATE_TRANSACTION = true;
    
    public void populateDatabase() {
        if (POPULATE_DATABASE_IN_SEPARATE_TRANSACTION) {
            m_transTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(final TransactionStatus status) {
                    doPopulateDatabase();
                    return null;
                }
            });
        } else {
            doPopulateDatabase();
        }
    }
    
    public void doPopulateDatabase() {
        OnmsDistPoller distPoller = getDistPoller("localhost", "127.0.0.1");
        
        OnmsCategory ac = getCategory("DEV_AC");
        OnmsCategory mid = getCategory("IMP_mid");
        OnmsCategory ops = getCategory("OPS_Online");
        
        OnmsCategory catRouter = getCategory("Routers");
        @SuppressWarnings("unused")
        OnmsCategory catSwitches = getCategory("Switches");
        OnmsCategory catServers = getCategory("Servers");
        getCategory("Production");
        getCategory("Test");
        getCategory("Development");
        
        getServiceType("ICMP");
        getServiceType("SNMP");
        getServiceType("HTTP");        

        NetworkBuilder builder = new NetworkBuilder(distPoller);
        
        setNode1(builder.addNode("test1.availability.opennms.org").
                 setId(1).
                 setType("A").
                 getNode());
        Assert.assertNotNull("newly built node 1 should not be null", getNode1());
        builder.addCategory(ac);
        builder.addCategory(mid);
        builder.addCategory(ops);
        builder.addCategory(catRouter); 
        builder.setBuilding("HQ");
        builder.addInterface("192.168.100.1").setIsManaged("M");
        //getNodeDao().save(builder.getCurrentNode());
        //getNodeDao().flush();
        builder.addService(getServiceType("ICMP")).setStatus("A");
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        builder.addNode("test2.availability.opennms.org").
            setId(2).
            //setForeignSource("imported:").
            
            //setForeignId("2").
            setType("A");
        builder.addCategory(mid);
        builder.addCategory(catServers);
        builder.setBuilding("HQ");
        builder.addInterface("192.168.100.2").setIsManaged("M").setIsSnmpPrimary("P");
        builder.addService(getServiceType("ICMP")).setStatus("A");
        //builder.addService(getServiceType("SNMP")).setStatus("A");;
        builder.addInterface("192.168.100.3").setIsManaged("M");
        builder.addService(getServiceType("ICMP")).setStatus("A");
        //builder.addService(getServiceType("HTTP")).setStatus("A");
        getNodeDao().save(builder.getCurrentNode());
        getNodeDao().flush();
        
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(distPoller);
        event.setEventUei("uei.opennms.org/test");
        event.setEventTime(new Date());
        event.setEventSource("test");
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        getEventDao().save(event);
        getEventDao().flush();
       
        // Create an inventory category.
        OnmsInventoryCategory invCat = new OnmsInventoryCategory("Network Equipment");
        getInventoryCategoryDao().save(invCat);
        getInventoryCategoryDao().flush();

        // Create an inventory asset within the previous inventory category, associated with node1.
        OnmsInventoryAsset invAsset = new OnmsInventoryAsset(invCat, "Network Card", getNode1());
        invAsset.setEffectiveDate(new Date());
        

        // Create an inventory asset properties and assign it to the previous asset.
        OnmsInventoryAssetProperty invAssetProp = new OnmsInventoryAssetProperty(
                "manufacturer",
                "Intel");
        invAssetProp.setEffectiveDate(new Date());
        invAsset.addProperty(invAssetProp);
        
        OnmsInventoryAssetProperty invAssetProp2 = new OnmsInventoryAssetProperty(
                "serialnum",
                "3235488862NB92");
        invAssetProp2.setEffectiveDate(new Date());
        invAsset.addProperty(invAssetProp2);
        
        getInventoryAssetDao().save(invAsset);
        getInventoryAssetDao().flush();
        setInvAsset1(invAsset);
    }

    private OnmsCategory getCategory(String categoryName) {
        OnmsCategory cat = getCategoryDao().findByName(categoryName);
        if (cat == null) {
            cat = new OnmsCategory(categoryName);
            cat.getAuthorizedGroups().add(categoryName+"Group");
            getCategoryDao().save(cat);
            getCategoryDao().flush();
        }
        return cat;
    }

    private OnmsDistPoller getDistPoller(String localhost, String localhostIp) {
        OnmsDistPoller distPoller = getDistPollerDao().get(localhost);
        if (distPoller == null) {
            distPoller = new OnmsDistPoller(localhost, localhostIp);
            getDistPollerDao().save(distPoller);
            getDistPollerDao().flush();
        }
        return distPoller;
    }

    private OnmsServiceType getServiceType(String name) {
        OnmsServiceType serviceType = getServiceTypeDao().findByName(name);
        if (serviceType == null) {
            serviceType = new OnmsServiceType(name);
            getServiceTypeDao().save(serviceType);
            getServiceTypeDao().flush();
        }
        return serviceType;
    }

    public EventDao getEventDao() {
        return m_eventDao;
    }


    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }
    
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }


    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
        
    public OnmsNode getNode1() {
        return m_node1;
    }
    
    private void setNode1(OnmsNode node1) {
        m_node1 = node1;
    }

    public CategoryDao getCategoryDao() {
		return m_categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		m_categoryDao = categoryDao;
	}

	public DistPollerDao getDistPollerDao() {
		return m_distPollerDao;
	}

	public void setDistPollerDao(DistPollerDao distPollerDao) {
		m_distPollerDao = distPollerDao;
	}

	public ServiceTypeDao getServiceTypeDao() {
		return m_serviceTypeDao;
	}

	public void setServiceTypeDao(ServiceTypeDao serviceTypeDao) {
		m_serviceTypeDao = serviceTypeDao;
	}

	public InventoryCategoryDao getInventoryCategoryDao() {
		return m_inventoryCategoryDao;
	}

	public void setInventoryCategoryDao(InventoryCategoryDao inventoryCategoryDao) {
		m_inventoryCategoryDao = inventoryCategoryDao;
	}

	public InventoryAssetDao getInventoryAssetDao() {
		return m_inventoryAssetDao;
	}

	public void setInventoryAssetDao(InventoryAssetDao inventoryAssetDao) {
		m_inventoryAssetDao = inventoryAssetDao;
	}

	public InventoryAssetPropertyDao getInventoryAssetPropertyDao() {
		return m_inventoryAssetPropertyDao;
	}

	public void setInventoryAssetPropertyDao(InventoryAssetPropertyDao inventoryAssetPropertyDao) {
		m_inventoryAssetPropertyDao = inventoryAssetPropertyDao;
	}
	public OnmsInventoryAsset getInvAsset1() {
		return m_invAsset1;
	}
	public void setInvAsset1(OnmsInventoryAsset invAsset1) {
		m_invAsset1 = invAsset1;
	}

	public OnmsInventoryCategory getInventoryCategory1() {
		return m_inventoryCategory1;
	}

	public void setInventoryCategory1(OnmsInventoryCategory inventoryCategory1) {
		this.m_inventoryCategory1 = inventoryCategory1;
	}
    public TransactionTemplate getTransactionTemplate() {
        return m_transTemplate;
    }

    public void setTransactionTemplate(final TransactionTemplate transactionTemplate) {
        m_transTemplate = transactionTemplate;
    }
    
}
