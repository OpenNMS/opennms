package org.opennms.web.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.web.MissingParameterException;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.svclayer.GraphResultsService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class GraphResultsController extends AbstractController {
    private GraphResultsService m_graphResultsService;
    
    private RelativeTimePeriod[] m_periods =
        RelativeTimePeriod.getDefaultPeriods();

    private RrdStrategy m_rrdStrategy;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] {
                "resourceId",
                "reports"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        String[] resourceIds = request.getParameterValues("resourceId");
        String[] reports = request.getParameterValues("reports");
        
        // see if the start and end time were explicitly set as params
        String start = request.getParameter("start");
        String end = request.getParameter("end");

        String relativeTime = request.getParameter("relativetime");
        
        String startMonth = request.getParameter("startMonth");
        String startDate = request.getParameter("startDate");
        String startYear = request.getParameter("startYear");
        String startHour = request.getParameter("startHour");

        String endMonth = request.getParameter("endMonth");
        String endDate = request.getParameter("endDate");
        String endYear = request.getParameter("endYear");
        String endHour = request.getParameter("endHour");
        
        long startLong;
        long endLong;

        if (start != null || end != null) {
            String[] ourRequiredParameters = new String[] {
                    "start",
                    "end"
            };
        
            if (start == null) {
                throw new MissingParameterException("start",
                                                    ourRequiredParameters);
            }
            
            if (end == null) {
                throw new MissingParameterException("end",
                                                    ourRequiredParameters);
            }
            
            // XXX could use some error checking
            startLong = Long.parseLong(start);
            endLong = Long.parseLong(end);
        } else if (startMonth != null || startDate != null 
                   || startYear != null || startHour != null
                   || endMonth != null || endDate != null || endYear != null
                   || endHour != null) {
            
            String[] ourRequiredParameters = new String[] {
                    "startMonth",
                    "startDate",
                    "startYear",
                    "startHour",
                    "endMonth",
                    "endDate",
                    "endYear",
                    "endHour"
            };
            
            for (String requiredParameter : ourRequiredParameters) {
                if (request.getParameter(requiredParameter) == null) {
                    throw new MissingParameterException(requiredParameter,
                                                        ourRequiredParameters);
                }
            }

            Calendar startCal = Calendar.getInstance();
            startCal.set(Calendar.MONTH, Integer.parseInt(startMonth));
            startCal.set(Calendar.DATE, Integer.parseInt(startDate));
            startCal.set(Calendar.YEAR, Integer.parseInt(startYear));
            startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);

            Calendar endCal = Calendar.getInstance();
            endCal.set(Calendar.MONTH, Integer.parseInt(endMonth));
            endCal.set(Calendar.DATE, Integer.parseInt(endDate));
            endCal.set(Calendar.YEAR, Integer.parseInt(endYear));
            endCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);

            startLong = startCal.getTime().getTime();
            endLong = endCal.getTime().getTime();
        } else {
            if (relativeTime == null) {
                relativeTime = m_periods[0].getId();
            }

            RelativeTimePeriod period = RelativeTimePeriod.getPeriodByIdOrDefault(
                                                                                  m_periods,
                                                                                  relativeTime,
                                                                                  m_periods[0]);

            long[] times = period.getStartAndEndTimes();
            startLong = times[0];
            endLong = times[1];
        }
        
        GraphResults model =
            m_graphResultsService.findResults(resourceIds,
                                              reports, startLong,
                                              endLong, relativeTime);

        ModelAndView modelAndView = new ModelAndView("/graph/results",
                                                     "results",
                                                     model);
        modelAndView.addObject("rrdStrategy", m_rrdStrategy);
        return modelAndView;
    }

    public GraphResultsService getGraphResultsService() {
        return m_graphResultsService;
    }

    public void setGraphResultsService(GraphResultsService graphResultsService) {
        m_graphResultsService = graphResultsService;
    }

    public RrdStrategy getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setRrdStrategy(RrdStrategy rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }
}
