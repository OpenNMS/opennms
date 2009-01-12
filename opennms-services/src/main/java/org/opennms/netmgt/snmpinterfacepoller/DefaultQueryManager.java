package org.opennms.netmgt.snmpinterfacepoller;

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


public class DefaultQueryManager implements QueryManager {

    String m_criteria;
    OnmsSnmpInterface m_snmpInterface;
    private Category log;

    private String getCriteria() {
        return m_criteria;
    }

    private void setCriteria(String criteria) {
        m_criteria = criteria;
    }

    public DefaultQueryManager() {
        log = ThreadCategory.getInstance(this.getClass());
    }

    public List<OnmsSnmpInterface> getSnmpInterfaces(String criteria) {
        
        setCriteria(criteria);
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
        
        return iface;
    }

    public void saveSnmpInterface(OnmsSnmpInterface snmpInterface) {
        setSnmpInterface(snmpInterface);
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean(bf,"snmpInterfaceDao", SnmpInterfaceDao.class);
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        transTemplate.execute(
                      new TransactionCallback() {
                          public Object doInTransaction(final TransactionStatus status) {
                              snmpInterfaceDao.saveOrUpdate(getSnmpInterface());
                              return new Object();
                          }
                      }
        );
    }

    private OnmsSnmpInterface getSnmpInterface() {
        return m_snmpInterface;
    }

    private void setSnmpInterface(OnmsSnmpInterface snmpInterface) {
        m_snmpInterface = snmpInterface;
    }

}
