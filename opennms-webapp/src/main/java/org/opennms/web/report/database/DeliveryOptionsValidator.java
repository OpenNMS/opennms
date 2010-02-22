package org.opennms.web.report.database;

import org.opennms.reporting.core.DeliveryOptions;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class DeliveryOptionsValidator {

    public void validateDeliveryOptions(DeliveryOptions deliveryOptions, ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if (!((deliveryOptions.getSendMail() | deliveryOptions.getPersist()))) {
            messages.addMessage(new MessageBuilder().error().source("sendMail").
                                defaultText("one of send mail or persist should be selected").build());
            messages.addMessage(new MessageBuilder().error().source("persist").
                                defaultText("one of send mail or persist should be selected").build());
        } else {
            if (deliveryOptions.getSendMail() && (deliveryOptions.getMailTo() == "")) {
                messages.addMessage(new MessageBuilder().error().source("mailTo").
                                    defaultText("cannot have empty mail recipient").build());
            }
        }
        if (!(deliveryOptions.getInstanceId().length() == 0)) {
            messages.addMessage(new MessageBuilder().error().source("instanceId").
                                defaultText("cannot have an empty Id for the report instance").build());
        }
        
    }
}
