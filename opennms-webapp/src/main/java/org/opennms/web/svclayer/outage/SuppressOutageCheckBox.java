package org.opennms.web.svclayer.outage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.cell.Cell;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.table.view.html.ColumnBuilder;
import org.opennms.web.outage.Outage;

import org.apache.commons.lang.StringUtils;

public class SuppressOutageCheckBox implements Cell {
	public String getExportDisplay(TableModel model, Column column) {
		return null;
	}

	public String getHtmlDisplay(TableModel model, Column column) {
		ColumnBuilder columnBuilder = new ColumnBuilder(column);

		columnBuilder.tdStart();

		try {
			Object bean = model.getCurrentRowBean();
			String outageid = BeanUtils.getProperty(bean, "outageid");

			Collection selectedoutagesIds = (Collection) model.getContext()
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
		} catch (Exception e) {
		}

		columnBuilder.tdEnd();
		return columnBuilder.toString();
	}
}
