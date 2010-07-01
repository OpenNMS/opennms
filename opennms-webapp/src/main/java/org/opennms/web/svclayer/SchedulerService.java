package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.web.svclayer.support.TriggerDescription;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.RequestContext;

/**
 * <p>SchedulerService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface SchedulerService {
    
    /**
     * <p>getTriggerDescriptions</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<TriggerDescription> getTriggerDescriptions();

    /**
     * <p>removeTrigger</p>
     *
     * @param triggerName a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public abstract void removeTrigger(String triggerName);
    
    /**
     * <p>removeTriggers</p>
     *
     * @param triggerNames an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public abstract void removeTriggers(String[] triggerNames);
    
    /**
     * <p>exists</p>
     *
     * @param triggerName a {@link java.lang.String} object.
     * @return a {@link java.lang.Boolean} object.
     */
    public abstract Boolean exists(String triggerName);

    /**
     * <p>addCronTrigger</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param criteria a {@link org.opennms.api.reporting.parameter.ReportParameters} object.
     * @param deliveryOptions a {@link org.opennms.reporting.core.DeliveryOptions} object.
     * @param cronExpression a {@link java.lang.String} object.
     * @param context a {@link org.springframework.webflow.execution.RequestContext} object.
     * @return a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public abstract String addCronTrigger(String id,
            ReportParameters criteria, 
            DeliveryOptions deliveryOptions,
            String cronExpression, 
            RequestContext context);

    /**
     * <p>execute</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param criteria a {@link org.opennms.api.reporting.parameter.ReportParameters} object.
     * @param deliveryOptions a {@link org.opennms.reporting.core.DeliveryOptions} object.
     * @param context a {@link org.springframework.webflow.execution.RequestContext} object.
     * @return a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public abstract String execute(String id,
            ReportParameters criteria, 
            DeliveryOptions deliveryOptions,
            RequestContext context);

}
