package org.opennms.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.StreamUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.web.MissingParameterException;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class RrdGraphController extends AbstractController {
    private RrdGraphService m_rrdGraphService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String[] requiredParameters = new String[] {
                "type",
                "parentResourceType",
                "parentResource",
                "resourceType",
                "resource",
                "start",
                "end"
        };
        
        for (String requiredParameter : requiredParameters) {
            if (request.getParameter(requiredParameter) == null) {
                throw new MissingParameterException(requiredParameter,
                                                    requiredParameters);
            }
        }

        String type = request.getParameter("type");
        String parentResourceType = request.getParameter("parentResourceType");
        String parentResource = request.getParameter("parentResource");
        String resourceType = request.getParameter("resourceType");
        String resource = request.getParameter("resource");
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
        
        InputStream tempIn;
        if ("true".equals(request.getParameter("adhoc"))) {
            String[] adhocRequiredParameters = new String[] {
                    "title",
                    "ds",
                    "agfunction",
                    "color",
                    "dstitle",
                    "style"
            };
            
            for (String requiredParameter : adhocRequiredParameters) {
                if (request.getParameter(requiredParameter) == null) {
                    throw new MissingParameterException(requiredParameter,
                                                        adhocRequiredParameters);
                }
            }

            String title = request.getParameter("title");
            String[] dataSources = request.getParameterValues("ds");
            String[] aggregateFunctions = request.getParameterValues("agfunction");
            String[] colors = request.getParameterValues("color");
            String[] dataSourceTitles = request.getParameterValues("dstitle");
            String[] styles = request.getParameterValues("style");
            
            tempIn = m_rrdGraphService.getAdhocGraph(type,
                                                     parentResourceType,
                                                     parentResource,
                                                     resourceType,
                                                     resource,
                                                     title,
                                                     dataSources,
                                                     aggregateFunctions,
                                                     colors,
                                                     dataSourceTitles,
                                                     styles,
                                                     startTime, endTime);
        } else {
            String report = request.getParameter("report");
            if (report == null) {
                throw new MissingParameterException("report");
            }
            
            tempIn = m_rrdGraphService.getPrefabGraph(type,
                    parentResourceType, parentResource,
                    resourceType, resource, report, startTime, endTime);
        }

        response.setContentType("image/png");
        
        StreamUtils.streamToStream(tempIn, response.getOutputStream());

        tempIn.close();
                
        return null;
    }

    public RrdGraphService getRrdGraphService() {
        return m_rrdGraphService;
    }

    public void setRrdGraphService(RrdGraphService rrdGraphService) {
        m_rrdGraphService = rrdGraphService;
    }
}
