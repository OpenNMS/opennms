/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.subscriptionchecker.web.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.subscriptionchecker.internal.Model;
import org.opennms.web.api.HtmlInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ModalInjector implements HtmlInjector {
	private static final Logger LOG = LoggerFactory.getLogger(ModalInjector.class);
	
    private Model m_model;


    @Override
    public String inject(HttpServletRequest request) throws TemplateException, IOException {  
    	boolean isIndexPage= isPage("/opennms/index.jsp", request);
    	
    	if(LOG.isDebugEnabled()) LOG.debug("try injecting subscription modal:"+m_model+" isIndexPage /opennms/index.jsp:"+isIndexPage);
    	
        if (m_model.isEnabled() == true && isIndexPage ) {
            return generateModalHtml(isUserInAdminRole(request), 
            		m_model.getSubscriptionGroupId(),
                	m_model.getSubscriptionArtifactId(),
                	m_model.getSubscriptionVersion(),
                	m_model.getAdminMessage(),
                	m_model.getUserMessage()
                	);
        }
        return null;
    }

    protected static String generateModalHtml(boolean userInAdminRole, String subscriptionGroupId, String subscriptionArtifactId, 
    		String subscriptionVersion, String adminMessage, String userMessage) throws IOException, TemplateException {
    	
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        ClassTemplateLoader ctl = new ClassTemplateLoader(ModalInjector.class, "/web");
        cfg.setTemplateLoader(ctl);
        // Load the template
        Template template = cfg.getTemplate("modal.ftl.html");
        // Build out model
        Map<String, Object> data = Maps.newHashMap();
        
        data.put("userInAdminRole", userInAdminRole);
        data.put("subscriptionGroupId", subscriptionGroupId);
        data.put("subscriptionArtifactId", subscriptionArtifactId);
        data.put("subscriptionVersion", subscriptionVersion);
        
        data.put("adminMessage", adminMessage);
        data.put("userMessage", userMessage);
        
        // Render to string
        Writer out = new StringWriter();
        template.process(data, out);
        out.flush();
        return out.toString();
    }

    protected static boolean isPage(String endOfUri, HttpServletRequest request) {
        final String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        return uri.endsWith(endOfUri);
    }

    protected static boolean isUserInAdminRole(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN");
    }


	public void setModel(Model model) {
		m_model = model;
    	if(LOG.isDebugEnabled()) LOG.debug("subscription checker settings:"+m_model);
	}


}
