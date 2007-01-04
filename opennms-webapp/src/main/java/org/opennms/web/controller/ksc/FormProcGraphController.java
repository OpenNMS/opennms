package org.opennms.web.controller.ksc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.web.svclayer.KscReportService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class FormProcGraphController extends AbstractController implements InitializingBean {
    private KSC_PerformanceReportFactory m_kscReportFactory;
    private KscReportService m_kscReportService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get The Customizable (Working) Graph 
        Graph graph = getKscReportFactory().getWorkingGraph();

        // Get Form Variables
        String action = request.getParameter("action");
        String timespan = request.getParameter("timespan");
        String graphtype = request.getParameter("graphtype");
        String title = request.getParameter("title");
        String g_index = request.getParameter("graphindex");
        int graph_index = (Integer.parseInt(g_index));
        graph_index--; 
     
        // Save the modified variables into the working graph 
        graph.setTitle(title);
        graph.setTimespan(timespan);
        graph.setGraphtype(graphtype);
        
        OnmsResource resource = getKscReportService().getResourceFromGraph(graph);

        if (action.equals("Save")) {
            // The working graph is complete now... lets save working graph to working report 
            getKscReportFactory().unloadWorkingGraph(graph_index);
        }
        
        if (action.equals("Save") || action.equals("Cancel")) {
            return new ModelAndView("redirect:/KSC/customReport.htm");
        } else if (action.equals("Update")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customGraphEditDetails.htm");
            modelAndView.addObject("resourceId", resource.getId());
            modelAndView.addObject("graphtype", graph.getGraphtype());
            return modelAndView;
        } else if (action.equals("ChooseResource")) {
            ModelAndView modelAndView = new ModelAndView("redirect:/KSC/customGraphChooseResource.htm");
            modelAndView.addObject("resourceId", resource.getId());
            modelAndView.addObject("selectedResourceId", resource.getId());
            return modelAndView;
        } else {
            throw new IllegalArgumentException("parameter action of '" + action + "' is not supported.  Must be one of: Save, Cancel, Update, or ChooseResource");
        }
    }

    public KSC_PerformanceReportFactory getKscReportFactory() {
        return m_kscReportFactory;
    }

    public void setKscReportFactory(KSC_PerformanceReportFactory kscReportFactory) {
        m_kscReportFactory = kscReportFactory;
    }


    public KscReportService getKscReportService() {
        return m_kscReportService;
    }

    public void setKscReportService(KscReportService kscReportService) {
        m_kscReportService = kscReportService;
    }

    public void afterPropertiesSet() throws Exception {
        if (m_kscReportFactory == null) {
            throw new IllegalStateException("property kscReportFactory must be set");
        }
        if (m_kscReportService == null) {
            throw new IllegalStateException("property kscReportService must be set");
        }
    }
}
