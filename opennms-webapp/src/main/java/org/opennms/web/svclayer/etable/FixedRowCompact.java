/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.etable;

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
