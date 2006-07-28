package org.opennms.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;
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
		referenceData.put("deamons", daemonStatusService.getCurrentDaemonStatus());
		return referenceData;
	}

      
}
