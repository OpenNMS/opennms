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
// Created: October 5th, 2009 jonathan@opennms.org
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

import java.util.Iterator;

import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.reporting.core.model.DatabaseReportDateParm;
import org.opennms.reporting.core.model.DatabaseReportIntParm;
import org.opennms.reporting.core.model.DatabaseReportStringParm;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class DatabaseReportCriteriaCommand extends DatabaseReportCriteria {
    
    private static final long serialVersionUID = -8677153593646542878L;

    public DatabaseReportCriteriaCommand() {
        super();
    }
        
        public void validateReportParameters(ValidationContext context) {
            MessageContext messages = context.getMessageContext();
            
            // this should be replaced with a call to validate on the reportengine bean
            
            for (Iterator<DatabaseReportDateParm> dateParms = m_dateParms.iterator(); dateParms.hasNext();) {
                DatabaseReportDateParm dateParm = dateParms.next();
                if (dateParm.getValue() == null) {
                    messages.addMessage(new MessageBuilder().error().source("date parms").
                                        defaultText("cannot have null date field" + dateParm.getDisplayName()).build());
                }
            }
            
            for (Iterator<DatabaseReportStringParm> stringParms = m_stringParms.iterator(); stringParms.hasNext();) {
                DatabaseReportStringParm stringParm = stringParms.next();
                if (stringParm.getValue() == "" ) {
                    messages.addMessage(new MessageBuilder().error().source("string parms").
                                        defaultText("cannot have empty string field " + stringParm.getDisplayName()).build());
                }
            }
            
            for (Iterator<DatabaseReportIntParm> intParms = m_intParms.iterator(); intParms.hasNext();) {
                DatabaseReportIntParm intParm = intParms.next();
                // TODO add a more sensible check here
                if (intParm.getValue() == 0 ) {
                    messages.addMessage(new MessageBuilder().error().source("int parms").
                                        defaultText("cannot have empty string field " + intParm.getDisplayName()).build());
                }
            }
            
        }

}
