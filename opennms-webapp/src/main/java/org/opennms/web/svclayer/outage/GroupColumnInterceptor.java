/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer.outage;


import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.interceptor.ColumnInterceptor;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class GroupColumnInterceptor implements ColumnInterceptor {
    private static final String LAST_VALUE = "lastValue";

    public void addColumnAttributes(TableModel tableModel, Column column) {
        //do nothing
    }

    public void modifyColumnAttributes(TableModel tableModel, Column column) {
        Object value = column.getValue();
        Object lastValue = column.getAttribute(LAST_VALUE);

        column.addAttribute(LAST_VALUE, column.getValue());

        if ((value == null && lastValue == null) || (value != null && value.equals(lastValue))) {
            column.setValue("&nbsp;");
            column.setStyle("border-top-style: none; border-bottom-style: none;");
        } else {
            column.setStyle("border-bottom-style: none;");
        }
    }
} 