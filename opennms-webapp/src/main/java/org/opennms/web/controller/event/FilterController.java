package org.opennms.web.controller.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.OnmsFilter;
import org.opennms.web.services.FilterService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class FilterController extends MultiActionController implements InitializingBean {

	@Autowired
	private FilterService filterService;
	        
    @Transactional(readOnly=false)
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filterName = getFilterName(request); 
        String filter = getFilter(request);
        String error = null;
        OnmsFilter filterObject = null;
        
        if (StringUtils.isEmpty(filterName)) {
            error = "Filter name must not be empty.";
        }
        if (StringUtils.isEmpty(filter)) {
            error = "Filter must not be empty.";
        }
        
        if (error == null) {
        	filterObject = filterService.createFilter(request.getRemoteUser(), filterName, filter, OnmsFilter.Page.EVENT);
        	if (filterObject == null) {
        		error = "An error occured while saving the filter.";
        	}
        }
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("filter.create.error", error);
        if (filterObject != null) {
            modelAndView.addObject("filterId", filterObject.getId());
            modelAndView.addObject("filterName", filterObject.getName());
        }
        return modelAndView;
    }
    
    @Transactional(readOnly=false) 
	public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) {
    	String filterId = request.getParameter("filterId");
    	boolean success = filterService.deleteFilter(filterId, request.getRemoteUser());
    	
    	ModelAndView modelAndView = new ModelAndView();
    	if (!success) {
    		modelAndView.addObject("filter.delete.error", "Filter couldn't be deleted.");
    	}
		return modelAndView;
	}

	@Override
    public void afterPropertiesSet() throws Exception {
    	BeanUtils.assertAutowiring(this);
    }
    
    private String getFilterName(HttpServletRequest request) {
    	return request.getParameter("filterName");
    }
    
    private String getFilter(HttpServletRequest request) {
    	String filter = request.getParameter("filter");
    	if (filter == null) return null;
    	if (filter.startsWith("&amp;")) {
    		filter = filter.replace("&amp;", "");
    	}
    	if (filter.startsWith("&")) {
    		filter = filter.replace("&", "");
    	}
    	return filter;
    }
}
