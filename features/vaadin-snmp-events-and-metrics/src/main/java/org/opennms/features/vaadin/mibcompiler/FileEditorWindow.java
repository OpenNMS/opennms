/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;

import org.opennms.features.vaadin.api.Logger;

import com.vaadin.v7.data.util.TextFileProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

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
        addStyleName("dialog");
        setModal(true);
        setClosable(false);
        setWidth("800px");
        setHeight("540px");

        editor = new TextArea();
        editor.setPropertyDataSource(new TextFileProperty(file));
        editor.setImmediate(false);
        editor.setSizeFull();
        editor.setRows(30);
        editor.setReadOnly(readOnly);

        cancel = new Button(readOnly ? "Close" : "Cancel");
        cancel.addClickListener(this);
        save = new Button("Save");
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
