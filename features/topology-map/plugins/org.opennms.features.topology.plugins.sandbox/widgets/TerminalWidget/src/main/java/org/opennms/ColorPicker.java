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

package org.opennms;

import java.util.Map;

import org.opennms.gwt.client.ui.VColorPicker;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ClientWidget;

@SuppressWarnings("serial")
@ClientWidget(VColorPicker.class)
public class ColorPicker extends AbstractField {

    public ColorPicker() {
        super();
    }

    /** The property value of the field is a String. */
    @Override
    public Class<?> getType() {
        return String.class;
    }

    /** Set the currently selected color. */
    public void setColor(String newcolor) {
        // Sets the color name as the property of the component.
        // Setting the property will automatically cause repainting of
        // the component with paintContent().
        setValue(newcolor);
    }

    /** Retrieve the currently selected color. */
    public String getColor() {
        return (String) getValue();
    }

    /** Paint (serialize) the component for the client. */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        // Superclass writes any common attributes in the paint target.
        super.paintContent(target);

        // Add the currently selected color as a variable in the paint
        // target.
        target.addVariable(this, "colorname", getColor());
    }

    /** Deserialize changes received from client. */
    @SuppressWarnings("rawtypes")
    @Override
    public void changeVariables(Object source, Map variables) {
        // Sets the currently selected color
        if (variables.containsKey("colorname") && !isReadOnly()) {
            final String newValue = (String) variables.get("colorname");
            // Changing the property of the component will
            // trigger a ValueChangeEvent
            setValue(newValue, true);
        }
    }
    
}
