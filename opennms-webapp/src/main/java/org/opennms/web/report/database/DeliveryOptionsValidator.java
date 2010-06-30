package org.opennms.web.report.database;

import org.opennms.reporting.core.DeliveryOptions;
import org.opennms.web.svclayer.SchedulerService;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * <p>DeliveryOptionsValidator class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DeliveryOptionsValidator {

    private SchedulerService m_reportSchedulerService;

    /**
     * <p>validateDeliveryOptions</p>
     *
     * @param deliveryOptions a {@link org.opennms.reporting.core.DeliveryOptions} object.
     * @param context a {@link org.springframework.binding.validation.ValidationContext} object.
     */
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
        if (deliveryOptions.getInstanceId().length() == 0) {
            messages.addMessage(new MessageBuilder().error().source("instanceId").
                                defaultText("cannot have an empty Id for the report instance").build());
        } else if (m_reportSchedulerService.exists(deliveryOptions.getInstanceId())) {
            messages.addMessage(new MessageBuilder().error().source("instanceId").
                                defaultText("report instanceId is already in use").build());
        }
        
    }
    
    /**
     * <p>setReportSchedulerService</p>
     *
     * @param reportSchedulerService a {@link org.opennms.web.svclayer.SchedulerService} object.
     */
    public void setReportSchedulerService(SchedulerService reportSchedulerService) {
        m_reportSchedulerService = reportSchedulerService;
    }
}
