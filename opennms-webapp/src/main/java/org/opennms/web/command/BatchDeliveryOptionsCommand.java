//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: December 7th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.command;


import org.opennms.api.reporting.DeliveryOptions;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class BatchDeliveryOptionsCommand extends DeliveryOptions {

    private static final long serialVersionUID = -4725838438394923258L;
    
    public BatchDeliveryOptionsCommand() {
        super();
    }
    
    public void validateBatchDeliveryOptions(ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        if ((m_persist == false) && (m_sendMail == false)) {
            messages.addMessage(new MessageBuilder().error().source("Persist").
                defaultText("At least one of the these options must be set").build());
            messages.addMessage(new MessageBuilder().error().source("sendMail").
                defaultText("At least one of the these options must be set").build());
        } else if (m_sendMail && (m_mailTo.equals(""))) {
            messages.addMessage(new MessageBuilder().error().source("mailTo").
                defaultText("require an email address for delivery").build());
        }
    }

}
