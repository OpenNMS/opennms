package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.api.integration.reporting.DeliveryOptions;
import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.web.svclayer.support.TriggerDescription;
import org.springframework.webflow.execution.RequestContext;

public interface SchedulerService {
    
    public abstract List<TriggerDescription> getTriggerDescriptions();

    public abstract void removeTrigger(String triggerName);
    
    public abstract void removeTriggers(String[] triggerNames);
    
    public abstract Boolean exists(String triggerName);

    public abstract String addCronTrigger(DatabaseReportCriteria criteria, 
            DeliveryOptions deliveryOptions,
            String triggerName, 
            String cronExpression, 
            RequestContext context);

    public abstract String execute(DatabaseReportCriteria criteria, 
            DeliveryOptions deliveryOptions,
            RequestContext context);

}