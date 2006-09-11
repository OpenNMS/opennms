package org.opennms.web.svclayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The idea of this class is to represent a simple table that has column headers
 * and rows.  
 * @author brozow
 *
 */
public class SimpleWebTable {
	
	public class Cell {
		private Object m_content;
		private String m_styleClass;
		public Cell() {}
		public Cell(Object content, String styleClass) {
			m_content = content;
			m_styleClass = styleClass;
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
	}
	
	
	private List<Cell> m_columnHeaders = new ArrayList<Cell>();
	private List<List<Cell>> m_rows = new ArrayList<List<Cell>>();
	private List<Cell> m_currentRow = null;
	private String m_title = "";
	
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
	
	public void addColumn(Object headerContent, String headerStyle) {
		Cell headerCell = new Cell(headerContent, headerStyle);
		m_columnHeaders.add(headerCell);
	}
	
	public void newRow() {
		List<Cell> row = new ArrayList<Cell>();
		m_rows.add(row);
		m_currentRow = row;
	}
	
	public void addCell(Object cellContent, String cellStyle) {
		if (m_currentRow == null)
			throw new IllegalStateException("make sure you call newRow before trying to add any cells to the table!");
		
		m_currentRow.add(new Cell(cellContent, cellStyle));
		
		
	}

}
