/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 11, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer;

import java.util.ArrayList;
import java.util.List;

import org.opennms.web.Util;
import org.springframework.validation.Errors;

/**
 * 
 * The idea of this class is to represent a simple table that has column headers
 * and rows.  
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class SimpleWebTable {

	public static class Cell {
		private Object m_content;
		private String m_styleClass;
		private String m_link;

		public Cell() {}

		public Cell(Object content, String styleClass) {
			m_content = content;
			m_styleClass = styleClass;
		}

		public Cell(Object content, String styleClass, String link) {
			m_content = content;
			m_styleClass = styleClass;
			m_link = link;
		}

		public Object getContent() {
			return m_content;
		}

		public void setContent(Object content) {
			m_content = content;
		}

		public String getStyleClass() {
			return m_styleClass;
		}

		public void setStyleClass(String styleClass) {
			m_styleClass = styleClass;
		}

		public String getLink() {
			return m_link;
		}

		public void setLink(String link) {
			m_link = link;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Cell)) {
				return false;
			}

			Cell c = (Cell) o;

			if ((m_content != null || c.m_content != null)
					&& (m_content == null
							|| !m_content.equals(c.m_content))) { 
				return false;
			}

			if ((m_styleClass != null || c.m_styleClass != null)
					&& (m_styleClass == null
							|| !m_styleClass.equals(c.m_styleClass))) { 
				return false;
			}

			if ((m_link != null || c.m_link != null)
					&& (m_link == null
							|| !m_link.equals(c.m_link))) { 
				return false;
			}

			return true;
		}

		@Override
		public String toString() {
			return "Content: \"" + m_content + "\", styleClass: \""
			+ m_styleClass + "\", link: \"" + m_link + "\"";
		}
	}


	private List<Cell> m_columnHeaders = new ArrayList<Cell>();
	private List<List<Cell>> m_rows = new ArrayList<List<Cell>>();
	private List<Cell> m_currentRow = null;
	private String m_title = "";
	private Errors m_errors = null;

	public List<Cell> getColumnHeaders() {
		return m_columnHeaders;
	}

	public List<List<Cell>> getRows() {
		return m_rows;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title  = title;
	}

	public Cell addColumn(Object headerContent, String headerStyle) {
		Cell headerCell = new Cell(headerContent, headerStyle);
		m_columnHeaders.add(headerCell);
		return headerCell;
	}
	

	public Cell addColumn(Object headerContent) {
		return addColumn(headerContent, "");
	}

	public void newRow() {
		List<Cell> row = new ArrayList<Cell>();
		m_rows.add(row);
		m_currentRow = row;
	}

	public Cell addCell(Object cellContent, String cellStyle, String link) {
		if (m_currentRow == null) {
			throw new IllegalStateException("make sure you call newRow before trying to add any cells to the table!");
		}

		Cell cell = new Cell(cellContent, cellStyle, link);
		m_currentRow.add(cell);
		return cell;
	}

	public Cell addCell(Object cellContent, String cellStyle) {
		return addCell(cellContent, cellStyle, null);
	}


	public Cell addCell(String cellContent) {
		return addCell(cellContent, "", null);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("<h2>" + Util.htmlify(getTitle()) + "</h2>\n");

		buf.append("<table>\n");

		buf.append("  <tr>\n");
		for (Cell cell : getColumnHeaders()) {
			buf.append("    <th style=\"" + cell.getStyleClass()
					+ "\">\n");
			if (cell.getLink() != null) {
				buf.append("      <a href=\""
						+ Util.htmlify(cell.getLink()) + "\">"
						+ cell.getContent() + "</a>\n");
			} else {
				buf.append("      " + cell.getContent() + "\n");
			}
			buf.append("    </th>\n");
		}
		buf.append("  </tr>\n");

		for (List<Cell> cells : getRows()) {
			buf.append("  <tr>\n");
			for (Cell cell : cells) {
				buf.append("    <td style=\"" + cell.getStyleClass()
						+ "\">\n");
				if (cell.getLink() != null) {
					buf.append("      <a href=\""
							+ Util.htmlify(cell.getLink()) + "\">"
							+ cell.getContent() + "</a>\n");
				} else {
					buf.append("      " + cell.getContent() + "\n");
				}
				buf.append("    </td>\n");
			}
			buf.append("  </tr>\n");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	public Errors getErrors() {
		return m_errors;
	}

	public void setErrors(Errors errors) {
		m_errors = errors;
	}

}
