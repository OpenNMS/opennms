package org.opennms.gwt.web.ui.enterprisereporting.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;

public class ReportListGrid extends Grid {

    private Element m_header;
    private Element m_headerRow;
    
    public ReportListGrid() {
        super();
    }
    
    public ReportListGrid(int rows, int columns) {
        super(rows, columns);
    }
    
    public void addHeader(int column, String text) {
        if(m_header == null) {
            createHeader();
        }
        Element th = DOM.createTH();
        th.setInnerHTML(text);
        DOM.insertChild(m_headerRow, th, column);
    }

    private void createHeader() {
        m_header = DOM.createTHead();
        m_headerRow = DOM.createTR();
        DOM.appendChild(m_header, m_headerRow);
        DOM.insertChild(getElement(), m_header, 0);
    }
}
