/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.application.browsers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.opennms.features.topology.api.browsers.AbstractSelectionLinkGenerator;
import org.opennms.features.topology.api.support.DialogWindow;
import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.themes.BaseTheme;

public class ApplicationOutageSelectionLinkGenerator extends AbstractSelectionLinkGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationOutageSelectionLinkGenerator.class);
	private final String idPropertyName;
	private final OutageDao outageDao;

    public ApplicationOutageSelectionLinkGenerator(String idPropertyName, final OutageDao outageDao) {
		this.idPropertyName = idPropertyName;
		this.outageDao = outageDao;
	}

	@Override
	public Object generateCell(final Table source, Object itemId, Object columnId) {
		if (source == null) return null; // no source
		Property<Integer> idProperty = source.getContainerProperty(itemId,  idPropertyName);
		final Integer outageId = idProperty.getValue();
		if (outageId == null) return null; // no value

		// create Link
		Button button = new Button("" + outageId);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 3698209256202413810L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				// try if outage is there, otherwise show information dialog
				OnmsOutage outage = outageDao.get(outageId);
				if (outage == null) {
					new DialogWindow(source.getUI(),
							"Outage does not exist!",
							"The outage information cannot be shown. \nThe outage does not exist anymore. \n\nPlease refresh the Outage Table.");
					return;
				}

				// outage still exists, show details
				final URI currentLocation = Page.getCurrent().getLocation();
				final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
				final String redirectFragment = contextRoot + "/outage/detail.htm?quiet=true&id=" + outageId;
				LOG.debug("outage {} clicked, current location = {}, uri = {}", outageId, currentLocation, redirectFragment);

				try {
					source.getUI().addWindow(
							new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), () -> "Outage Info " + outageId));
				} catch (MalformedURLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		});
		return button;
	}

}
