/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools;

import java.util.Iterator;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * {@link DisclosurePanel} that stores if its collaped or not in a cookie.
 * First added {@link Widget} is used as header.
 */
public class DisclosurePanelCookie extends Composite implements HasWidgets {

	private DisclosurePanel panel = new DisclosurePanel();

	@UiConstructor
	public DisclosurePanelCookie(final String cookieName) {
		
		panel.setStyleName("DisclosurePanelCookie");
		panel.setAnimationEnabled(true);

		if (Cookies.isCookieEnabled()) {
			// prepare Cookie if not already set
			if (Cookies.getCookie(cookieName + "Open") == null) {
				Cookies.setCookie(cookieName + "Open", "true");
			}

			// check cookie and set open/close by cookie-value
			if (Cookies.getCookie(cookieName + "Open").equals("true")) {
				panel.setOpen(true);
			} else {
				panel.setOpen(false);
			}

			// add openhandler that sets open/true to cookie
			panel.addOpenHandler(new OpenHandler<DisclosurePanel>() {
				@Override
				public void onOpen(OpenEvent<DisclosurePanel> event) {
					Cookies.setCookie(cookieName + "Open", "true");
				}
			});

			// add closehandler that sets close/flase to cookie
			panel.addCloseHandler(new CloseHandler<DisclosurePanel>() {
				@Override
				public void onClose(CloseEvent<DisclosurePanel> event) {
					Cookies.setCookie(cookieName + "Open", "false");
				}
			});
		}
		initWidget(panel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.user.client.ui.HasWidgets#add(com.google.gwt.user.client
	 * .ui.Widget)
	 */
	@Override
	public void add(Widget widget) {
		if (panel.getHeader() == null) {
			panel.setHeader(widget);
		} else {
			panel.setContent(widget);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasWidgets#clear()
	 */
	@Override
	public void clear() {
		panel.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.HasWidgets#iterator()
	 */
	@Override
	public Iterator<Widget> iterator() {
		return panel.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.gwt.user.client.ui.HasWidgets#remove(com.google.gwt.user.client
	 * .ui.Widget)
	 */
	@Override
	public boolean remove(Widget widget) {
		return panel.remove(widget);
	}
}
