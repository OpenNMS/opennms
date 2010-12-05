package org.opennms.web.report.database;

import java.util.Iterator;
import java.util.List;

import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * <p>ReportParametersValidator class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ReportParametersValidator {
    
    /**
     * <p>validateReportParameters</p>
     *
     * @param reportCriteria a {@link org.opennms.api.reporting.parameter.ReportParameters} object.
     * @param context a {@link org.springframework.binding.validation.ValidationContext} object.
     */
    public void  validateReportParameters(ReportParameters reportCriteria, ValidationContext context) {
        MessageContext messages = context.getMessageContext();
        
        
        List<ReportDateParm> dateParms = reportCriteria.getDateParms();
        
        for (Iterator<ReportDateParm> dateParmIter = dateParms.iterator(); dateParmIter.hasNext();) {
            ReportDateParm dateParm = dateParmIter.next();
            if (dateParm.getDate() == null) {
                messages.addMessage(new MessageBuilder().error().source("date parms").
                                    defaultText("cannot have null date field" + dateParm.getDisplayName()).build());
            }
        }
        
        List<ReportStringParm> stringParms = reportCriteria.getStringParms();
        
        for (Iterator<ReportStringParm> stringParmIter = stringParms.iterator(); stringParmIter.hasNext();) {
            ReportStringParm stringParm = stringParmIter.next();
            if (stringParm.getValue() == "" ) {
                messages.addMessage(new MessageBuilder().error().source("string parms").
                                    defaultText("cannot have empty string field" + stringParm.getDisplayName()).build());
            }
        }
        
        List<ReportIntParm> intParms = reportCriteria.getIntParms();
        
        for (Iterator<ReportIntParm> intParmIter = intParms.iterator(); intParmIter.hasNext();) {
            ReportIntParm intParm = intParmIter.next();
            // TODO add a more sensible check here - I think we probably can have zero int parm
            if (intParm.getValue() == 0 ) {
                messages.addMessage(new MessageBuilder().error().source("int parms").
                                    defaultText("cannot have zero integer field" + intParm.getDisplayName()).build());
            }
        }
        
        List<ReportFloatParm> floatParms = reportCriteria.getFloatParms();
        
        for (Iterator<ReportFloatParm> floatParmIter = floatParms.iterator(); floatParmIter.hasNext();) {
            ReportFloatParm floatParm = floatParmIter.next();
            if (floatParm.getValue() == null ) {
                messages.addMessage(new MessageBuilder().error().source("float parms").
                                    defaultText("cannot have null float field" + floatParm.getDisplayName()).build());
            }
        }
        
    }

}
