/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
