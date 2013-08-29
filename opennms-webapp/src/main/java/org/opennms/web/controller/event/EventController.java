package org.opennms.web.controller.event;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.model.OnmsFilter;
import org.opennms.web.services.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class EventController extends MultiActionController {

    @Autowired
    FilterService filterService;
    
    @Transactional
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
        List<OnmsFilter> userFilterList = filterService.getFilters(request.getRemoteUser(), OnmsFilter.Page.EVENT);
        ModelAndView modelAndView = new ModelAndView("event/index");
        modelAndView.addObject("filters", userFilterList);
        return modelAndView;
    }
    
    @Transactional
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
        String filterName = request.getParameter("filterName");
        String filter = request.getParameter("filter");
        String error = null;
        
        if (StringUtils.isEmpty(filterName)) {
            error = "Filter name must not be empty.";
        }
        if (StringUtils.isEmpty(filter)) {
            error = "Filter must not be empty.";
        }
  
        OnmsFilter filterObject = filterService.createFilter(request.getRemoteUser(), filterName, filter, OnmsFilter.Page.EVENT);
        if (filterObject == null) {
            error = "An error occured while saving the filter.";
        }
        
        ModelAndView modelAndView = new ModelAndView("event/list");
        modelAndView.addObject("filter.create.error", error);
        if (filterObject != null) {
            modelAndView.addObject("filterId", filterObject.getId());
            modelAndView.addObject(filterObject.getFilter());
        }
        return modelAndView;
    }
    

//    @Override
//    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        
//        
//        List<OnmsFilter> userFilterList = filterService.getFilters(request.getRemoteUser(), OnmsFilter.Page.EVENT);
//        ModelAndView modelAndView = new ModelAndView("event/index");
//        modelAndView.addObject("filters", userFilterList);
//        return modelAndView;
//    }

//    public void setFilterService(FilterService filterService) {
//        this.filterService = filterService;
//    }
}
