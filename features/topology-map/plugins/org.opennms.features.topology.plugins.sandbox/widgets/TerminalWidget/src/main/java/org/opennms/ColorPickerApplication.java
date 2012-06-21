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

import com.vaadin.ui.Button;
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
        
        Button openWindow = new Button("Open Window");
        openWindow.addListener(new Button.ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				showSSHWindow();
			}
		});
        getMainWindow().addComponent(openWindow);

    }

	private void showSSHWindow() {
		getMainWindow().addWindow(getSSHWindow());
	}

	private Window getSSHWindow() {
		return new SSHWindow((int)getMainWindow().getWidth(), (int)getMainWindow().getHeight());
	}
}
