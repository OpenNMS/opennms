/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.DataCollectionConfigDao;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;

/**
 * A factory for creating SNMP Collection Field objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class SnmpCollectionFieldFactory implements FormFieldFactory {

    /** The data collection config dao. */
    final DataCollectionConfigDao dataCollectionConfigDao;

    /**
     * Instantiates a new SNMP collection field factory.
     *
     * @param dataCollectionConfigDao the data collection configuration DAO
     */
    public SnmpCollectionFieldFactory(final DataCollectionConfigDao dataCollectionConfigDao) {
        this.dataCollectionConfigDao = dataCollectionConfigDao;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
    public Field<?> createField(Item item, Object propertyId, Component uiContext) {
        if ("name".equals(propertyId)) {
            final TextField f = new TextField("SNMP Collection Name");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("snmpStorageFlag".equals(propertyId)) {
            final ComboBox f = new ComboBox("SNMP Storage Flag");
            f.setRequired(true);
            f.addItem("select");
            f.addItem("all");
            return f;
        }
        if ("rrd".equals(propertyId)) {
            final RrdField f = new RrdField("RRD");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("includeCollectionCollection".equals(propertyId)) {
            IncludeCollectionField f = new IncludeCollectionField(dataCollectionConfigDao);
            f.setCaption("Include Collections");
            return f;
        }
        return null;
    }
}
