/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.gwt.web.ui.asset.client.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.gwt.web.ui.asset.client.AssetPageConstants;
import org.opennms.gwt.web.ui.asset.client.presenter.AssetDynaPagePresenter;
import org.opennms.gwt.web.ui.asset.client.tools.DisclosurePanelCookie;
import org.opennms.gwt.web.ui.asset.client.tools.fieldsets.FieldSet;
import org.opennms.gwt.web.ui.asset.client.tools.fieldsets.FieldSetTextBox;
import org.opennms.gwt.web.ui.asset.shared.AssetDynaCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;
import org.opennms.gwt.web.ui.asset.shared.ContentElement;
import org.opennms.gwt.web.ui.asset.shared.FieldSetModel;
import org.opennms.gwt.web.ui.asset.shared.PageModel;
import org.opennms.gwt.web.ui.asset.shared.SectionModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * 
 */
public class AssetDynaPageImpl extends Composite implements AssetDynaPagePresenter.Display {

	private AssetPageConstants con = GWT.create(AssetPageConstants.class);

	AssetDynaCommand m_asset;
	Panel mainPanel;
	Button saveButton = new Button("SaveButton");
	Button resetButton = new Button("ResetButton");

	private ArrayList<FieldSet> fieldSetList = new ArrayList<FieldSet>();

	public AssetDynaPageImpl() {

		mainPanel = new VerticalPanel();
		mainPanel.add(new Label("Foo"));
		initWidget(mainPanel);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void cleanUp() {
		for (FieldSet fs : fieldSetList) {
			fs.clearChanged();
		}
	}

	@Override
	public AssetDynaCommand getData() {
		for (Iterator<FieldSet> iterator = fieldSetList.iterator(); iterator.hasNext();) {
			FieldSet fs = iterator.next();
			fs.writeValueBacktToFieldSetModel();
			GWT.log("FieldSet WriteBack: " + fs.getLabel() + " " + fs.getValue());
		}
		return m_asset;
	}

	@Override
	public HasClickHandlers getResetButton() {
		return resetButton;
	}

	@Override
	public HasClickHandlers getSaveButton() {
		return saveButton;
	}

	@Override
	public boolean isUiValid() {
		for (FieldSet fs : fieldSetList) {
			if (!fs.getError().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setData(AssetDynaCommand asset) {
		m_asset = asset;
		PageModel pageModel = asset.getPageModel();
		List<SectionModel> SectionList = pageModel.getSectionModelList();
		for (Iterator<SectionModel> sectionIterator = SectionList.iterator(); sectionIterator.hasNext();) {
			SectionModel sectionModel = sectionIterator.next();
			GWT.log("sectionModel.getName = " + sectionModel.getName());
			createSectionUi(mainPanel, sectionModel);
		}
		// GWT.log("fieldSetModel.getName = " + contentElement.getName());
		// FieldSet fs = new FieldSetTextBox(contentElement.getName(),
		// contentElement.getValue(), contentElement.getHelpText(),
		// contentElement);
		// sectionPanel.add((Widget) fs);
		// fieldSetList.add(fs);

		mainPanel.add(saveButton);
		mainPanel.add(resetButton);
	}

	/**
	 * @param sectionModel
	 */
	private void createSectionUi(Panel rootPanel, SectionModel sectionModel) {
		DisclosurePanelCookie dpc = new DisclosurePanelCookie(sectionModel.getName());
		dpc.add(new HTML("<h3>" + sectionModel.getName() + "</h3>"));
		Panel sectionPanel = new VerticalPanel();
		dpc.add(sectionPanel);
		ArrayList<ContentElement> contentElementList = sectionModel.getContentElementList();
		for (Iterator<ContentElement> contentElementIterator = contentElementList.iterator(); contentElementIterator
				.hasNext();) {
			ContentElement contentElement = contentElementIterator.next();
			if (contentElement instanceof org.opennms.gwt.web.ui.asset.shared.FieldSetModel) {
				GWT.log("Found instanceof FieldSetModel: " + contentElement);
				FieldSet fs = new FieldSetTextBox(((FieldSetModel) contentElement).getName(),
						((FieldSetModel) contentElement).getValue(), ((FieldSetModel) contentElement).getHelpText(),
						(FieldSetModel) contentElement);
				sectionPanel.add((Widget) fs);
			} else if (contentElement instanceof org.opennms.gwt.web.ui.asset.shared.SectionModel) {
				GWT.log("Found instanceof SectionModel: " + contentElement);
				createSectionUi(sectionPanel, (SectionModel)contentElement);
			}
		}
		rootPanel.add(dpc);
	}

	@Override
	public void setDataSugg(AssetSuggCommand assetSugg) {
	}

	@Override
	public void setEnable(Boolean enabled) {
		for (FieldSet fieldSet : fieldSetList) {
			fieldSet.setEnabled(enabled);
		}
		saveButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
	}

	@Override
	public void setError(String description, Throwable throwable) {
		String error = "";
		if (throwable != null) {
			error = throwable.toString();
		}
		final DialogBox dialog = new DialogBox();
		dialog.setText(description);
		VerticalPanel panel = new VerticalPanel();
		HTMLPanel html = new HTMLPanel(error);
		html.setStyleName("Message");
		panel.add(html);

		Button ok = new Button("OK");
		SimplePanel buttonPanel = new SimplePanel();
		buttonPanel.setWidget(ok);
		buttonPanel.setStyleName("Button");
		panel.add(buttonPanel);

		dialog.setPopupPosition(Window.getScrollLeft() + 100, Window.getScrollTop() + 100);
		dialog.setWidget(panel);
		ok.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				dialog.hide();
			}
		});

		dialog.show();
	}

	@Override
	public void setInfo(String info) {
	}
}
