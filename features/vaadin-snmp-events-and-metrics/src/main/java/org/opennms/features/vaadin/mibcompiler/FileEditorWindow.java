/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.io.File;

import org.opennms.features.vaadin.api.Logger;

import com.vaadin.data.util.TextFileProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.Runo;

/**
 * The File Editor Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class FileEditorWindow extends Window implements Button.ClickListener {

    /** The MIB editor area. */
    private final TextArea editor;

    /** The cancel button. */
    private final Button cancel;

    /** The save button. */
    private final Button save;

    /** The logger. */
    protected final Logger logger;

    /** The file. */
    protected final File file;

    /**
     * Instantiates a new file editor window.
     *
     * @param file the file
     * @param logger the logger
     * @param readOnly true, if you want to display a read only window.
     */
    public FileEditorWindow(final File file, final Logger logger, boolean readOnly) {
        this.file = file;
        this.logger = logger;

        setCaption((readOnly ? "View" : "Edit") + " MIB");
        addStyleName(Runo.WINDOW_DIALOG);
        setModal(true);
        setClosable(false);
        setWidth("800px");
        setHeight("540px");

        editor = new TextArea();
        editor.setPropertyDataSource(new TextFileProperty(file));
        editor.setBuffered(true);
        editor.setImmediate(false);
        editor.setSizeFull();
        editor.setRows(30);
        editor.setReadOnly(readOnly);

        cancel = new Button(readOnly ? "Close" : "Cancel");
        cancel.setImmediate(false);
        cancel.addClickListener(this);
        save = new Button("Save");
        save.setImmediate(false);
        save.addClickListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(cancel);
        if (!readOnly)
            toolbar.addComponent(save);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(editor);
        layout.addComponent(toolbar);
        layout.setExpandRatio(editor, 1.0f);
        layout.setComponentAlignment(toolbar, Alignment.BOTTOM_RIGHT);
        setContent(layout);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(ClickEvent event) {
        if (event.getButton().equals(save)) {
            if (editor.isReadOnly()) {
                Notification.show("Unsupported action for readOnly viewer.", Notification.Type.WARNING_MESSAGE);
            } else {
                editor.commit();
                logger.info("The file " + file + " has been changed.");
            }
        }
        if (event.getButton().equals(cancel)) {
            if (!editor.isReadOnly())
                logger.info("The file editing has been canceled.");
            editor.discard();
        }
        close();
    }

}
