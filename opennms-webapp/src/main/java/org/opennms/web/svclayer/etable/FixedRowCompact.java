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
