/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opennms.features.jmxconfiggenerator.webui.Config;
import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.ui.ButtonPanel;
import org.opennms.features.jmxconfiggenerator.webui.ui.UIHelper;
import org.opennms.features.jmxconfiggenerator.webui.ui.UiState;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.AttributeNameValidator;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.NameValidator;
import org.opennms.features.jmxconfiggenerator.webui.ui.validators.UniqueAttributeNameValidator;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Attrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.CompAttrib;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class MBeansView extends VerticalLayout implements ClickListener, ModelChangeListener, ViewStateChangedListener {

	/**
	 * Handles the ui behaviour.
	 */
	private final MBeansController controller = new MBeansController();

	/**
	 * We need an instance of the current UiModel to create the output jmx
	 * config model when clicking on 'next' button.
	 */
	private UiModel model;
	private final AbstractSplitPanel mainPanel;
	private final Layout mbeansContent;
	private final JmxConfigGeneratorApplication app;
	private final MBeansTree mbeansTree;
	private final MBeansContentTabSheet mbeansTabSheet;
	private final ButtonPanel buttonPanel = new ButtonPanel(this);
	private final NameEditForm mbeansForm = new NameEditForm(controller, new FormParameter() {
		@Override
		public boolean hasFooter() {
			return true;
		}

		@Override
		public String getCaption() {
			return "MBeans details";
		}

		@Override
		public String getEditablePropertyName() {
			return MetaMBeanItem.NAME;
		}

		@Override
		public String getNonEditablePropertyName() {
			return MetaMBeanItem.OBJECTNAME;
		}

		@Override
		public Object[] getVisiblePropertieNames() {
			return new Object[] { MetaMBeanItem.SELECTED, MetaMBeanItem.OBJECTNAME, MetaMBeanItem.NAME };
		}

		@Override
		public EditControls.Callback getAdditionalCallback() {
			return new EditControls.Callback<Form>() {
				@Override
				public void callback(EditControls.ButtonType type, Form outer) {
					if (type == EditControls.ButtonType.save && outer.isValid()) {
						controller.updateMBeanIcon();
						controller.updateMBean();
					}
				}
			};
		}
	});

	public MBeansView(JmxConfigGeneratorApplication app) {
		this.app = app;
		setSizeFull();
		mbeansTabSheet = new MBeansContentTabSheet(controller);
		mbeansTree = new MBeansTree(controller);
		mbeansContent = initContentPanel(mbeansForm, mbeansTabSheet);
		mainPanel = initMainPanel(mbeansTree, mbeansContent);

		registerListener(controller);

		addComponent(mainPanel);
		addComponent(buttonPanel);
		setExpandRatio(mainPanel, 1);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().equals(buttonPanel.getPrevious())) {
			app.updateView(UiState.ServiceConfigurationView);
		}
		if (event.getButton().equals(buttonPanel.getNext())) {
			if (!isValid()) {
				UIHelper.showValidationError("There are errors on this view. Please fix them first");
				return;
			}
			model.setJmxDataCollectionAccordingToSelection(controller
					.createJmxDataCollectionAccordingToSelection(model));
			app.updateView(UiState.ResultConfigGeneration);
		}
	}

	private AbstractSplitPanel initMainPanel(Component first, Component second) {
		AbstractSplitPanel layout = new HorizontalSplitPanel();
		layout.setSizeFull();
		layout.setLocked(false);
		layout.setSplitPosition(20, UNITS_PERCENTAGE);
		layout.setFirstComponent(first);
		layout.setSecondComponent(second);
		layout.setCaption(first.getCaption());
		return layout;
	}

	private Layout initContentPanel(NameEditForm form, MBeansContentTabSheet tabSheet) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(false);
		layout.addComponent(form);
		layout.addComponent(tabSheet);
		layout.setExpandRatio(tabSheet, 1);
		return layout;
	}

	@Override
	public void modelChanged(Object newModel) {
		if (newModel instanceof UiModel) {
			model = (UiModel) newModel;
			// forward to all sub elements of this view
			controller.notifyObservers(UiModel.class, newModel); 
		}
	}

	private Panel wrapToPanel(Component component) {
		Panel panel = new Panel(component.getCaption());
		panel.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(false);
		layout.setSpacing(false);
		layout.setSizeFull();
		layout.addComponent(component);

		panel.setContent(layout);
		component.setCaption(null);
		return panel;
	}

	private void registerListener(MBeansController controller) {
		controller.registerListener(Item.class, mbeansForm);
		controller.registerListener(Mbean.class, mbeansTabSheet);
		controller.registerListener(UiModel.class, mbeansTree);
		controller.registerListener(UiModel.class, controller);
		controller.addView(mbeansForm);
		controller.addView(mbeansTabSheet);
		controller.addView(mbeansTree);
		controller.addView(this);
	}

	// TODO the whole validation is made twice :-/
	// TODO we can fix that when there is a central "ValidationStrategy"-Handler instance or so
	private boolean isValid() {
		List<InvalidValueException> exceptionList = new ArrayList<InvalidValueException>();
		NameValidator nameValidator = new NameValidator();
		
		Validator attributeNameValidator = new AttributeNameValidator();
		Validator attributeLengthValidator = new StringLengthValidator(String.format("Maximal length is %d", Config.ATTRIBUTES_ALIAS_MAX_LENGTH), 0, Config.ATTRIBUTES_ALIAS_MAX_LENGTH, false);  // TODO do it more dynamically
		UniqueAttributeNameValidator attributeUniqueNameValidator = new UniqueAttributeNameValidator(controller, new HashMap<Object, Field<String>>());
		
		
		// 1. validate each MBean (Mbean name without required check!)
		for (Mbean eachMBean : controller.getSelectedMbeans()) {
			validate(nameValidator, eachMBean.getName(), exceptionList); // TODO do it more dynamically
			
			// 2. validate each CompositeAttribute
			for (CompAttrib eachCompositeAttribute : controller.getSelectedCompositeAttributes(eachMBean)) {
				validate(nameValidator, eachCompositeAttribute.getName(), exceptionList); // TODO do it more dynamically
				
				for (org.opennms.xmlns.xsd.config.jmx_datacollection.CompMember eachCompMember : controller.getSelectedCompositeMembers(eachCompositeAttribute)) {
					validate(attributeNameValidator, eachCompMember.getAlias(), exceptionList); // TODO do it more dynamically
					validate(attributeLengthValidator, eachCompMember.getAlias(), exceptionList); // TODO do it more dynamically
					validate(attributeUniqueNameValidator, eachCompMember.getAlias(), exceptionList); // TODO do it more dynamically
				}
			}
			
			// 3. validate each Attribute
			for (Attrib eachAttribute : controller.getSelectedAttributes(eachMBean)) {
				validate(attributeNameValidator, eachAttribute.getAlias(), exceptionList); // TODO do it more dynamically
				validate(attributeLengthValidator, eachAttribute.getAlias(), exceptionList); // TODO do it more dynamically
				validate(attributeUniqueNameValidator, eachAttribute.getAlias(), exceptionList); // TODO do it more dynamically				
			} 
		}
		return exceptionList.isEmpty();
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		// hide next, previous buttons if in edit mode
		buttonPanel.getPrevious().setEnabled(event.getNewState() != ViewState.Edit);
		buttonPanel.getNext().setEnabled(event.getNewState() != ViewState.Edit);
	}
	
	private static void validate(Validator validator, Object value, List<InvalidValueException> exceptionList) {
		try {
			validator.validate(value); // TODO do it more dynamically
		} catch (Validator.InvalidValueException ex) {
			exceptionList.add(ex);
		}
	}
}
