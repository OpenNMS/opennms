package org.opennms.web.svclayer.outage;

import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.DisplayCell;
import org.extremecomponents.table.core.TableModel;

public class GroupCell extends DisplayCell {
	private static final String LAST_VALUE = "lastValue";

	protected String getCellValue(TableModel model, Column column) {
		String value = column.getValueAsString();
		String lastValue = column.getAttributeAsString(LAST_VALUE);

		if (value.equals(lastValue)) {
			value = "&nbsp;";
		}

		column.addAttribute(LAST_VALUE, column.getValueAsString());
		return value;
	}
}
