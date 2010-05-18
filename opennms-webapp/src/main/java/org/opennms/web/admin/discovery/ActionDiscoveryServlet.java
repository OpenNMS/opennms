//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 29: Use Util.createEventProxy instead of creating a TcpEventProxy
//              on our own. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.admin.discovery;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles updating the status of the notifications
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ActionDiscoveryServlet extends HttpServlet {
 
    private static final long serialVersionUID = 2L;
    
    protected static ThreadCategory log = ThreadCategory.getInstance("WEB");
    
    
    public static String addSpecificAction = "AddSpecific";
    public static String removeSpecificAction = "RemoveSpecific";
    
    public static String addIncludeRangeAction = "AddIncludeRange";
    public static String removeIncludeRangeAction = "RemoveIncludeRange";

    public static String addIncludeUrlAction = "AddIncludeUrl";
    public static String removeIncludeUrlAction = "RemoveIncludeUrl";

    public static String addExcludeRangeAction = "AddExcludeRange";
    public static String removeExcludeRangeAction = "RemoveExcludeRange";
    
    public static String saveAndRestartAction = "SaveAndRestart";
    
    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    log.info("Loading Discovery configuration.");
        HttpSession sess = request.getSession(true);
        DiscoveryConfiguration config = (DiscoveryConfiguration) sess.getAttribute("discoveryConfiguration");
        //load current general settings
        config = GeneralSettingsLoader.load(request,config);
        
        String action = request.getParameter("action");
        log.debug("action: "+action);
        

        
        //add a Specific
        if(action.equals(addSpecificAction)){
        	log.debug("Adding Specific");
        	String ipAddr = request.getParameter("specificipaddress");
        	String timeout = request.getParameter("specifictimeout");
        	String retries = request.getParameter("specificretries");
        	Specific newSpecific = new Specific();
        	newSpecific.setContent(ipAddr);
        	if(timeout!=null && !timeout.trim().equals("") && !timeout.equals(config.getTimeout())){
        		newSpecific.setTimeout(WebSecurityUtils.safeParseLong(timeout));
        	}

        	if(retries!=null && !retries.trim().equals("") && !retries.equals(config.getRetries())){
        		newSpecific.setRetries(WebSecurityUtils.safeParseInt(retries));
        	}
        	config.addSpecific(newSpecific);
        }

        //remove 'Specific' from configuration
        if(action.equals(removeSpecificAction)){
        	log.debug("Removing Specific");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	Specific spec= config.getSpecific(index);
        	boolean result = config.removeSpecific(spec);
        	log.debug("Removing Specific result = "+result);
        } 

        
        //add an 'Include Range'
        if(action.equals(addIncludeRangeAction)){
        	log.debug("Adding Include Range");
        	String ipAddrBase = request.getParameter("irbase");
        	String ipAddrEnd = request.getParameter("irend");
        	String timeout = request.getParameter("irtimeout");
        	String retries = request.getParameter("irretries");
        	IncludeRange newIR = new IncludeRange();
        	newIR.setBegin(ipAddrBase);
        	newIR.setEnd(ipAddrEnd);
        	if(timeout!=null && !timeout.trim().equals("") && !timeout.equals(config.getTimeout())){
        		newIR.setTimeout(WebSecurityUtils.safeParseLong(timeout));
        	}
        	if(retries!=null && !retries.trim().equals("") && !retries.equals(config.getRetries())){
        		newIR.setRetries(WebSecurityUtils.safeParseInt(retries));
        	}
        	config.addIncludeRange(newIR);
        }

        //remove 'Include Range' from configuration
        if(action.equals(removeIncludeRangeAction)){
        	log.debug("Removing Include Range");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	IncludeRange ir= config.getIncludeRange(index);
        	boolean result = config.removeIncludeRange(ir);
        	log.debug("Removing Include Range result = "+result);
        } 
        
        //add an 'Include URL'
        if(action.equals(addIncludeUrlAction)){
            log.debug("Adding Include URL");
            String url = request.getParameter("iuurl");
            String timeout = request.getParameter("iutimeout");
            String retries = request.getParameter("iuretries");

            IncludeUrl iu = new IncludeUrl();
            iu.setContent(url);
            if(timeout!=null && !timeout.trim().equals("") && !timeout.equals(config.getTimeout())){
                iu.setTimeout(WebSecurityUtils.safeParseLong(timeout));
            }
            if(retries!=null && !retries.trim().equals("") && !retries.equals(config.getRetries())){
                iu.setRetries(WebSecurityUtils.safeParseInt(retries));
            }
            config.addIncludeUrl(iu);
        }

        //remove 'Include URL' from configuration
        if(action.equals(removeIncludeUrlAction)){
            log.debug("Removing Include URL");
            String specificIndex = request.getParameter("index");
            int index = WebSecurityUtils.safeParseInt(specificIndex);
            IncludeUrl iu = config.getIncludeUrl(index);
            boolean result = config.removeIncludeUrl(iu);
            log.debug("Removing Include URL result = "+result);
        } 
        
        //add an 'Exclude Range'
        if(action.equals(addExcludeRangeAction)){
        	log.debug("Adding Exclude Range");
        	String ipAddrBegin = request.getParameter("erbegin");
        	String ipAddrEnd = request.getParameter("erend");
        	ExcludeRange newER = new ExcludeRange();
        	newER.setBegin(ipAddrBegin);
        	newER.setEnd(ipAddrEnd);
        	config.addExcludeRange(newER);
        }

        //remove 'Exclude Range' from configuration
        if(action.equals(removeExcludeRangeAction)){
        	log.debug("Removing Exclude Range");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	ExcludeRange er= config.getExcludeRange(index);
        	boolean result = config.removeExcludeRange(er);
        	log.debug("Removing Exclude Range result = "+result);
        }         
        
        //save configuration and restart discovery service
        if(action.equals(saveAndRestartAction)){
        	DiscoveryConfigFactory dcf=null;
        	try{
        		if (log.isDebugEnabled()) {
        			StringWriter configString = new StringWriter();
        			config.marshal(configString);
        			log.debug(configString.toString().trim());
        		}
        		DiscoveryConfigFactory.init();
        		dcf = DiscoveryConfigFactory.getInstance();
            	        dcf.saveConfiguration(config);
        	}catch(Exception ex){
        		log.error("Error while saving configuration. "+ex);
        		throw new ServletException(ex);
        	}
        	
        	EventProxy proxy = null;
        	try {
    			proxy = Util.createEventProxy();
    		} catch (Exception me) {
    			log.error(me.getMessage());
    		}

    		Event event = new Event();
            event.setUei(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI);
            event.setSource("ActionDiscoveryServlet");
            event.setHost("host");
            event.setTime(EventConstants.formatToString(new java.util.Date()));

            try {
            	proxy.send(event);
            } catch (Exception me) {
    			log.error(me.getMessage());
    		}

            log.info("Restart Discovery requested!");  
            sess.removeAttribute("discoveryConfiguration");
            response.sendRedirect(org.opennms.web.Util.calculateUrlBase( request )+"event/query?msgmatchany=Discovery");
            return;
        }
        
        sess.setAttribute("discoveryConfiguration", config);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/edit-config.jsp");
        dispatcher.forward(request, response);
    }
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
