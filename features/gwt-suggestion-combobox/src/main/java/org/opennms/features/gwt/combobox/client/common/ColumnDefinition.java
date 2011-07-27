package org.opennms.features.gwt.combobox.client.common;

import com.google.gwt.user.client.ui.Widget;

public abstract class ColumnDefinition<T> {
    public abstract Widget render(T t);
    
    public boolean isClickable() {
        return true;
    }
    
    public boolean isSelectable() {
        return true;
    }
}
