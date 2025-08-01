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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class Event UEI Window.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public abstract class EventUeiWindow extends Window implements Button.ClickListener {

    /** The Event UEI base. */
    private final TextField ueiBase;

    /** The OK button. */
    private final Button okButton;

    /** The CANCEL button. */
    private final Button cancelButton;

    /**
     * Instantiates a new Event Generator window.
     * 
     * @param defaultUei the default value for UEI base
     */
    public EventUeiWindow(String defaultUei) {
        setCaption("Generate Events");
        setModal(true);
        setWidth("400px");
        setHeight("150px");
        setResizable(false);
        setClosable(false);
        addStyleName("dialog");

        ueiBase = new TextField("UEI Base");
        ueiBase.setNullSettingAllowed(false);
        ueiBase.setWidth("100%");
        ueiBase.setRequired(true);
        ueiBase.setValue(defaultUei);
        ueiBase.setRequiredError("UEI Base cannot be null.");

        okButton = new Button("Continue");
        okButton.addClickListener(this);

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(this);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(okButton);
        toolbar.addComponent(cancelButton);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(ueiBase);
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
            if (ueiBase.getValue() != null && ! ((String) ueiBase.getValue()).trim().equals("")) {
                close();
                changeUeiHandler((String)ueiBase.getValue());
            }
        }
        if (btn == cancelButton) {
            close();
        }
    }

    /**
     * Change UEI handler.
     *
     * @param ueiBase the UEI base
     */
    public abstract void changeUeiHandler(String ueiBase);

}
