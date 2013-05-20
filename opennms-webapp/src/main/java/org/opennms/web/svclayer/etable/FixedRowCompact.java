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

package org.opennms.web.svclayer.etable;
//
//
//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//  http://www.opennms.org/
//  http://www.opennms.com/
//

import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.AbstractHtmlView;
import org.extremecomponents.util.HtmlBuilder;

/**
 * <p>FixedRowCompact class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class FixedRowCompact extends AbstractHtmlView {
	/** {@inheritDoc} */
        @Override
	protected void beforeBodyInternal(TableModel model) {
		getTableBuilder().tableStart();

		getTableBuilder().theadStart();

		getTableBuilder().titleRowSpanColumns();

		toolbar(getHtmlBuilder(), getTableModel());

		getTableBuilder().filterRow();

		getTableBuilder().headerRow();

		getTableBuilder().theadEnd();

		getTableBuilder().tbodyStart();
	}

	/** {@inheritDoc} */
        @Override
	protected void afterBodyInternal(TableModel model) {
		getCalcBuilder().defaultCalcLayout();

		getTableBuilder().tbodyEnd();

		getTableBuilder().tableEnd();
	}

	/**
	 * <p>toolbar</p>
	 *
	 * @param html a {@link org.extremecomponents.util.HtmlBuilder} object.
	 * @param model a {@link org.extremecomponents.table.core.TableModel} object.
	 */
	protected void toolbar(HtmlBuilder html, TableModel model) {
		new CompactFixedRowToolbar(html, model).layout();
	}
}
