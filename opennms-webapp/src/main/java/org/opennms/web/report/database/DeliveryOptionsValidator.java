package org.opennms.web.report.database;

import org.opennms.api.reporting.DeliveryOptions;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class DeliveryOptionsValidator {

    public void validateDeliveryOptions(DeliveryOptions deliveryOptions, ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if (!(deliveryOptions.getCanPersist() && (deliveryOptions.getSendMail() | deliveryOptions.getPersist()))) {
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
        
    }
}
