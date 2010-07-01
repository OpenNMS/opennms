package org.opennms.web.report.database.model;

import org.springframework.binding.validation.ValidationContext;

/**
 * <p>TriggerDetailsValidator class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class TriggerDetailsValidator {
    
    /**
     * <p>ValidateTriggerDetails</p>
     *
     * @param triggerDetails a {@link org.opennms.web.report.database.model.TriggerDetails} object.
     * @param context a {@link org.springframework.binding.validation.ValidationContext} object.
     */
    public void ValidateTriggerDetails(TriggerDetails triggerDetails, ValidationContext context) {
        //TODO validate the cron expression here
    }

}
