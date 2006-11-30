package org.opennms.web.svclayer;

import java.util.ArrayList;
import java.util.List;

import org.opennms.web.Util;
import org.springframework.validation.Errors;

/**
 * The idea of this class is to represent a simple table that has column headers
 * and rows.  
 * @author brozow
 *
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
