/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        if (!((deliveryOptions.getSendMail() || deliveryOptions.getPersist()))) {
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
