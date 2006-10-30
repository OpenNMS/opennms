package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminApplicationService.ServiceEditModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AdminApplicationService {
    public OnmsApplication getApplication(String applicationIdString);

    public List<OnmsMonitoredService> findAllMonitoredServices();
    
    public EditModel findApplicationAndAllMonitoredServices(String applicationIdString);

    @Transactional(readOnly = false)
    public void performEdit(String editAction, String editAction2, String[] toAdd, String[] toDelete);

    @Transactional(readOnly = false)
    public OnmsApplication addNewApplication(String name);

    public List<OnmsApplication> findAllApplications();

    @Transactional(readOnly = false)
    public void removeApplication(String applicationIdString);

    public List<OnmsApplication> findByMonitoredService(int id);

    @Transactional(readOnly = false)
    public void performServiceEdit(String ifServiceIdString, String editAction, String[] toAdd, String[] toDelete);

    public ServiceEditModel findServiceApplications(String ifServiceIdString);

}
