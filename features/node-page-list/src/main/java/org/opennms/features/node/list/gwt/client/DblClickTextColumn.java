package org.opennms.features.node.list.gwt.client;

import com.google.gwt.user.cellview.client.Column;

public abstract class DblClickTextColumn<T> extends Column<T, String> {

    public DblClickTextColumn() {
        super(new DblClickTextCell());
    }


}
