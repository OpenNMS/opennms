package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DaemonStatusController extends SimpleFormController {
	 private DaemonStatusService daemonStatusService;

     public void setDaemonStatusService(DaemonStatusService daemonStatusService) {
    	 this.daemonStatusService = daemonStatusService;
     }

	@Override
	protected Map referenceData(HttpServletRequest arg0) throws Exception {
		// TODO Auto-generated method stub
		Map referenceData = new HashMap();
		Collection daemons = daemonStatusService.getCurrentDaemonStatusColl();
		logger.debug("number of daemons:" + daemons.size());
		referenceData.put("daemons", daemons);
		return referenceData;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse arg1, Object arg2, BindException arg3) throws Exception {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> daemons = 
			daemonStatusService.performOperationOnDaemons(ServletRequestUtils.getStringParameter(request, "operation"),
				ServletRequestUtils.getStringParameters(request, "values"));
		ModelAndView modelAndView = super.onSubmit(request, arg1, arg2, arg3);
		modelAndView.addAllObjects(referenceData(request));
		return modelAndView;
	}
      
	@Override
	protected Object formBackingObject(HttpServletRequest arg0) throws Exception {
		// TODO Auto-generated method stub
		return super.formBackingObject(arg0);
	}
}
