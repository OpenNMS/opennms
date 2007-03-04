package org.opennms.web.svclayer.support;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.RtcService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultRtcService implements RtcService, InitializingBean {
    private MonitoredServiceDao m_monitoredServiceDao;
    private OutageDao m_outageDao;

    public RtcNodeModel getNodeList() {
        OnmsCriteria serviceCriteria = createServiceCriteria();
        OnmsCriteria outageCriteria = createOutageCriteria();
        
        return getNodeListForCriteria(serviceCriteria, outageCriteria);
    }
    
    public RtcNodeModel getNodeListForCriteria(OnmsCriteria serviceCriteria, OnmsCriteria outageCriteria) {
        serviceCriteria.addOrder(Order.asc("node.label"));
        serviceCriteria.addOrder(Order.asc("node.id"));
        serviceCriteria.addOrder(Order.asc("ipInterface.ipAddress"));
        serviceCriteria.addOrder(Order.asc("serviceType.name"));

        Date periodEnd = new Date(System.currentTimeMillis());
        Date periodStart = new Date(periodEnd.getTime() - (24 * 60 * 60 * 1000));
        
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.isNull("ifRegainedService"));
        disjunction.add(Restrictions.ge("ifLostService", periodStart));
        disjunction.add(Restrictions.ge("ifRegainedService", periodStart));
        outageCriteria.add(disjunction);
        
        outageCriteria.addOrder(Order.asc("monitoredService"));
        outageCriteria.addOrder(Order.asc("ifLostService"));
        
        List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(serviceCriteria);
        List<OnmsOutage> outages = m_outageDao.findMatching(outageCriteria);
        
        Map<OnmsMonitoredService, Long> serviceDownTime = calculateServiceDownTime(periodEnd, periodStart, outages);
        
        RtcNodeModel model = new RtcNodeModel();
        
        OnmsNode lastNode = null;
        int serviceCount = 0;
        int serviceDownCount = 0;
        long downMillisCount = 0;
        for (OnmsMonitoredService service : services) {
            if (!service.getIpInterface().getNode().equals(lastNode) && lastNode != null) {
                Double availability = calculateAvailability(serviceCount, downMillisCount);
                
                model.addNode(new RtcNodeModel.RtcNode(lastNode, serviceCount, serviceDownCount, availability));
                
                serviceCount = 0;
                serviceDownCount = 0;
                downMillisCount = 0;
            }
            
            serviceCount++;
            if (service.isDown()) {
                serviceDownCount++;
            }
            
            Long downMillis = serviceDownTime.get(service);
            if  (downMillis != null) {
                downMillisCount += downMillis;
            }
            
            lastNode = service.getIpInterface().getNode();
        }
        if (lastNode != null) {
            Double availability = calculateAvailability(serviceCount, downMillisCount);
            
            model.addNode(new RtcNodeModel.RtcNode(lastNode, serviceCount, serviceDownCount, availability));
        }
        
        return model;
    }

    public OnmsCriteria createOutageCriteria() {
        OnmsCriteria outageCriteria = new OnmsCriteria(OnmsOutage.class, "outage");

        outageCriteria.createAlias("monitoredService", "monitoredService", CriteriaSpecification.INNER_JOIN);
        outageCriteria.add(Restrictions.eq("monitoredService.status", "A"));
        outageCriteria.createAlias("monitoredService.ipInterface", "ipInterface", CriteriaSpecification.INNER_JOIN);
        outageCriteria.add(Restrictions.ne("ipInterface.isManaged", "D"));
        outageCriteria.createAlias("monitoredService.ipInterface.node", "node", CriteriaSpecification.INNER_JOIN);
        outageCriteria.add(Restrictions.ne("node.type", "D"));
        
        return outageCriteria;
    }

    public OnmsCriteria createServiceCriteria() {
        OnmsCriteria serviceCriteria = new OnmsCriteria(OnmsMonitoredService.class, "monitoredService");

        serviceCriteria.add(Restrictions.eq("monitoredService.status", "A"));
        serviceCriteria.createAlias("ipInterface", "ipInterface", CriteriaSpecification.INNER_JOIN);
        serviceCriteria.add(Restrictions.ne("ipInterface.isManaged", "D"));
        serviceCriteria.createAlias("ipInterface.node", "node", CriteriaSpecification.INNER_JOIN);
        serviceCriteria.add(Restrictions.ne("node.type", "D"));
        serviceCriteria.createAlias("serviceType", "serviceType", CriteriaSpecification.INNER_JOIN);
        serviceCriteria.createAlias("currentOutages", "currentOutages", CriteriaSpecification.LEFT_JOIN);
        
        return serviceCriteria;
    }

    private Map<OnmsMonitoredService, Long> calculateServiceDownTime(Date periodEnd, Date periodStart, List<OnmsOutage> outages) {
        Map<OnmsMonitoredService, Long> map = new HashMap<OnmsMonitoredService, Long>();
        for (OnmsOutage outage : outages) {
            if (map.get(outage.getMonitoredService()) == null) {
                map.put(outage.getMonitoredService(), new Long(0));
            }
            
            Date begin;
            if (outage.getIfLostService().before(periodStart)) {
                begin = periodStart;
            } else {
                begin = outage.getIfLostService();
            }
            
            Date end;
            if (outage.getIfRegainedService() == null || !outage.getIfRegainedService().before(periodEnd)) {
                end = periodEnd;
            } else {
                end = outage.getIfRegainedService();
            }
            
            Long count = map.get(outage.getMonitoredService());
            count += (end.getTime() - begin.getTime());
            map.put(outage.getMonitoredService(), count);
        }
        return map;
    }

    private Double calculateAvailability(int serviceCount, long downMillisCount) {
        long upMillis = (serviceCount * (24 * 60 * 60 * 1000)) - downMillisCount;

        return ((double) upMillis / (double) (serviceCount * (24 * 60 * 60 * 1000)));
    }
    
    public void afterPropertiesSet() {
        Assert.state(m_monitoredServiceDao != null, "property monitoredServiceDao must be set and non-null");
        Assert.state(m_outageDao != null, "property outageDao must be set and non-null");
    }
    
    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }
    public OutageDao getOutageDao() {
        return m_outageDao;
    }
    public void setOutageDao(OutageDao outageDao) {
        m_outageDao = outageDao;
    }
}
