/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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
package org.opennms.features.topology.plugins.browsers;

import java.net.MalformedURLException;
import java.net.URL;

import org.opennms.features.topology.api.support.DialogWindow;
import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.features.topology.api.support.InfoWindow.LabelCreator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;

public class AlarmIdColumnLinkGenerator implements ColumnGenerator {

	private static final long serialVersionUID = 621311104480258016L;
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
				try {
					source.getUI().addWindow(
						new InfoWindow(new URL(Page.getCurrent().getLocation().toURL(), "../../alarm/detail.htm?id=" + alarmId), new LabelCreator() {
								
							@Override
							public String getLabel() {
								return "Alarm Info " + alarmId;
							}
						}));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		return button;
	}
}
