package org.opennms.web.controller.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.OnmsFilter;
import org.opennms.web.services.FavoriteFilterService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class FavoriteFilterController extends MultiActionController  {

//	@Autowired
//	private FavoriteFilterService filterService;
	        
//    @Transactional(readOnly=false)
//    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        ModelAndView modelAndView = new ModelAndView();
//        try {
//            String error = null;
//            OnmsFilter favorite = filterService.createFilter(request.getRemoteUser(), request.getParameter("filterName"), request.getParameter("filter"), OnmsFilter.Page.EVENT);
//            if (favorite == null) throw new FavoriteFilterService.FavoriteFilterException("An error occured while creating the filter");
//            modelAndView.addObject("favorite", modelAndView);
//        } catch (FavoriteFilterService.FavoriteFilterException ex) {
//            modelAndView.addObject("favorite.create.error", ex.getMessage());
//        }
//        return modelAndView;
//    }
    
//    @Transactional(readOnly=false)
//	public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) {
//    	String filterId = request.getParameter("favoriteId");
//    	boolean success = filterService.deleteFilter(filterId, request.getRemoteUser());
//
//    	ModelAndView modelAndView = new ModelAndView();
//    	if (!success) {
//    		modelAndView.addObject("favorite.delete.error", "Filter couldn't be deleted.");
//    	}
//		return modelAndView;
//	}

//	@Override
//    public void afterPropertiesSet() throws Exception {
//    	BeanUtils.assertAutowiring(this);
//    }
    

}
