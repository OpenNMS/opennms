/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Locale;

/**
 * The CSV List Converter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CsvListConverter implements Converter<String, ArrayList<String>> {

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public ArrayList<String> convertToModel(String fieldValue, Class<? extends ArrayList<String>> targetType, Locale locale) throws ConversionException {
        if (fieldValue == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        if (fieldValue != null) {
            for (String s : fieldValue.split(",")) {
                if (s == null || "".equals(s.trim())) {
                    // Blank value, skip it
                } else {
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    @Override
    public String convertToPresentation(ArrayList<String> propertyValue, Class<? extends String> targetType, Locale locale) throws ConversionException {
        return propertyValue == null ? null : StringUtils.join(propertyValue, ',');
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<ArrayList<String>> getModelType() {
        return (Class<ArrayList<String>>) new ArrayList<String>().getClass();
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.converter.Converter#getPresentationType()
     */
    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
