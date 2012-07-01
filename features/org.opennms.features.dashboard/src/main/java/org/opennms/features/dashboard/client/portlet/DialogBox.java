/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.dashboard.client.portlet;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class DialogBox  extends AbsPopup
{
	private HandlerRegistration okBtnHandler;
	private HandlerRegistration cancelBtnHandler;
	
	public DialogBox()
	{
		FlowPanel btnPanel = new FlowPanel();
		btnPanel.setWidth( "100%" );		
		btnPanel.add( cancelBtn );		
		btnPanel.add( okBtn );
		bodyPanel.setWidget( 2, 0, btnPanel );
		bodyPanel.getCellFormatter().setVerticalAlignment( 2, 0, HasVerticalAlignment.ALIGN_BOTTOM );
		bodyPanel.getCellFormatter().setHeight( 2, 0, BUTTON_PANEL_HEIGHT+"px" );
		okBtnHandler = okBtn.addClickHandler( closeHandler );
		cancelBtnHandler = cancelBtn.addClickHandler( closeHandler );
	}
	
	public DialogBox(String messageTitle)
	{
		this();
		titlePanel.setStyleName( "popup-WindowPanel-header" );		
		title.setText( messageTitle );
		bodyPanel.setWidget( 0, 0, titlePanel );
		bodyPanel.getCellFormatter().setVerticalAlignment( 0, 0, HasVerticalAlignment.ALIGN_TOP );
		bodyPanel.getCellFormatter().setHeight( 0, 0, TITLE_HEIGHT+"px" );
	}
	
	public void setMessageTitle(String messageTitle)
	{
		title.setText( messageTitle );	
	}
	
	public void setDialog(String message)
	{
		Label messageLbl=new Label( message);
		messageLbl.setSize( "100%", "100%" );
		messageBody.clear();
		messageBody.add( messageLbl );
	}
	
	public void setDialog(Widget widget)
	{
		messageBody.clear();
		messageBody.add( widget );
	}

	public Button getCancelBtn()
	{
		return cancelBtn;
	}

	public Button getOkBtn()
	{
		return okBtn;
	}

	public void addWarningImage()
	{
//		Image image=new Image( imageResource.dialogWarning());
//		imageBody.add( image );
	}
	
	public void removeOkButtonHandler()
	{
		if(okBtnHandler != null)
		{
			okBtnHandler.removeHandler();
		}
	}

	public void removeCancelButtonHandler()
	{
		if(cancelBtnHandler != null)
		{
			cancelBtnHandler.removeHandler();
		}
	}
}


