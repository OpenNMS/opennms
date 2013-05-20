package org.opennms.features.topology.app.internal.operations;

import com.vaadin.ui.Window;

public class GroupWindow extends Window {
    private static final long serialVersionUID = -2746052327624949890L;

    public GroupWindow(String title, String width, String height) {
        super(title);
        setModal(true);
        setResizable(false);
        setWidth(width);
        setHeight(height);
    }
}