package org.opennms.features.dashboard.client.portlet;


import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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


