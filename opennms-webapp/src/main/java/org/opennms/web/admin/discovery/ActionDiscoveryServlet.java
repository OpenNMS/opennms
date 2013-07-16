/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.discovery;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ActionDiscoveryServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(ActionDiscoveryServlet.class);

 
    private static final long serialVersionUID = 2L;
    
    /** Constant <code>log</code> */
    
    
    /** Constant <code>addSpecificAction="AddSpecific"</code> */
    public static String addSpecificAction = "AddSpecific";
    /** Constant <code>removeSpecificAction="RemoveSpecific"</code> */
    public static String removeSpecificAction = "RemoveSpecific";
    
    /** Constant <code>addIncludeRangeAction="AddIncludeRange"</code> */
    public static String addIncludeRangeAction = "AddIncludeRange";
    /** Constant <code>removeIncludeRangeAction="RemoveIncludeRange"</code> */
    public static String removeIncludeRangeAction = "RemoveIncludeRange";

    /** Constant <code>addIncludeUrlAction="AddIncludeUrl"</code> */
    public static String addIncludeUrlAction = "AddIncludeUrl";
    /** Constant <code>removeIncludeUrlAction="RemoveIncludeUrl"</code> */
    public static String removeIncludeUrlAction = "RemoveIncludeUrl";

    /** Constant <code>addExcludeRangeAction="AddExcludeRange"</code> */
    public static String addExcludeRangeAction = "AddExcludeRange";
    /** Constant <code>removeExcludeRangeAction="RemoveExcludeRange"</code> */
    public static String removeExcludeRangeAction = "RemoveExcludeRange";
    
    /** Constant <code>saveAndRestartAction="SaveAndRestart"</code> */
    public static String saveAndRestartAction = "SaveAndRestart";
    
    
	/** {@inheritDoc} */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    LOG.info("Loading Discovery configuration.");
        HttpSession sess = request.getSession(true);
        DiscoveryConfiguration config = (DiscoveryConfiguration) sess.getAttribute("discoveryConfiguration");
        if (config == null) {
            config = ModifyDiscoveryConfigurationServlet.getDiscoveryConfig();
            sess.setAttribute("discoveryConfiguration", config);
        }
        //load current general settings
        config = GeneralSettingsLoader.load(request,config);
        
        String action = request.getParameter("action");
        LOG.debug("action: {}", action);
        

        
        //add a Specific
        if(action.equals(addSpecificAction)){
        	LOG.debug("Adding Specific");
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
        	LOG.debug("Removing Specific");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	Specific spec= config.getSpecific(index);
        	boolean result = config.removeSpecific(spec);
        	LOG.debug("Removing Specific result = {}", result);
        } 

        
        //add an 'Include Range'
        if(action.equals(addIncludeRangeAction)){
        	LOG.debug("Adding Include Range");
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
        	LOG.debug("Removing Include Range");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	IncludeRange ir= config.getIncludeRange(index);
        	boolean result = config.removeIncludeRange(ir);
        	LOG.debug("Removing Include Range result = {}", result);
        } 
        
        //add an 'Include URL'
        if(action.equals(addIncludeUrlAction)){
            LOG.debug("Adding Include URL");
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
            LOG.debug("Removing Include URL");
            String specificIndex = request.getParameter("index");
            int index = WebSecurityUtils.safeParseInt(specificIndex);
            IncludeUrl iu = config.getIncludeUrl(index);
            boolean result = config.removeIncludeUrl(iu);
            LOG.debug("Removing Include URL result = {}", result);
        } 
        
        //add an 'Exclude Range'
        if(action.equals(addExcludeRangeAction)){
        	LOG.debug("Adding Exclude Range");
        	String ipAddrBegin = request.getParameter("erbegin");
        	String ipAddrEnd = request.getParameter("erend");
        	ExcludeRange newER = new ExcludeRange();
        	newER.setBegin(ipAddrBegin);
        	newER.setEnd(ipAddrEnd);
        	config.addExcludeRange(newER);
        }

        //remove 'Exclude Range' from configuration
        if(action.equals(removeExcludeRangeAction)){
        	LOG.debug("Removing Exclude Range");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
        	ExcludeRange er= config.getExcludeRange(index);
        	boolean result = config.removeExcludeRange(er);
        	LOG.debug("Removing Exclude Range result = {}", result);
        }         
        
        //save configuration and restart discovery service
        if(action.equals(saveAndRestartAction)){
        	DiscoveryConfigFactory dcf=null;
        	try{
        			StringWriter configString = new StringWriter();
        			config.marshal(configString);
        			LOG.debug(configString.toString().trim());
        		DiscoveryConfigFactory.init();
        		dcf = DiscoveryConfigFactory.getInstance();
            	        dcf.saveConfiguration(config);
        	}catch(Throwable ex){
        		LOG.error("Error while saving configuration. {}", ex);
        		throw new ServletException(ex);
        	}
        	
        	EventProxy proxy = null;
        	try {
    			proxy = Util.createEventProxy();
    		} catch (Throwable me) {
    			LOG.error(me.getMessage());
    		}

    		EventBuilder bldr = new EventBuilder(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI, "ActionDiscoveryServlet");
    		bldr.setHost("host");

            try {
            	proxy.send(bldr.getEvent());
            } catch (Throwable me) {
    			LOG.error(me.getMessage());
    		}

            LOG.info("Restart Discovery requested!");  
            sess.removeAttribute("discoveryConfiguration");
            response.sendRedirect(Util.calculateUrlBase( request, "event/query?msgmatchany=Discovery" ));
            return;
        }
        
        sess.setAttribute("discoveryConfiguration", config);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/edit-config.jsp");
        dispatcher.forward(request, response);
    }
	
	/** {@inheritDoc} */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
