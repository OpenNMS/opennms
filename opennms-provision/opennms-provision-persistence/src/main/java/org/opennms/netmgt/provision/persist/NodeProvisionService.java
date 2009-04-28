package org.opennms.netmgt.provision.persist;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.ModelAndView;

public interface NodeProvisionService {

    public ModelAndView getModelAndView(HttpServletRequest request) ;
    
    public boolean provisionNode(String foreignSource, String foreignId, String nodeLabel, String ipAddress,
            String[] categories,
            String snmpCommunity, String snmpVersion,
            String deviceUsername, String devicePassword, String enablePassword) throws Exception;
}
