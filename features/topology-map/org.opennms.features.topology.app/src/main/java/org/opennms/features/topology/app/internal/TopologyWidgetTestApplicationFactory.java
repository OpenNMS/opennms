/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.web.api.OnmsHeaderProvider;
import org.ops4j.pax.vaadin.AbstractApplicationFactory;
import org.ops4j.pax.vaadin.ScriptTag;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.vaadin.Application;

public class TopologyWidgetTestApplicationFactory extends AbstractApplicationFactory {
    
	private final BlueprintContainer m_blueprintContainer;
	private final String m_beanName;
	private OnmsHeaderProvider m_headerProvider;
	
	public TopologyWidgetTestApplicationFactory(BlueprintContainer container, String beanName) {
		m_blueprintContainer = container;
		m_beanName = beanName;
	}
	
    @Override
	public Application createApplication(HttpServletRequest request) throws ServletException {
        TopologyWidgetTestApplication application = (TopologyWidgetTestApplication) m_blueprintContainer.getComponentInstance(m_beanName);
        application.setHeaderHtml(getHeader(request));
        application.setUser(request.getRemoteUser());
        LoggerFactory.getLogger(getClass()).debug(MessageFormatter.format("created {} for servlet path {}", application, request.getServletPath()).getMessage()/* , new Exception("Show me the stack trace") */);
        return application;
	}


    private String getHeader(HttpServletRequest request) {
        if(m_headerProvider == null) return "";
        
        return m_headerProvider.getHeaderHtml(request);
    }

    @Override
	public Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		return TopologyWidgetTestApplication.class;
	}

    @Override
    public Map<String, String> getAdditionalHeaders() {
        final Map<String,String> headers = new HashMap<String,String>();
        headers.put("X-UA-Compatible", "chrome=1");
        return headers;
    }

    @Override
    public List<ScriptTag> getAdditionalScripts() {
        final List<ScriptTag> tags = new ArrayList<ScriptTag>();
        tags.add(new ScriptTag("http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js", "text/javascript", null));
        tags.add(new ScriptTag(null, "text/javascript", "CFInstall.check({ mode: \"overlay\" });"));
        return tags;
    }
    
    public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
        m_headerProvider = headerProvider;
    }
}
