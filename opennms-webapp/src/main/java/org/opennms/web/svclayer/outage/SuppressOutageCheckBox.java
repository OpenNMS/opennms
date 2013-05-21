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

/**
 * <p>SuppressOutageCheckBox class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.web.svclayer.outage;

import java.util.Collection;

import org.apache.commons.beanutils.BeanUtils;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.Cell;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.html.ColumnBuilder;
public class SuppressOutageCheckBox implements Cell {
	/** {@inheritDoc} */
        @Override
	public String getExportDisplay(TableModel model, Column column) {
		return null;
	}

	/** {@inheritDoc} */
        @Override
	public String getHtmlDisplay(TableModel model, Column column) {
		ColumnBuilder columnBuilder = new ColumnBuilder(column);

		columnBuilder.tdStart();

		try {
			Object bean = model.getCurrentRowBean();
			String outageid = BeanUtils.getProperty(bean, "outageid");

			@SuppressWarnings("unchecked")
			Collection<String> selectedoutagesIds = (Collection<String>) model.getContext()
					.getSessionAttribute(
							SuppressOutageCheckBoxConstants.SELECTED_OUTAGES);
			if (selectedoutagesIds != null
					&& selectedoutagesIds.contains(outageid)) {
				columnBuilder.getHtmlBuilder().input("hidden").name(
						"chkbx_" + outageid).value(
						SuppressOutageCheckBoxConstants.SELECTED).xclose();
				columnBuilder.getHtmlBuilder().input("checkbox").name(
						BeanUtils.getProperty(bean, "outageid"));
				columnBuilder.getHtmlBuilder().onclick("setOutageState(this)");
				columnBuilder.getHtmlBuilder().checked();
				columnBuilder.getHtmlBuilder().xclose();
			} else {
				columnBuilder.getHtmlBuilder().input("hidden").name(
						"chkbx_" + outageid).value(
						SuppressOutageCheckBoxConstants.UNSELECTED).xclose();
				columnBuilder.getHtmlBuilder().input("checkbox").name(
						BeanUtils.getProperty(bean, "outageid"));
				columnBuilder.getHtmlBuilder().onclick("setOutageState(this)");
				columnBuilder.getHtmlBuilder().xclose();
			}
		} catch (Throwable e) {
		}

		columnBuilder.tdEnd();
		return columnBuilder.toString();
	}
}
