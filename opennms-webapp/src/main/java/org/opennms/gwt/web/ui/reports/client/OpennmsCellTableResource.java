package org.opennms.gwt.web.ui.reports.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;

public interface OpennmsCellTableResource extends Resources {
    
    public OpennmsCellTableResource INSTANCE = GWT.create(OpennmsCellTableResource.class);
    
    public interface CellTableStyle extends Style{};
    
    @Source({"OpennmsCellTable.css"})
    public CellTableStyle cellTableStyle();
}
