/* 
 * Copyright 2009 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opennms.gwt.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * A regular GWT component without integration with Vaadin.
 */
public class GwtColorPicker extends Composite implements ClickHandler {

    /** Currently selected color name to give client-side feedback to the user. */
    protected Label currentcolor = new Label();

    public GwtColorPicker() {
        // Create a 4x4 grid of buttons with names for 16 colors
        final Grid grid = new Grid(4, 4);
        final String[] colors = new String[] { "aqua", "black", "blue",
                "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive",
                "purple", "red", "silver", "teal", "white", "yellow" };
        int colornum = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++, colornum++) {
                // Create a button for each color
                final Button button = new Button(colors[colornum]);
                button.addClickHandler(this);

                // Put the button in the Grid layout
                grid.setWidget(i, j, button);

                // Set the button background colors.
                DOM.setStyleAttribute(button.getElement(), "background",
                        colors[colornum]);

                // For dark colors, the button label must be in white.
                if ("black navy maroon blue purple".indexOf(colors[colornum]) != -1) {
                    DOM.setStyleAttribute(button.getElement(), "color", "white");
                }
            }
        }

        // Create a panel with the color grid and currently selected color
        // indicator
        final HorizontalPanel panel = new HorizontalPanel();
        panel.add(grid);
        panel.add(currentcolor);

        // Set the class of the color selection feedback box to allow CSS
        // styling.
        // We need to obtain the DOM element for the current color label.
        // This assumes that the <td> element of the HorizontalPanel is
        // the parent of the label element. Notice that the element has no
        // parent
        // before the widget has been added to the horizontal panel.
        final Element panelcell = DOM.getParent(currentcolor.getElement());
        DOM.setElementProperty(panelcell, "className",
                "colorpicker-currentcolorbox");

        // Set initial color. This will be overridden with the value read from
        // server.
        setColor("white");

        // Composite GWT widgets must call initWidget().
        initWidget(panel);
    }

    /** Sets the currently selected color. */
    public void setColor(String newcolor) {
        // Give client-side feedback by changing the color name in the label
        currentcolor.setText(newcolor);

        // Obtain the DOM elements. This assumes that the <td> element
        // of the HorizontalPanel is the parent of the label element.
        final Element nameelement = currentcolor.getElement();
        final Element cell = DOM.getParent(nameelement);

        // Give feedback by changing the background color
        DOM.setStyleAttribute(cell, "background", newcolor);
        DOM.setStyleAttribute(nameelement, "background", newcolor);
        if ("black navy maroon blue purple".indexOf(newcolor) != -1) {
            DOM.setStyleAttribute(nameelement, "color", "white");
        } else {
            DOM.setStyleAttribute(nameelement, "color", "black");
        }
    }

    /** Handles click on a color button. */
	public void onClick(ClickEvent event) {
		// Use the button label as the color name to set
		setColor(((Button) event.getSource()).getText());
	}
}
