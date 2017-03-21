/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.discovery;

import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addExcludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addIncludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addIncludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addSpecificAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeExcludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeIncludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeIncludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeSpecificAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.saveAndRestartAction;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.discovery.DiscoveryTaskExecutor;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that handles configuring a one-time discovery scan.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class DiscoveryScanServlet extends HttpServlet {

    private static final long serialVersionUID = 1018907997499737690L;

    /** Constant <code>log</code> */
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryScanServlet.class);

    public static final String ATTRIBUTE_DISCOVERY_CONFIGURATION = DiscoveryScanServlet.class.getSimpleName() + "-discoveryConfiguration";

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    LOG.info("Loading Discovery configuration.");
        HttpSession sess = request.getSession(true);
        DiscoveryConfiguration config = (DiscoveryConfiguration) sess.getAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION);
        if (config == null) {
            config = new DiscoveryConfiguration();
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
        	String foreignSource = request.getParameter("specificforeignsource");
        	String location = request.getParameter("specificlocation");
        	Specific newSpecific = new Specific();
        	newSpecific.setAddress(ipAddr);
        	if(timeout!=null && !"".equals(timeout.trim()) && !timeout.equals(String.valueOf(config.getTimeout().orElse(null)))){
        		newSpecific.setTimeout(WebSecurityUtils.safeParseLong(timeout));
        	}

        	if(retries!=null && !"".equals(retries.trim()) && !retries.equals(String.valueOf(config.getRetries().orElse(null)))){
        		newSpecific.setRetries(WebSecurityUtils.safeParseInt(retries));
        	}

        	if(foreignSource!=null && !"".equals(foreignSource.trim()) && !foreignSource.equals(config.getForeignSource().orElse(null))){
        		newSpecific.setForeignSource(foreignSource);
        	}

        	if(location!=null && !"".equals(location.trim()) && !location.equals(config.getLocation().orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID))){
        		newSpecific.setLocation(location);
        	}

        	config.addSpecific(newSpecific);
        }

        //remove 'Specific' from configuration
        if(action.equals(removeSpecificAction)){
        	LOG.debug("Removing Specific");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
            final int index1 = index;
        	Specific spec= config.getSpecifics().get(index1);
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
        	String foreignSource = request.getParameter("irforeignsource");
        	String location = request.getParameter("irlocation");
        	IncludeRange newIR = new IncludeRange();
        	newIR.setBegin(ipAddrBase);
        	newIR.setEnd(ipAddrEnd);

        	if(timeout!=null && !"".equals(timeout.trim()) && !timeout.equals(String.valueOf(config.getTimeout().orElse(null)))){
        		newIR.setTimeout(WebSecurityUtils.safeParseLong(timeout));
        	}

        	if(retries!=null && !"".equals(retries.trim()) && !retries.equals(String.valueOf(config.getRetries().orElse(null)))){
        		newIR.setRetries(WebSecurityUtils.safeParseInt(retries));
        	}

        	if(foreignSource!=null && !"".equals(foreignSource.trim()) && !foreignSource.equals(config.getForeignSource().orElse(null))){
        		newIR.setForeignSource(foreignSource);
        	}

        	if(location!=null && !"".equals(location.trim()) && !location.equals(config.getLocation().orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID))){
        		newIR.setLocation(location);
        	}

        	config.addIncludeRange(newIR);
        }

        //remove 'Include Range' from configuration
        if(action.equals(removeIncludeRangeAction)){
        	LOG.debug("Removing Include Range");
        	String specificIndex = request.getParameter("index");
        	int index = WebSecurityUtils.safeParseInt(specificIndex);
            final int index1 = index;
        	IncludeRange ir= config.getIncludeRanges().get(index1);
        	boolean result = config.removeIncludeRange(ir);
        	LOG.debug("Removing Include Range result = {}", result);
        } 
        
        //add an 'Include URL'
        if(action.equals(addIncludeUrlAction)){
            LOG.debug("Adding Include URL");
            String url = request.getParameter("iuurl");
            String timeout = request.getParameter("iutimeout");
            String retries = request.getParameter("iuretries");
            String foreignSource = request.getParameter("iuforeignsource");
            String location = request.getParameter("iulocation");

            IncludeUrl iu = new IncludeUrl();
            iu.setUrl(url);
            if(timeout!=null && !"".equals(timeout.trim()) && !timeout.equals(String.valueOf(config.getTimeout().orElse(null)))){
                iu.setTimeout(WebSecurityUtils.safeParseLong(timeout));
            }

            if(retries!=null && !"".equals(retries.trim()) && !retries.equals(String.valueOf(config.getRetries().orElse(null)))){
                iu.setRetries(WebSecurityUtils.safeParseInt(retries));
            }

            if(foreignSource!=null && !"".equals(foreignSource.trim()) && !foreignSource.equals(config.getForeignSource().orElse(null))){
                iu.setForeignSource(foreignSource);
            }

            if(location!=null && !"".equals(location.trim()) && !location.equals(config.getLocation().orElse(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID))){
                iu.setLocation(location);
            }

            config.addIncludeUrl(iu);
        }

        //remove 'Include URL' from configuration
        if(action.equals(removeIncludeUrlAction)){
            LOG.debug("Removing Include URL");
            String specificIndex = request.getParameter("index");
            int index = WebSecurityUtils.safeParseInt(specificIndex);
            final int index1 = index;
            IncludeUrl iu = config.getIncludeUrls().get(index1);
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
            final int index1 = index;
        	ExcludeRange er= config.getExcludeRanges().get(index1);
        	boolean result = config.removeExcludeRange(er);
        	LOG.debug("Removing Exclude Range result = {}", result);
        }
        
        // Submit the discovery job
        if(action.equals(saveAndRestartAction)){
        
            try {
                WebApplicationContext beanFactory = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
                // Fetch the ServiceRegistry
                ServiceRegistry registry = beanFactory.getBean(ServiceRegistry.class);
                // Use it to look up a DiscoveryTaskExecutor service
                DiscoveryTaskExecutor executor = registry.findProvider(DiscoveryTaskExecutor.class);
                // If the service exists...
                if (executor != null) {
                    // Submit the job to the discovery queue
                    executor.handleDiscoveryTask(config);
                } else {
                    LOG.warn("No DiscoveryTaskExecutor service is available");
                }
            } catch(Throwable ex) {
                LOG.error("Error while submitting task", ex);
                throw new ServletException(ex);
            }

        	// TODO: Send an event here when the scan is started? Or do it on the Camel side?
        	/*
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
            */

            sess.removeAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION);
            response.sendRedirect(Util.calculateUrlBase( request, "admin/discovery/scan-done.jsp" ));
            return;
        }
        
        sess.setAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION, config);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/edit-scan.jsp");
        dispatcher.forward(request, response);
    }

	/** {@inheritDoc} */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
