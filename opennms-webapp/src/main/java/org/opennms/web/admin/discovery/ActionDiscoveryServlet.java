/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.admin.discovery;

import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addExcludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addIncludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addIncludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addExcludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.addSpecificAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeExcludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeIncludeRangeAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeIncludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeExcludeUrlAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.removeSpecificAction;
import static org.opennms.web.admin.discovery.DiscoveryServletConstants.saveAndRestartAction;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.LocationUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.ExcludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles updating the status of the notifications
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ActionDiscoveryServlet extends HttpServlet {

    /** Constant <code>log</code> */
    private static final Logger LOG = LoggerFactory.getLogger(ActionDiscoveryServlet.class);

    private static final long serialVersionUID = 2L;

    public static final String ATTRIBUTE_DISCOVERY_CONFIGURATION = ActionDiscoveryServlet.class.getSimpleName() + "-discoveryConfiguration";

    /**
     * <p>getDiscoveryConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
     * @throws ServletException 
     */
    public static DiscoveryConfiguration getDiscoveryConfig() throws ServletException {
        DiscoveryConfiguration config = null;
        try {
            DiscoveryConfigFactory factory = DiscoveryConfigFactory.getInstance();
            factory.reload();
            config = factory.getConfiguration();
        } catch (final Exception e) {
            throw new ServletException("Could not load configuration: " + e.getMessage(), e);
        }
        return config;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOG.info("Loading Discovery configuration.");
        HttpSession sess = request.getSession(true);
        DiscoveryConfiguration config = (DiscoveryConfiguration) sess.getAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION);
        if (config == null) {
            config = getDiscoveryConfig();
            sess.setAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION, config);
        }

        //Update general settings from the incoming request parameters
        config = GeneralSettingsLoader.load(request, config);

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

            if (!LocationUtils.doesLocationsMatch(location,
                    config.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME))) {
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

            if (!LocationUtils.doesLocationsMatch(location,
                    config.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME))) {
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
            String url = WebSecurityUtils.sanitizeString(request.getParameter("iuurl"));
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

            if (!LocationUtils.doesLocationsMatch(location,
                    config.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME))) {
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

        //add an 'Exclude URL'
        if(action.equals(addExcludeUrlAction)){
            LOG.debug("Adding Exclude URL");
            String url = WebSecurityUtils.sanitizeString(request.getParameter("euurl"));
            String foreignSource = request.getParameter("euforeignsource");
            String location = request.getParameter("eulocation");

            ExcludeUrl eu = new ExcludeUrl();
            eu.setUrl(url);

            if(foreignSource!=null && !"".equals(foreignSource.trim()) && !foreignSource.equals(config.getForeignSource().orElse(null))){
                eu.setForeignSource(foreignSource);
            }

            if (!LocationUtils.doesLocationsMatch(location,
                    config.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME))) {
                eu.setLocation(location);
            }

            config.addExcludeUrl(eu);
        }

        //remove 'Exclude URL' from configuration
        if(action.equals(removeExcludeUrlAction)){
            LOG.debug("Removing Exclude URL");
            String specificIndex = request.getParameter("index");
            int index = WebSecurityUtils.safeParseInt(specificIndex);
            final int index1 = index;
            ExcludeUrl eu = config.getExcludeUrls().get(index1);
            boolean result = config.removeExcludeUrl(eu);
            LOG.debug("Removing Exclude URL result = {}", result);
        }

        //add an 'Exclude Range'
        if(action.equals(addExcludeRangeAction)){
            LOG.debug("Adding Exclude Range");
            String ipAddrBegin = request.getParameter("erbegin");
            String ipAddrEnd = request.getParameter("erend");
            String location = request.getParameter("erlocation");
            ExcludeRange newER = new ExcludeRange();
            newER.setBegin(ipAddrBegin);
            newER.setEnd(ipAddrEnd);
            if (!LocationUtils.doesLocationsMatch(location,
                    config.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME))) {
                newER.setLocation(location);
            }
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

        //save configuration and restart discovery service
        if(action.equals(saveAndRestartAction)){
            DiscoveryConfigFactory dcf=null;
            try{
                StringWriter configString = new StringWriter();
                JaxbUtils.marshal(config, configString);
                LOG.debug(configString.toString().trim());
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

            if (proxy == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "unable to get event proxy");
                return;
            }

            EventBuilder bldr = new EventBuilder(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI, "ActionDiscoveryServlet");
            bldr.setHost("host");

            try {
                proxy.send(bldr.getEvent());
            } catch (Throwable me) {
                LOG.error(me.getMessage());
            }

            LOG.info("Restart Discovery requested!");  
            sess.removeAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION);
            response.sendRedirect(Util.calculateUrlBase( request, "admin/discovery/config-done.jsp" ));
            return;
        }

        sess.setAttribute(ATTRIBUTE_DISCOVERY_CONFIGURATION, config);
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/discovery/edit-config.jsp");
        dispatcher.forward(request, response);
    }

    /** {@inheritDoc} */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
