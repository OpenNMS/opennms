package org.opennms.web.svclayer.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.web.Util;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.SimpleWebTable;

public class DefaultDistributedPollerService implements
        DistributedPollerService {
    
    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

    public SimpleWebTable createStatusTable() {
        List<OnmsLocationMonitor> monitors = m_locationMonitorDao.findAll();
        
        Collections.sort(monitors, m_comparator);
        
        SimpleWebTable table = new SimpleWebTable();
        table.setTitle("Distributed Poller Status");
        
        table.addColumn("Area", "");
        table.addColumn("Definition Name", "");
        table.addColumn("ID", "");
        table.addColumn("Status", "");
        table.addColumn("Last Check-in Time", "");
        
        for (OnmsLocationMonitor monitor : monitors) {
            String area = "";
            OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
            if (def != null && def.getArea() != null) {
                area = def.getArea();
            }
                        
            String style = getStyleForStatus(monitor.getStatus());
          
            table.newRow();
            table.addCell(area, style);
            table.addCell(monitor.getDefinitionName(), "");
            table.addCell(monitor.getId(), "", "distributedStatusHistory.htm"
                          + "?location="
                          + Util.encode(monitor.getDefinitionName())
                          + "&monitorId="
                          + Util.encode(monitor.getId().toString()));
            table.addCell(monitor.getStatus(), "divider bright");
            table.addCell(new Date(monitor.getLastCheckInTime().getTime()),
                          "");
        }

        return table;
    }

    private String getStyleForStatus(MonitorStatus status) {
        switch (status) {
        case NEW:
            return "Warning";
            
        case REGISTERED:
            return "Warning";
                
        case STARTED:
            return "Normal";
            
        case STOPPED:
            return "Minor";
                
        case UNRESPONSIVE:
            return "Indeterminate";
                
        default:
            return "Indeterminate";
        }
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }
    
    /**
     * Sorts OnmsLocationMonitor by the area for the monitoring location
     * definition (if any), then monitoring location definition name, and
     * finally by location monitor ID.
     * 
     * @author djgregor
     */
    public class OnmsLocationMonitorAreaNameComparator
                implements Comparator<OnmsLocationMonitor> {
        public int compare(OnmsLocationMonitor o1, OnmsLocationMonitor o2) {
            OnmsMonitoringLocationDefinition def1 = null;
            OnmsMonitoringLocationDefinition def2 = null;
            
            if (o1.getDefinitionName() != null) {
                def1 = m_locationMonitorDao.findMonitoringLocationDefinition(o1.getDefinitionName());
            }
            
            if (o2.getDefinitionName() != null) {
                def2 = m_locationMonitorDao.findMonitoringLocationDefinition(o2.getDefinitionName());
            }
            
            int diff;
            
            if ((def1 == null || def1.getArea() == null) && (def2 != null && def2.getArea() != null)) {
                return 1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 == null || def2.getArea() == null)) {
                return -1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 != null && def2.getArea() != null)) {
                if ((diff = def1.getArea().compareToIgnoreCase(def1.getArea())) != 0) {
                    return diff;
                }
            }

            if ((diff = o1.getDefinitionName().compareToIgnoreCase(o2.getDefinitionName())) != 0) {
                return diff;
            }
            
            return o1.getId().compareTo(o2.getId());
        }
    }

}
