/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.fieldsets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a> 
 * </br>
 * For later use. Don't use it jet!
 */
public class FlexTableFieldSet extends FlexTable {
	int activRow = -1;
	int activCell = -1;
	int maxCells = 0;

	public FlexTableFieldSet() {
		super();
		this.setStylePrimaryName("FlexTableFieldSet");
	}

	public void addNewRowWidget(Widget wg) {
		activRow++;
		activCell = 0;
		wg.setStyleDependentName("NewRowWidget", true);
		setWidget(activRow, activCell, wg);
	}

	public void addNewRowWidget(Widget wg, int colSpan) {
		addNewRowWidget(wg);
		getFlexCellFormatter().setColSpan(activRow, activCell, colSpan);
	}

	public void addNewWidget(Widget wg) {
		activCell++;
		wg.setStyleDependentName("NewWidget", true);
		setWidget(activRow, activCell, wg);
		if (activCell > maxCells) {
			maxCells = activCell;
		}
	}

	public void addNewWidget(Widget wg, int colSpan) {
		addNewWidget(wg);
		getFlexCellFormatter().setColSpan(activRow, activCell, colSpan);
	}

	public void addSectionHeader(Widget wg) {
		activRow++;
		wg.setStyleDependentName("SectionHeader", true);
		setWidget(activRow, 0, new HTML("<h3>" + wg + "</h3>"));
		getFlexCellFormatter().setColSpan(activRow, 0, 800);
		activRow++;
		activCell = -1;
	}

	public int getActivCell() {
		return activCell;
	}

	public int getActivRow() {
		return activRow;
	}

	public void setActivCell(int activCell) {
		this.activCell = activCell;
	}

	public void setActivRow(int activRow) {
		this.activRow = activRow;
	}
}
