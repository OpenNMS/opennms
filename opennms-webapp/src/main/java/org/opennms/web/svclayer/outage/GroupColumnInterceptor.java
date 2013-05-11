/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.outage;


import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.interceptor.ColumnInterceptor;

/**
 * <p>GroupColumnInterceptor class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class GroupColumnInterceptor implements ColumnInterceptor {
    private static final String LAST_VALUE = "lastValue";

    /** {@inheritDoc} */
    @Override
    public void addColumnAttributes(TableModel tableModel, Column column) {
        //do nothing
    }

    /** {@inheritDoc} */
    @Override
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
