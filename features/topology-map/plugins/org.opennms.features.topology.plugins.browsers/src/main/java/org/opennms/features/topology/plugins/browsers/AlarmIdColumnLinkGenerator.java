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
package org.opennms.features.topology.plugins.browsers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.opennms.features.topology.api.support.DialogWindow;
import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.features.topology.api.support.InfoWindow.LabelCreator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.v7.data.Property;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.Table.ColumnGenerator;
import com.vaadin.v7.ui.themes.BaseTheme;

public class AlarmIdColumnLinkGenerator implements ColumnGenerator {
	private static final long serialVersionUID = 621311104480258016L;
	private static final Logger LOG = LoggerFactory.getLogger(AlarmIdColumnLinkGenerator.class);
	private final String alarmIdPropertyName;
	private final AlarmDao alarmDao;
	
	/**
	 * @param alarmIdPropertyName Property name of the alarm Id property (e.g. "id")
	 */
	public AlarmIdColumnLinkGenerator(final AlarmDao alarmDao, final String alarmIdPropertyName) {
		this.alarmIdPropertyName = alarmIdPropertyName;
		this.alarmDao = alarmDao;
	}

	@Override
	public Object generateCell(final Table source, Object itemId, Object columnId) {
		if (source == null) return null; // no source
		Property<Integer> alarmIdProperty = source.getContainerProperty(itemId,  alarmIdPropertyName);
		final Integer alarmId = alarmIdProperty.getValue(); 
		if (alarmId == null) return null; // no value

		// create Link
		Button button = new Button("" + alarmId);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		button.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3698209256202413810L;

            @Override
			public void buttonClick(ClickEvent event) {
			    // try if alarm is there, otherwise show information dialog
			    OnmsAlarm alarm = alarmDao.get(alarmId);
			    if (alarm == null) {
		           new DialogWindow(source.getUI(), 
		                         "Alarm does not exist!", 
		                         "The alarm information cannot be shown. \nThe alarm does not exist anymore. \n\nPlease refresh the Alarm Table.");
			        return;
			    }
			    
			    // alarm still exists, show alarm details
		                final URI currentLocation = Page.getCurrent().getLocation();
		                final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
		                final String redirectFragment = contextRoot + "/alarm/detail.htm?quiet=true&id=" + alarmId;
		                LOG.debug("alarm {} clicked, current location = {}, uri = {}", alarmId, currentLocation, redirectFragment);

		                try {
					source.getUI().addWindow(
						new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new LabelCreator() {
								
							@Override
							public String getLabel() {
								return "Alarm Info " + alarmId;
							}
						}));
				} catch (MalformedURLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		});
		return button;
	}
}
