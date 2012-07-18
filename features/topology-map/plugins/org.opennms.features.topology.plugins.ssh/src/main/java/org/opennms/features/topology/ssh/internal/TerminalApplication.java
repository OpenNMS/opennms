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

package org.opennms.features.topology.ssh.internal;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.ssh.internal.operations.ContextSSHOperation;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

/**
 * Vaadin application for terminal emulation and SSH functionality
 * 
 * @author pdgrenon
 * @author lmbell
 */
@SuppressWarnings("serial")
public class TerminalApplication extends com.vaadin.Application {

    Window main = new Window("Vaadin Application"); // The main window used as the background of the application
    OperationContext opContext;
    
    /**
     * Initialize the application and sets the theme
     */
    @Override
    public void init() {
        setMainWindow(main);
        setTheme("mytheme");
    	
        final ContextSSHOperation operation = new ContextSSHOperation();
        opContext = new OperationContext() {
            
            public Window getMainWindow() {
                return main;
            }
            
            public GraphContainer getGraphContainer() {
                return null;
            }
        };
  
        Button openWindow = new Button("Open Window");
        openWindow.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				//showAuthWindow();
				operation.execute(null, opContext);
				
			}
		});

        getMainWindow().addComponent(openWindow);
    }

}
