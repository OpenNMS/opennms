/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class MIB Edit Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibEditWindow extends Window implements Button.ClickListener {

    /** The editor. */
    private final TextArea editor;

    /** The cancel. */
    private final Button cancel;

    /** The save. */
    private final Button save;

    /**
     * Instantiates a new MIB edit window.
     *
     * @param document the document
     */
    public MibEditWindow(Property document) {
        setCaption("Edit MIB");
        setModal(true);
        setWidth(800, Sizeable.UNITS_PIXELS);
        setHeight(540, Sizeable.UNITS_PIXELS);

        editor = new TextArea();
        editor.setPropertyDataSource(document);
        editor.setWriteThrough(false);
        editor.setSizeFull();
        editor.setRows(30);

        cancel = new Button("Cancel");
        cancel.addListener(this);
        save = new Button("Save");
        save.addListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(cancel);
        toolbar.addComponent(save);

        addComponent(editor);
        addComponent(toolbar);

        ((VerticalLayout)getContent()).setExpandRatio(editor, 1.0f);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    public void buttonClick(ClickEvent event) {
        if (event.getButton().equals(save)) {
            editor.commit();
            getApplication().getMainWindow().removeWindow(this);
        }
        if (event.getButton().equals(cancel)) {
            editor.discard();
            getApplication().getMainWindow().removeWindow(this);
        }
    }

}
