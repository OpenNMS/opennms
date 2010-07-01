package org.opennms.web.report.database;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

/**
 * <p>BatchReportFlowHander class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class BatchReportFlowHander extends AbstractFlowHandler {
    
    /** {@inheritDoc} */
    @Override
    public String handleExecutionOutcome(FlowExecutionOutcome outcome, HttpServletRequest request,
        HttpServletResponse response) {
    return "contextRelative:/report/database/batchList.htm";
    }

    /** {@inheritDoc} */
    @Override
    public String handleException(FlowException e, HttpServletRequest request, HttpServletResponse response) {
        if (e instanceof NoSuchFlowExecutionException) {
            return "contextRelative:/report/database/batchList.htm";
        } else {
            throw e;
        }
    }

}
