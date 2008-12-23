package org.opennms.netmgt.scriptd.ins.events;

import java.util.List;

import org.apache.log4j.Category;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class InsAbstractSession extends Thread {

    private String m_criteria;
    
    private Category log;
	/**
	 * the shared string for client authentication
	 * If the shared string is not set, then server doesn't require authentication 
	 */
	public String sharedAuthAsciiString = null;
	
	/**
	 * the criteria for getting active alarms
	 */
	public String criteriaRestriction = "";
	
	public void setSharedASCIIString(String sharedASCIIString) {
		this.sharedAuthAsciiString = sharedASCIIString;
	}
	
	public String getSharedASCIIString() {
		return sharedAuthAsciiString;
	}

	public String getCriteriaRestriction() {
		return criteriaRestriction;
	}

	public void setCriteriaRestriction(String criteriaRestriction) {
		this.criteriaRestriction = criteriaRestriction;
	}

    public InsAbstractSession() {
        super();
        ThreadCategory.setPrefix("OpenNMS.InsProxy");
        log=ThreadCategory.getInstance(this.getClass());
    }

    public Category getLog() {
        return log;
    }
    
    public void setLog(Category log) {
        this.log = log;
    }
	
	
    private String getCriteria() {
        return m_criteria;
    }

    private void setCriteria(String criteria) {
        m_criteria = criteria;
    }
    
    protected String getIfAlias(int nodeid, int ifindex) {

        setCriteria("nodeid = " + nodeid + " AND snmpifindex = " + ifindex);
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        List<OnmsSnmpInterface> iface = (List<OnmsSnmpInterface>) transTemplate.execute(
                   new TransactionCallback() {
                        public Object doInTransaction(final TransactionStatus status) {
                            final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
                            onmsCriteria.add(Restrictions.sqlRestriction(getCriteria()));
                            return snmpInterfaceDao.findMatching(onmsCriteria);
                        }
                   }
        );
        log.debug("interfacce trovate: " + iface.size());
        
        return iface.get(0).getIfAlias();
    }

    protected OnmsSnmpInterface getIfAlias(int nodeid, String ipaddr) {

        setCriteria("nodeid = " + nodeid + " AND ipaddr = '" + ipaddr +"'");
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        List<OnmsSnmpInterface> iface = (List<OnmsSnmpInterface>) transTemplate.execute(
                   new TransactionCallback() {
                        public Object doInTransaction(final TransactionStatus status) {
                            final OnmsCriteria onmsCriteria = new OnmsCriteria(OnmsSnmpInterface.class);
                            onmsCriteria.add(Restrictions.sqlRestriction(getCriteria()));
                            return snmpInterfaceDao.findMatching(onmsCriteria);
                        }
                   }
        );
        log.debug("interfacce trovate: " + iface.size());
        
        return iface.get(0);
    }

}
