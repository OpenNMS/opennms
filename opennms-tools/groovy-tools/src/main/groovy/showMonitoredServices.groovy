#!/usr/bin/env groovy


import org.opennms.core.utils.*;
import org.springframework.context.support.*;
import org.springframework.transaction.*;
import org.springframework.transaction.support.*;
import org.opennms.netmgt.dao.MonitoredServiceDao;

class InTransaction implements TransactionCallback {
  Closure m_callback;

    InTransaction(Closure callback) {
       m_callback = callback;
    }

    public Object doInTransaction(TransactionStatus status) {
       m_callback.call(status);
    }

}

  

System.setProperty("rrd.base.dir", new File(".").getAbsolutePath());
System.setProperty("rrd.binary", "/usr/bin/rrdtool");

context = BeanUtils.getFactory("daoContext", ClassPathXmlApplicationContext.class);

TransactionTemplate transTemplate = context.getBean("transactionTemplate", TransactionTemplate.class);
MonitoredServiceDao dao = context.getBean("monitoredServiceDao", MonitoredServiceDao.class);

inTransaction = new InTransaction() {
    dao.findAll().each {
        println "${it.nodeId}:${it.ipAddress}:${it.serviceName}"
    }
}

transTemplate.execute(inTransaction);



System.exit(0);


