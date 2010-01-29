package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.api.reporting.DeliveryOptions;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.web.svclayer.support.TriggerDescription;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.RequestContext;

@Transactional(readOnly = true)
public interface SchedulerService {
    
    public abstract List<TriggerDescription> getTriggerDescriptions();

    @Transactional(readOnly = false)
    public abstract void removeTrigger(String triggerName);
    
    @Transactional(readOnly = false)
    public abstract void removeTriggers(String[] triggerNames);
    
    public abstract Boolean exists(String triggerName);

    @Transactional(readOnly = false)
    public abstract String addCronTrigger(String id,
            ReportParameters criteria, 
            DeliveryOptions deliveryOptions,
            String triggerName, 
            String cronExpression, 
            RequestContext context);

    @Transactional(readOnly = false)
    public abstract String execute(String id,
            ReportParameters criteria, 
            DeliveryOptions deliveryOptions,
            RequestContext context);

}