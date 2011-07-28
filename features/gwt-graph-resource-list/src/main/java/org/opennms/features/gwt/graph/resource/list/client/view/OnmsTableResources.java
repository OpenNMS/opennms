package org.opennms.features.gwt.graph.resource.list.client.view;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Style;

public interface OnmsTableResources extends CellTable.Resources {
    @Source({"customCellTableStyles.css"})
    Style cellTableStyle();
}
