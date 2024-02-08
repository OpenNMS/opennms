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
package org.opennms.netmgt.vaadin.core;

import com.vaadin.v7.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Vaadin dialog window to query for a single String value.
 *
 * @author Christian Pape <christian@opennms.org>
 */
public class StringInputDialogWindow extends Window implements Window.CloseListener, Button.ClickListener {

    /**
     * The callback interface
     */
    public interface Action {
        void execute(StringInputDialogWindow window);
    }

    /**
     * the actions
     */
    private Action m_okAction;
    private Action m_cancelAction;

    /**
     * the buttons
     */
    private final Button m_cancelButton;
    private final Button m_okButton;

    /**
     * the input field
     */
    private final TextField m_inputField;

    /**
     * flag whether ok was pressed
     */
    private boolean m_okPressed;

    /**
     * Default constructor
     */
    public StringInputDialogWindow() {
        this("Input required", "Input");
    }

    /**
     * Constructor responsible for creating new instances of this class
     *
     * @param caption   the window's title
     * @param fieldName the title of the input field
     */
    public StringInputDialogWindow(String caption, String fieldName) {
        super(caption);

        /**
         * set window properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);

        /**
         * create the main layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();

        /**
         * add the input field
         */
        m_inputField = new TextField(fieldName);

        m_inputField.setValue("");
        m_inputField.focus();
        m_inputField.selectAll();
        m_inputField.setImmediate(true);

        /**
         * create nested FormLayout instance
         */
        FormLayout formLayout = new FormLayout();
        formLayout.setSizeUndefined();
        formLayout.setMargin(true);
        formLayout.addComponent(m_inputField);

        /**
         * add the buttons in a horizontal layout
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth("100%");

        /**
         * create cancel button
         */
        m_cancelButton = new Button("Cancel");
        m_cancelButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        m_cancelButton.addClickListener(this);

        horizontalLayout.addComponent(m_cancelButton);
        horizontalLayout.setExpandRatio(m_cancelButton, 1);
        horizontalLayout.setComponentAlignment(m_cancelButton, Alignment.TOP_RIGHT);

        /**
         * create ok button
         */
        m_okButton = new Button("OK");
        m_okButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        m_okButton.addClickListener(this);

        horizontalLayout.addComponent(m_okButton);
        formLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(formLayout);

        /**
         * the close listener
         */
        addCloseListener(this);

        /**
         * set the content
         */
        setContent(verticalLayout);
    }

    /**
     * Sets the validator to be used.
     * @param validator the validator
     * @return the instance itself
     */
    public StringInputDialogWindow withValidator(Validator validator) {
        m_inputField.addValidator(validator);
        return this;
    }

    /**
     * Returns the value of the input field.
     *
     * @return the current value
     */
    public String getValue() {
        return m_inputField.getValue();
    }

    /**
     * Sets the caption of the window.
     *
     * @param caption the caption to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    /**
     * Sets the label of the input field.
     *
     * @param fieldName the field name to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withFieldName(String fieldName) {
        m_inputField.setCaption(fieldName);
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is confirmed.
     *
     * @param okAction the action to be executed
     * @return the instance itself
     */
    public StringInputDialogWindow withOkAction(Action okAction) {
        this.m_okAction = okAction;
        return this;
    }

    /**
     * Sets the action to be performed when the dialog is cancelled.
     *
     * @param cancelAction the action to be executed
     * @return the instance itself
     */
    public StringInputDialogWindow withCancelAction(Action cancelAction) {
        this.m_cancelAction = cancelAction;
        return this;
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

    /**
     * Sets the label of the ok button.
     *
     * @param okLabel the label to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withOkLabel(String okLabel) {
        m_okButton.setCaption(okLabel);
        return this;
    }

    /**
     * Sets the label of the cancel button.
     *
     * @param cancelLabel the label to be used
     * @return the instance itself
     */
    public StringInputDialogWindow withCancelLabel(String cancelLabel) {
        m_cancelButton.setCaption(cancelLabel);
        return this;
    }

    @Override
    public void windowClose(CloseEvent e) {
        if (m_okPressed) {
            if (m_okAction != null) {
                m_okAction.execute(this);
            }
        } else {
            if (m_cancelAction != null) {
                m_cancelAction.execute(this);
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        m_okPressed = event.getSource() == m_okButton;

        if ((m_okPressed && m_inputField.isValid()) || !m_okPressed) {
            close();
        }
    }
}
