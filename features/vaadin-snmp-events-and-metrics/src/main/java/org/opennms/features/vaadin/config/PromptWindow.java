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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class Prompt Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class PromptWindow extends Window implements Button.ClickListener {

    /** The Event File Name base. */
    private final TextField fileName;

    /** The OK button. */
    private final Button okButton;

    /** The CANCEL button. */
    private final Button cancelButton;

    /**
     * Instantiates a new Event Generator window.
     *
     * @param caption the caption
     * @param fieldLabel the field label
     */
    public PromptWindow(String caption, String fieldLabel) {
        setCaption(caption);
        setModal(true);
        setWidth("400px");
        setHeight("150px");
        setResizable(false);
        setClosable(false);
        addStyleName("dialog");

        fileName = new TextField(fieldLabel);
        fileName.setNullSettingAllowed(false);
        fileName.setWidth("100%");
        fileName.setRequired(true);
        fileName.setRequiredError("This field cannot be null.");

        okButton = new Button("Continue");
        okButton.addClickListener(this);

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(okButton);
        toolbar.addComponent(cancelButton);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(fileName);
        layout.addComponent(toolbar);
        layout.setComponentAlignment(toolbar, Alignment.BOTTOM_RIGHT);

        setContent(layout);
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
     */
    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button btn = event.getButton();
        if (btn == okButton) {
            if (fileName.getValue() != null && ! ((String) fileName.getValue()).trim().equals("")) {
                textFieldChanged((String)fileName.getValue());
            }
        }
        close();
    }

    /**
     * Text field changed.
     *
     * @param fieldValue the field value
     */
    public abstract void textFieldChanged(String fieldValue);

}
