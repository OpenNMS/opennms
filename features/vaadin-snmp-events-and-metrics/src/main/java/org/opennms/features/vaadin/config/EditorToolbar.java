/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.config;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;

/**
 * The Class EditorToolbar.
 */
@SuppressWarnings("serial")
public abstract class EditorToolbar extends HorizontalLayout implements ClickListener {

    /** The Edit button. */
    private final Button editBtn = new Button("Edit", this);

    /** The Delete button. */
    private final Button deleteBtn = new Button("Delete", this);

    /** The Save button. */
    private final Button saveBtn = new Button("Save", this);

    /** The Cancel button. */
    private final Button cancelBtn = new Button("Cancel", this);

    /**
     * Instantiates a new event form.
     */
    public EditorToolbar() {
        setSpacing(true);
        addComponent(editBtn);
        addComponent(deleteBtn);
        addComponent(saveBtn);
        addComponent(cancelBtn);
        setReadOnly(true);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Form#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        saveBtn.setVisible(!readOnly);
        cancelBtn.setVisible(!readOnly);
        editBtn.setVisible(readOnly);
        deleteBtn.setVisible(readOnly);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == saveBtn && save()) {
            setReadOnly(true);
        }
        if (source == cancelBtn && cancel()) {
            setReadOnly(true);
        }
        if (source == editBtn && edit()) {
            setReadOnly(false);
        }
        if (source == deleteBtn) {
            ConfirmDialog.show(getUI(),
                               "Are you sure?",
                               "Do you really want to remove the selected definition ?\nThis action cannot be undone.",
                               "Yes",
                               "No",
                               new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed() && delete()) {
                        setVisible(false);
                    }
                }
            });
        }
    }

    /**
     * Edit.
     *
     * @return true, if successful
     */
    public abstract boolean edit();

    /**
     * Cancel.
     * 
     * @return true, if successful
     */
    public abstract boolean cancel();

    /**
     * Save.
     * 
     * @return true, if successful
     */
    public abstract boolean save();

    /**
     * Delete.
     * 
     * @return true, if successful
     */
    public abstract boolean delete();
}
