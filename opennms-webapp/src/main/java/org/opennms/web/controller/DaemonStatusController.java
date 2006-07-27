package org.opennms.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DaemonStatusController extends SimpleFormController {
	 private DaemonStatusService daemonStatusService;

     public void setEntityService(DaemonStatusService daemonStatusService) {
    	 this.daemonStatusService = daemonStatusService;
     }

     @Override
     protected Object formBackingObject(HttpServletRequest request) throws Exception {
         String selectedservices[] =  ServletRequestUtils.getStringParameters(request, "selectedservices");
         String operation =  ServletRequestUtils.getStringParameter(request, "operation");
         return daemonStatusService.performOperationOnDaemons(operation, selectedservices);
     }

     
}
