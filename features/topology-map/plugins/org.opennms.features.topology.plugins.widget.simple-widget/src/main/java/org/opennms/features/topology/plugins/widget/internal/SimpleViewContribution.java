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

package org.opennms.features.topology.plugins.widget.internal;

import java.util.Arrays;
import java.util.List;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;

public class SimpleViewContribution implements IViewContribution {
    
    private static final List<String> cities = Arrays.asList(new String[] {
            "Berlin", "Brussels", "Helsinki", "Madrid", "Oslo", "Paris",
            "Stockholm" });
    
    @SuppressWarnings("serial")
    @Override
    public Component getView(final WidgetContext widgetContext) {
        
        ListSelect listSelect = new ListSelect("Select a city", cities);
        listSelect.setHeight("100%");
        listSelect.setWidth("100%");
        listSelect.setImmediate(true);
        listSelect.addValueChangeListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event) {
                Notification.show("" + event.getProperty());
            }
        });
        
        return listSelect;
    }

    @Override
    public String getTitle() {
        return "Simple View";
    }

    @Override
    public Resource getIcon() {
        return null;
    }

}
