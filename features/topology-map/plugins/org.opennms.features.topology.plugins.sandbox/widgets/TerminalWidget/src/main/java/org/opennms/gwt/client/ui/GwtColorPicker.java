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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A regular GWT component without integration with Vaadin.
 */
public class GwtColorPicker extends Composite implements KeyDownHandler, KeyUpHandler, KeyPressHandler{

    /** Currently selected color name to give client-side feedback to the user. */
    protected Label currentcolor = new Label();
    protected Button writeText;
    private Element box;
    private Element outputSpan;
    private Element inputSpan;
    private Element inputTF;
    private VerticalPanel panel;
    
    public GwtColorPicker() {
        
    	box = DOM.createElement("div");
        DOM.setElementAttribute(box, "class", "terminal");
        
        outputSpan = DOM.createElement("span");
        outputSpan.setInnerHTML("");
        DOM.setElementAttribute(outputSpan, "class", "termLine");
        
        inputSpan = DOM.createElement("span");
        DOM.setElementAttribute(inputSpan, "class", "inputLine");
        
        inputTF = DOM.createElement("input");
        DOM.setElementAttribute(inputTF, "class", "inputTF");
        DOM.setElementAttribute(inputTF, "type", "text");
        
        DOM.appendChild(box, outputSpan);
        DOM.appendChild(box, inputSpan);
        DOM.appendChild(inputSpan, inputTF);
        
        // Create a panel with the color grid and currently selected color
        // indicator
        panel = new VerticalPanel();
        DOM.appendChild(panel.getElement(), box);

        // Composite GWT widgets must call initWidget().
        initWidget(panel);
        inputTF.focus();
    }

	public void onKeyPress(KeyPressEvent event) {
		char keyCode = event.getCharCode();
		System.out.println(keyCode);
		System.out.println(KeyCodes.KEY_ENTER);
		if (keyCode == KeyCodes.KEY_ENTER){
			String current = outputSpan.getInnerHTML();
			outputSpan.setInnerHTML(current + inputTF.getPropertyString("value") + "<br />");
			int scrollHeight = box.getScrollHeight();
			int offsetHeight = box.getOffsetHeight();
			if (scrollHeight > offsetHeight){
				box.setScrollTop(scrollHeight-offsetHeight);
			}
		}
	}

	public void onKeyUp(KeyUpEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		System.out.println(keyCode);
		System.out.println(KeyCodes.KEY_ENTER);
		if (keyCode == KeyCodes.KEY_ENTER){
			String current = outputSpan.getInnerHTML();
			outputSpan.setInnerHTML(current + inputTF.getPropertyString("value") + "<br />");
			int scrollHeight = box.getScrollHeight();
			int offsetHeight = box.getOffsetHeight();
			if (scrollHeight > offsetHeight){
				box.setScrollTop(scrollHeight-offsetHeight);
			}
		}
	}

}
