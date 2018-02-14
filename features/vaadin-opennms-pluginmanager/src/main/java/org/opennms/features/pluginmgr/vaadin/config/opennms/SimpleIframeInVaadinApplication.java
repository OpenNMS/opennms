/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.pluginmgr.vaadin.config.opennms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.opennms.web.api.OnmsHeaderProvider;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin Manager Administration Application.
 * 
 * SimpleIframeInVaadinApplication embeds an iframe reference in a Vaadin generated UI
 */
@Theme("opennms")
@Title("Plugin Manager Administration")
@SuppressWarnings("serial")
public class SimpleIframeInVaadinApplication extends UI {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleIframeInVaadinApplication.class);

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	private OnmsHeaderProvider m_headerProvider;
	private String m_headerHtml;

	private VerticalLayout m_rootLayout;

	private String iframePageUrl;

	private Map<String, String> headerLinks;

	/**
	 * headerLinks map of key= name and value=url for links to be placed in header of page
	 * @return
	 */
	public Map<String,String> getHeaderLinks() {
		return headerLinks;
	}

	/**
	 * @param headerLinks map of key= name and value=url for links to be placed in header of page
	 */
	public void setHeaderLinks(Map<String,String> headerLinks) {
		this.headerLinks = headerLinks;
	}

	public String getIframePageUrl() {
		return iframePageUrl;
	}

	public void setIframePageUrl(String iframePageUrl) {
		this.iframePageUrl = iframePageUrl;
	}

	// used ideas for adding OpenNMS header
	// from org.opennms.features.vaadin.nodemaps.internal.NodeMapsApplication

	public OnmsHeaderProvider getHeaderProvider() {
		return m_headerProvider;
	}

	public void setHeaderProvider(OnmsHeaderProvider headerProvider) {
		this.m_headerProvider = headerProvider;
	}

	public void setHeaderHtml(final String headerHtml) {
		m_headerHtml = headerHtml;
	}

	private void addHeader(VaadinRequest request) {
		if (m_headerProvider != null) {
			try {
				setHeaderHtml(m_headerProvider.getHeaderHtml(((VaadinServletRequest) request).getHttpServletRequest()));
			} catch (final Exception e) {
				LOG.error("failed to get header HTML for request " + request.getPathInfo(), e.getCause());
			}
		}
		if (m_headerHtml != null) {
			InputStream is = null;
			try {
				is = new ByteArrayInputStream(m_headerHtml.getBytes());
				final CustomLayout headerLayout = new CustomLayout(is);
				headerLayout.setWidth("100%");
				headerLayout.addStyleName("onmsheader");
				m_rootLayout.addComponent(headerLayout);
			} catch (final IOException e) {
				closeQuietly(is);
				LOG.debug("failed to get header layout data", e);
			}
		}
	}

	private void closeQuietly(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (final IOException closeE) {
				LOG.debug("failed to close HTML input stream", closeE);
			}
		}
	}



	/* (non-Javadoc)
	 * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
	 */
	@Override
	public void init(VaadinRequest request) {
		if (iframePageUrl==null) throw new RuntimeException("iframePageUrl must not be null");

		m_rootLayout = new VerticalLayout();
		m_rootLayout.setSizeFull();
		m_rootLayout.addStyleName("root-layout");
		setContent(m_rootLayout);
		addHeader(request);

		//add diagnostic page links
		if(headerLinks!=null) {
			// defining 2 horizontal layouts to force links to stay together
			HorizontalLayout horizontalLayout1= new HorizontalLayout();
			horizontalLayout1.setWidth("100%");
			horizontalLayout1.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
			HorizontalLayout horizontalLayout2= new HorizontalLayout();
			horizontalLayout1.addComponent(horizontalLayout2);

			for(String name: headerLinks.keySet()){
				String urlStr=headerLinks.get(name);
				ExternalResource urlResource=new ExternalResource(urlStr);
				Link link = new Link(name, urlResource);
				Label label= new Label("&nbsp;&nbsp;&nbsp;", ContentMode.HTML); // adds space between links
				horizontalLayout2.addComponent(link);
				horizontalLayout2.addComponent(label);
			}
			m_rootLayout.addComponent(horizontalLayout1);
		}

		ExternalResource iframPageResource = new ExternalResource(iframePageUrl);

		BrowserFrame browser = new BrowserFrame("", iframPageResource);
		browser.setWidth("100%");
		browser.setHeight("100%");
		m_rootLayout.addComponent(browser);

		// this forces the UI panel to use up all the available space below the header
		m_rootLayout.setExpandRatio(browser, 1.0f);

	}
}
