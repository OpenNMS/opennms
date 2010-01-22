package org.opennms.web.report.database;

import java.util.Iterator;
import java.util.List;

import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.reporting.core.model.DatabaseReportDateParm;
import org.opennms.reporting.core.model.DatabaseReportIntParm;
import org.opennms.reporting.core.model.DatabaseReportStringParm;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

public class ReportCriteriaValidator {
    
    public void  validateReportCriteria(DatabaseReportCriteria reportCriteria, ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        
        
        List<DatabaseReportDateParm> dateParms = reportCriteria.getDateParms();
        
        for (Iterator<DatabaseReportDateParm> dateParmIter = dateParms.iterator(); dateParmIter.hasNext();) {
            DatabaseReportDateParm dateParm = dateParmIter.next();
            if (dateParm.getValue() == null) {
                messages.addMessage(new MessageBuilder().error().source("date parms").
                                    defaultText("cannot have null date field" + dateParm.getDisplayName()).build());
            }
        }
        
        List<DatabaseReportStringParm> stringParms = reportCriteria.getStringParms();
        
        for (Iterator<DatabaseReportStringParm> stringParmIter = stringParms.iterator(); stringParmIter.hasNext();) {
            DatabaseReportStringParm stringParm = stringParmIter.next();
            if (stringParm.getValue() == "" ) {
                messages.addMessage(new MessageBuilder().error().source("string parms").
                                    defaultText("cannot have empty string field " + stringParm.getDisplayName()).build());
            }
        }
        
        List<DatabaseReportIntParm> intParms = reportCriteria.getIntParms();
        
        for (Iterator<DatabaseReportIntParm> intParmIter = intParms.iterator(); intParmIter.hasNext();) {
            DatabaseReportIntParm intParm = intParmIter.next();
            // TODO add a more sensible check here
            if (intParm.getValue() == 0 ) {
                messages.addMessage(new MessageBuilder().error().source("int parms").
                                    defaultText("cannot have empty string field " + intParm.getDisplayName()).build());
            }
        }
        
    }

}
