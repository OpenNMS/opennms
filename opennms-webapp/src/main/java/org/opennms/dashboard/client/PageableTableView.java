/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 2, 2007
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * <p>Abstract PageableTableView class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class PageableTableView extends DashletView implements Pageable {

    private VerticalPanel m_panel = new VerticalPanel();
	private FlexTable m_table = new FlexTable();
	private Pager m_pager;
	private String[] m_headings;
	private int m_pageSize = 5;
	private int m_currentIndex = 0;
	
	PageableTableView(Dashlet dashlet, int pageSize, String[] headings) {
        super(dashlet);
        m_pageSize = pageSize;
        
        initializeTable(headings);
        
        m_pager = new Pager(this);
        
        //m_panel.add(m_pager);
        m_panel.add(m_table);
        initWidget(m_panel);
        
    }
    
    /**
     * <p>onDashLoad</p>
     */
    public void onDashLoad() {
        addToTitleBar(m_pager, DockPanel.CENTER);
    }



	/**
	 * Override this to set the details of the individual rows
	 *
	 * @param table the table to set the data into
	 * @param row that table row to set the element into
	 * @param elementIndex the index of the element whose values should be set
	 */
	protected abstract void setRow(FlexTable table, int row, int elementIndex);


	/**
	 * The total number of elements being deplayed in this table
	 *
	 * @return a int.
	 */
	public abstract int getElementCount();

    /**
     * <p>initializeTable</p>
     *
     * @param headings an array of {@link java.lang.String} objects.
     */
    protected void initializeTable(String[] headings) {
	    
	    setHeadings(headings);
	    
	    for(int i = 1; i <= getPageSize(); i++) {  
	        clearRow(i);
	    }
	}

	private void setHeadings(String[] headings) {
		m_headings = headings;
	    for(int i = 0; i < headings.length; i++) {
	        m_table.setText(0, i, headings[i]);
	    }
	
	    m_table.getRowFormatter().setStyleName(0, "header");
	}

	private int getColumnCount() {
		return m_headings == null ? 0 : m_headings.length;
	}

	private void clearRow(int row) {
	    if (row >= m_table.getRowCount()) {
	        return;
	    }
	    
	    for(int column = 0; column < getColumnCount(); column++) {
	    	m_table.clearCell(row, column);
	    }
	
	    String currStyle = m_table.getRowFormatter().getStyleName(row);
	    if (currStyle != null) {
	        m_table.getRowFormatter().removeStyleName(row, currStyle);
	    }
	    formatCells(m_table, row);
	
	}

	/**
	 * <p>refresh</p>
	 */
	protected void refresh() {
	
	    int rows = Math.min(m_currentIndex+getPageSize(), getElementCount());
	    
	    for(int i = m_currentIndex+1; i <= rows; i++) {
	        setRow(m_table, i - m_currentIndex, i-1);
	        formatCells(m_table, i - m_currentIndex);
	    }
	    
	    for(int i = rows+1; i <= m_currentIndex+getPageSize(); i++) {
	        clearRow(i - m_currentIndex);
	    }
	
	    m_pager.update();
	}

	/**
	 * <p>formatCells</p>
	 *
	 * @param table a {@link com.google.gwt.user.client.ui.FlexTable} object.
	 * @param row a int.
	 */
	protected void formatCells(FlexTable table, int row) {
		for(int column = 0; column < getColumnCount(); column++) {
		    m_table.getCellFormatter().setStyleName(row, column, "divider");
		}
	}

	/**
	 * <p>getCurrentElement</p>
	 *
	 * @return a int.
	 */
	public int getCurrentElement() {
	    return m_currentIndex;
	}

	/**
	 * <p>getPageSize</p>
	 *
	 * @return a int.
	 */
	public int getPageSize() {
	    return m_pageSize;
	}

	/**
	 * <p>setPageSize</p>
	 *
	 * @param pageSize a int.
	 */
	public void setPageSize(int pageSize) {
		m_pageSize = pageSize;
	}

	/** {@inheritDoc} */
	public void setCurrentElement(int element) {
	    m_currentIndex = element;
	    refresh();
	}

}
