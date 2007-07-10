package org.opennms.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.Marshaller;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.RrdSummaryService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

public class RrdSummaryController extends AbstractController implements InitializingBean {
	
	static class MarshalledView implements View {

		public String getContentType() {
			return "text/xml";
		}

		public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			Assert.notNull(model.get("root"), "root must not be null.. unable to marshall xml");
			Marshaller.marshal(model.get("root"), response.getWriter());
		}
		
	}
	
	private RrdSummaryService m_rrdSummaryService;
	

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

        String[] requiredParameters = new String[] {
                "filterRule",
                "start",
                "end"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        String filterRule = request.getParameter("filterRule");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        
        long startTime;
        long endTime;
        try {
            startTime = Long.parseLong(start);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse start '"
                                               + start + "' as an integer time: " + e.getMessage(), e);
        }
        try {
            endTime = Long.parseLong(end);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse end '"
                                               + end + "' as an integer time: " + e.getMessage(), e);
        }
        
        
        Summary summary = m_rrdSummaryService.getSummary(filterRule, startTime, endTime);
        return new ModelAndView(new MarshalledView(), "root", summary); 
	}


	public void afterPropertiesSet() throws Exception {
		Assert.state(m_rrdSummaryService != null, "rrdSummaryService must be set");
	}


	public void setRrdSummaryService(RrdSummaryService rrdSummaryService) {
		m_rrdSummaryService = rrdSummaryService;
	}

}
