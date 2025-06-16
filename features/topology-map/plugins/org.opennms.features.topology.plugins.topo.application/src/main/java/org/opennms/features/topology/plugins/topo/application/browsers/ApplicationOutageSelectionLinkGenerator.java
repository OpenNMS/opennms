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
