package org.opennms.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.Marshaller;
import org.opennms.netmgt.config.attrsummary.Summary;
import org.opennms.web.svclayer.RrdSummaryService;
import org.opennms.web.svclayer.SummarySpecification;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractFormController;

public class RrdSummaryController extends AbstractFormController implements InitializingBean {
	
	static class MarshalledView implements View {

		public String getContentType() {
			return "text/xml";
		}

		public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			Assert.notNull(model.get("summary"), "summary must not be null.. unable to marshall xml");
			Marshaller.marshal(model.get("summary"), response.getWriter());
		}
		
	}
	
	private RrdSummaryService m_rrdSummaryService;
	

	public RrdSummaryController() {
	    super();
	    setCommandClass(SummarySpecification.class);
	}

    private ModelAndView getSummary(SummarySpecification spec) {
        Summary summary = m_rrdSummaryService.getSummary(spec);
        return new ModelAndView(new MarshalledView(), "summary", summary);
    }
	
	


	public void afterPropertiesSet() throws Exception {
		Assert.state(m_rrdSummaryService != null, "rrdSummaryService must be set");
	}


	public void setRrdSummaryService(RrdSummaryService rrdSummaryService) {
		m_rrdSummaryService = rrdSummaryService;
	}

    @Override
    protected boolean isFormSubmission(HttpServletRequest request) {
        return true;
    }

    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request,
            HttpServletResponse response, Object command, BindException errors)
            throws Exception {
        return getSummary((SummarySpecification)command);
        
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request,
            HttpServletResponse response, BindException errors)
            throws Exception {
        throw new UnsupportedOperationException("RrdSummaryController.showForm is not yet implemented");
    }



}
