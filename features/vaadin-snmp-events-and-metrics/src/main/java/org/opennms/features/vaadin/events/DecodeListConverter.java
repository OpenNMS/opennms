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

package org.opennms.features.vaadin.events;

import com.vaadin.data.util.converter.Converter;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.xml.eventconf.Decode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Varbind's Decode List Converter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class DecodeListConverter implements Converter<String, ArrayList<Decode>> {

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public ArrayList<Decode> convertToModel(String fieldValue, Class<? extends ArrayList<Decode>> targetType, Locale locale) throws ConversionException {
        if (fieldValue == null) {
            return null;
        }
        ArrayList<Decode> list = new ArrayList<>();
        for (String s : fieldValue.split(",")) {
            String[] parts = s.split("=");
            if (parts.length == 2) {
                Decode d = new Decode();
                d.setVarbindvalue(parts[0].trim());
                d.setVarbinddecodedstring(parts[1].trim());
                list.add(d);
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public String convertToPresentation(ArrayList<Decode> propertyValue, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (propertyValue == null) {
            return null;
        }
        final List<String> values = new ArrayList<>();
        for (Decode d : propertyValue) {
            values.add(d.getVarbindvalue() + '=' + d.getVarbinddecodedstring());
        }
        return StringUtils.join(values, ',');
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<Decode>> getModelType() {
        return (Class<ArrayList<Decode>>) new ArrayList<Decode>().getClass();

    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getPresentationType()
     */
    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }

}
