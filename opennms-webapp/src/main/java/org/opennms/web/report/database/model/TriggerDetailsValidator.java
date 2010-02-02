package org.opennms.web.report.database.model;

import org.opennms.web.svclayer.SchedulerService;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class TriggerDetailsValidator {
    
    SchedulerService m_schedulerService;

    public void ValidateTriggerDetails(TriggerDetails triggerDetails, ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if (m_schedulerService.exists(triggerDetails.getTriggerName())) {
            messages.addMessage(new MessageBuilder().error().source("triggerName").
                defaultText("Trigger name is already in use").build());
        }
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        m_schedulerService = schedulerService;
    }

}
