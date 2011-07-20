package org.opennms.features.gwt.graph.resource.list.client.view.styles;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.Style;

public interface CustomCellTreeResource extends CellTree.Resources {

    @Source({"ReportCellTree.css"})
    Style cellTreeStyle();

}
