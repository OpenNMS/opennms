package org.opennms.web.svclayer.etable;

import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.AbstractHtmlView;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.util.HtmlBuilder;

public class FixedRowCompact extends AbstractHtmlView {
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

	protected void afterBodyInternal(TableModel model) {
		getCalcBuilder().defaultCalcLayout();

		getTableBuilder().tbodyEnd();

		getTableBuilder().tableEnd();
	}

	protected void toolbar(HtmlBuilder html, TableModel model) {
		new CompactFixedRowToolbar(html, model).layout();
	}
}
