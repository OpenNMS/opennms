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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Demonstration application that shows how to use a simple custom client-side
 * GWT component, the ColorPicker.
 */
@SuppressWarnings("serial")
public class ColorPickerApplication extends com.vaadin.Application {
    
	Window main = new Window("Javascript Demo");

    @Override
    public void init() {
        setMainWindow(main);
        setTheme("mytheme");
        Panel mainPanel = new Panel("SSH");
        mainPanel.setSizeUndefined();
        //ThemeResource htmlFile = new ThemeResource("layouts/SSHWindow.html");
        CustomLayout layout = new CustomLayout("example");
        mainPanel.setContent(layout);
        getMainWindow().setContent(mainPanel);
    }
}
