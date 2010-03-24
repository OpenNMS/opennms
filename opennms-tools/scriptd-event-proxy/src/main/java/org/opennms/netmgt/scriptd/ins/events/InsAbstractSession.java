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
	    getLog().debug("Setting the criteria restriction for active alarm list: " + criteriaRestriction);
		this.criteriaRestriction = criteriaRestriction;
	}

    public InsAbstractSession() {
        super();
        ThreadCategory.setPrefix("OpenNMS.InsProxy");
        log=ThreadCategory.getInstance(this.getClass());
        log.debug("InsAbstract Session Constructor: loaded");
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
    
    @SuppressWarnings("unchecked")
    protected String getIfAlias(int nodeid, int ifindex) {

        log.debug("getting ifalias for nodeid: " +nodeid + " and ifindex: " + ifindex);

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
        log.debug("interfaces found: " + iface.size());

        if (iface.size() == 0) return "-1";
        String ifAlias = iface.get(0).getIfAlias();
        log.debug("ifalias found: " + ifAlias);
        
        return ifAlias;
    }

}
