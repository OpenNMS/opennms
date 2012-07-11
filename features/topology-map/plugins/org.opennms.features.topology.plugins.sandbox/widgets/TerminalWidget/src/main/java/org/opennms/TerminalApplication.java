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

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * Vaadin application for terminal emulation and SSH functionality
 * 
 * @author pdgrenon
 * @author lmbell
 */
@SuppressWarnings("serial")
public class TerminalApplication extends com.vaadin.Application {

    Window main = new Window("Vaadin Application"); // The main window used as the background of the application
    private String host = "debian.opennms.org"; // The host name to be connected to through SSH
    private int port = 22; // The port number to be connected to through SSH

    /**
     * Initialize the application and sets the theme
     */
    @Override
    public void init() {
        final SSHOperation operation = new SSHOperation();
        setMainWindow(main);
        setTheme("mytheme");
        Button openWindow = new Button("Open Window");
        openWindow.addListener(new Button.ClickListener() {
			
            
			public void buttonClick(ClickEvent event) {
				//showAuthWindow();
				operation.execute(null, getOperationContext());
				
			}
		});

        getMainWindow().addComponent(openWindow);

    }
    /**
     * This methods adds (shows) the authorization window to the main application
     */
    private void showAuthWindow() {
        getMainWindow().addWindow(getAuthWindow());
    }
	protected OperationContext getOperationContext() {
        return new OperationContext() {
            
            public Window getMainWindow() {
                
                return getMainWindow();
            }
            
            public GraphContainer getGraphContainer() {
               
                return null;
            }
        };
    }

    private void showAuthWindow() {
		getMainWindow().addWindow(getAuthWindow());
	}

	private Window getAuthWindow() {
		return new AuthWindow(this, getMainWindow(), host, port);
	}

	public void windowClose(CloseEvent e) {
		
	}
}
