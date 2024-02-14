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
package org.opennms.features.topology.api.support;

import java.net.URL;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
* The InfoWindow class constructs a custom Window component that contains an embedded 
* browser displaying the embeddedURL.
* @author Leonardo Bell
* @author Philip Grenon
* @version 1.0
*/
public class InfoWindow extends Window {
    /**
     * Is used to determine the label/caption for this pop up window.
     * @author Markus von Rüden
     *
     */
    public static interface LabelCreator {
            String getLabel();
    }
    
    private static final long serialVersionUID = -510407825043696244L;
	
    private static final double sizePercentage = 0.80; // Window size ratio to the main window
    private static final int widthCushion = 50; //Border cushion for width of window;
    private static final int heightCushion = 110; //Border cushion for height of window
    private Embedded infoBrowser = null; //Browser component which is directed at the Resource Graphs page

    
    public InfoWindow(final URL embeddedURL, LabelCreator labelCreator) {
        infoBrowser = new Embedded("", new ExternalResource(embeddedURL));

        String label = labelCreator == null ? "" : labelCreator.getLabel();
        setCaption(label);
        setResizable(false);
        setModal(true);
        
        /*Adds the browser to the main layout*/
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(infoBrowser);

        setContent(layout);
    }
    
    @Override
    public void attach() {
    	super.attach();
    	
    	int width = getUI().getPage().getBrowserWindowWidth();
    	int height = getUI().getPage().getBrowserWindowHeight();
    	
    	/*Sets the browser and window sizes based on the main window*/
        int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
        int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
        setWidth(windowWidth, Unit.PIXELS);
        setHeight(windowHeight, Unit.PIXELS);
        setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);
        
        /*Sets the size of the browser to fit within the sub-window*/
        infoBrowser.setType(Embedded.TYPE_BROWSER);
        infoBrowser.setWidth(browserWidth, Unit.PIXELS);
        infoBrowser.setHeight(browserHeight, Unit.PIXELS);
    }
}
