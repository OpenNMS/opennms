/*
 * Copyright 2012 Achim Nierbeck.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.vaadin.extender.internal.servlet;

import org.opennms.vaadin.extender.Constants;
import org.opennms.vaadin.extender.VaadinResourceService;
import org.osgi.framework.Bundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VaadinResourceServlet extends HttpServlet implements VaadinResourceService {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final String VAADIN = Constants.VAADIN_PATH;

	private final Bundle vaadin;

	private final List<Bundle> resourceBundles = new ArrayList<Bundle>();

	public VaadinResourceServlet(Bundle vaadin) {
		this.vaadin = vaadin;
	}

	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException,
			IOException {
		String path = req.getPathInfo();
		String resourcePath = VAADIN + path;

		String contentType = getServletContext().getMimeType(resourcePath);
		if (contentType != null) {
		    resp.setContentType(contentType);
		}

		URL resourceUrl = vaadin.getResource(resourcePath);

		if (null == resourceUrl) {
			resourceUrl = loadFromResources(resourcePath);
		}

		if (null == resourceUrl) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		InputStream in = resourceUrl.openStream();
		OutputStream out = resp.getOutputStream();

		byte[] buffer = new byte[1024];
		int read = 0;
		while (-1 != (read = in.read(buffer))) {
			out.write(buffer, 0, read);
		}
	}

	private URL loadFromResources(String resourcePath) {
		for (Bundle resourceBundle : resourceBundles) {
			URL resourceUrl = resourceBundle.getResource(resourcePath);
			if (null != resourceUrl)
				return resourceUrl;
		}
		return null;
	}

	@Override
	public void addResources(Bundle bundle) {
		resourceBundles.add(bundle);
	}

	@Override
	public void removeResources(Bundle bundle) {
		resourceBundles.remove(bundle);
	}

}
