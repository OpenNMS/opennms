/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

public abstract class DirectionalChangeListener implements ChangeListener {
    public void onChange(Widget widget) {
        onChange(widget, 1);
    }
    
    public abstract void onChange(Widget widget, int direction);
}