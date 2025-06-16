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
package org.opennms.features.vaadin.config;

import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.HorizontalLayout;

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
