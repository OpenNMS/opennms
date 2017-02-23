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

import java.util.Iterator;

import org.extremecomponents.table.bean.Export;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.html.BuilderConstants;
import org.extremecomponents.table.view.html.BuilderUtils;
import org.extremecomponents.table.view.html.StatusBarBuilder;
import org.extremecomponents.table.view.html.ToolbarBuilder;
import org.extremecomponents.util.HtmlBuilder;

/**
 * <p>CompactFixedRowToolbar class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author Jeff Johnston
 * @version $Id: $
 * @since 1.8.1
 */
public class CompactFixedRowToolbar extends CustomizableTwoColumnRowLayout {
	/**
	 * <p>Constructor for CompactFixedRowToolbar.</p>
	 *
	 * @param html a {@link org.extremecomponents.util.HtmlBuilder} object.
	 * @param model a {@link org.extremecomponents.table.core.TableModel} object.
	 */
	public CompactFixedRowToolbar(HtmlBuilder html, TableModel model) {
		super(html, model);
	}

    /** {@inheritDoc} */
    @Override
	protected boolean showLayout(TableModel model) {
		boolean showStatusBar = BuilderUtils.showStatusBar(model);
		boolean filterable = BuilderUtils.filterable(model);
		boolean showExports = BuilderUtils.showExports(model);
		boolean showPagination = BuilderUtils.showPagination(model);
		boolean showTitle = BuilderUtils.showTitle(model);
		if (!showStatusBar && !filterable && !showExports && !showPagination
				&& !showTitle) {
			return false;
		}

		return true;
	}

    /** {@inheritDoc} */
    @Override
	protected void columnLeft(HtmlBuilder html, TableModel model) {
		boolean showStatusBar = BuilderUtils.showStatusBar(model);
		if (!showStatusBar) {
			return;
		}

		html.td(4).styleClass(BuilderConstants.STATUS_BAR_CSS).close();

		new StatusBarBuilder(html, model).statusMessage();

		html.tdEnd();
	}

    /** {@inheritDoc} */
    @Override
	protected void columnRight(HtmlBuilder html, TableModel model) {
		boolean filterable = BuilderUtils.filterable(model);
		boolean showPagination = BuilderUtils.showPagination(model);
		boolean showExports = BuilderUtils.showExports(model);

		if (showPagination || showExports) {

			ToolbarBuilder toolbarBuilder = new ToolbarBuilder(html, model);

			html.td(4).styleClass(BuilderConstants.COMPACT_TOOLBAR_CSS).align("right").close();

			html.table(4).styleClass("normal").style("margin-bottom: 0px;").close();
			html.tr(5).close();

			if (showPagination) {
				html.td(5).close();
				toolbarBuilder.firstPageItemAsImage();
				html.tdEnd();

				html.td(5).close();
				toolbarBuilder.prevPageItemAsImage();
				html.tdEnd();

				html.td(5).close();
				toolbarBuilder.nextPageItemAsImage();
				html.tdEnd();

				html.td(5).close();
				toolbarBuilder.lastPageItemAsImage();
				html.tdEnd();

				// html.td(5).close();
				// toolbarBuilder.separator();
				// html.tdEnd();
				// Disabled the row dropdown for a fixedrow table....
				// html.td(5).close();
				// toolbarBuilder.rowsDisplayedDroplist();
				// html.tdEnd();

				// if (showExports) {
				// html.td(5).close();
				// toolbarBuilder.separator();
				// html.tdEnd();
				// }
			}

			if (showExports) {
				@SuppressWarnings("unchecked")
				Iterator<Export> iterator = model.getExportHandler().getExports()
						.iterator();
				for (Iterator<Export> iter = iterator; iter.hasNext();) {
					html.td(5).close();
					Export export = iter.next();
					toolbarBuilder.exportItemAsImage(export);
					html.tdEnd();
				}
			}

			if (filterable) {
				if (showExports || showPagination) {
					html.td(5).close();
					toolbarBuilder.separator();
					html.tdEnd();
				}

				html.td(5).close();
				toolbarBuilder.filterItemAsImage();
				html.tdEnd();

				html.td(5).close();
				toolbarBuilder.clearItemAsImage();
				html.tdEnd();
			}

			html.trEnd(5);

			html.tableEnd(4);

			html.tdEnd();
		}
	}

    /** {@inheritDoc} */
    @Override
    protected HtmlBuilder startTable(HtmlBuilder html) {
        return html.table(2).styleClass("normal").style("width: 100%;   margin-bottom: 0px;").close();
    }
}
