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
 * *****************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.jmxconfiggenerator.webui.JmxConfigGeneratorApplication;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaConfigModel;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.ServiceConfig;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextField;

/**
 * This form handles editing of a {@link ServiceConfig} model.
 * 
 * @author Markus von Rüden <mvr@opennms.com>
 * @see ServiceConfig
 */
public class ConfigForm extends Form implements ModelChangeListener<UiModel>, ClickListener {

	private JmxConfigGeneratorApplication app;

	private ButtonPanel buttonPanel = new ButtonPanel(this);

	public ConfigForm(JmxConfigGeneratorApplication app) {
		this.app = app;
		setImmediate(true);
		setDescription(UIHelper.loadContentFromFile(getClass(), "/descriptions/ServiceConfiguration.html"));
		setWriteThrough(true);
		setFooter(buttonPanel);
	}

	private Object[] createVisibleItemProperties() {
		return new Object[] { MetaConfigModel.SERVICE_NAME, MetaConfigModel.JMXMP, MetaConfigModel.HOST,
				MetaConfigModel.PORT, MetaConfigModel.AUTHENTICATE, MetaConfigModel.USER, MetaConfigModel.PASSWORD,
				MetaConfigModel.SKIP_DEFAULT_VM, MetaConfigModel.RUN_WRITABLE_MBEANS };
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (buttonPanel.getNext().equals(event.getButton())) {
			if (!isValid()) {
				UIHelper.showValidationError(getWindow(),
						"There are still errors on this page. You cannot continue. Please check if all required fields have been filled.");
				return;
			}
			app.updateView(UiState.MbeansDetection);
		}
		if (buttonPanel.getPrevious().equals(event.getButton())) {
			app.updateView(UiState.IntroductionView);
		}
	}

	/**
	 * Toggles the visibility of user and password fields. The fields are shown
	 * if "authenticate" checkbox is presssed. Otherwise they are not shown.
	 */
	private void updateAuthenticationFields(boolean visible) throws ReadOnlyException, ConversionException {
		getField(MetaConfigModel.USER).setVisible(visible);
		getField(MetaConfigModel.PASSWORD).setVisible(visible);
		if (!visible) {
			getField(MetaConfigModel.USER).setValue(null);
			getField(MetaConfigModel.PASSWORD).setValue(null);
		}
	}

	/**
	 * DefaultFieldFactory works for us, we only add some additional stuff to
	 * each field -> if needed.
	 * 
	 */
	private void initFields() {
		getField(MetaConfigModel.AUTHENTICATE).addListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				updateAuthenticationFields((Boolean) event.getProperty().getValue());
			}
		});
		((TextField) getField(MetaConfigModel.USER)).setNullRepresentation("");
		((TextField) getField(MetaConfigModel.PASSWORD)).setNullRepresentation("");
		((TextField) getField(MetaConfigModel.PASSWORD)).setSecret(true); // use
																			// PasswordField
																			// instead

		final TextField serviceNameField = ((TextField) getField(MetaConfigModel.SERVICE_NAME));
		serviceNameField.setNullRepresentation("");
		serviceNameField.setRequired(true);
		serviceNameField.setRequiredError("required");
		serviceNameField.addValidator(new RegexpValidator("^[A-Za-z0-9_-]+$",
				"You must specify a valid name. Allowed characters: (A-Za-z0-9_-)"));

		final TextField hostNameField = ((TextField) getField(MetaConfigModel.HOST));
		hostNameField.setRequired(true);
		hostNameField.setRequiredError("required");

		final TextField portField = ((TextField) getField(MetaConfigModel.PORT));
		portField.setRequired(true);
		portField.setRequiredError("required");
	}

	/**
	 * Updates the descriptions (tool tips) of each field in the form using
	 * {@link #getOptionDescriptions()
     * }
	 */
	private void updateDescriptions() {
		Map<String, String> optionDescriptions = getOptionDescriptions();
		Starter.class.getAnnotation(org.kohsuke.args4j.Option.class);
		for (Object property : getVisibleItemProperties()) {
			String propName = property.toString();
			if (getField(propName) != null && optionDescriptions.get(propName) != null) {
				getField(propName).setDescription(optionDescriptions.get(propName));
			}
		}
	}

	/**
	 * In class {@link org.opennms.tools.jmxconfiggenerator.Starter} are several
	 * command line options defined. Each option has a name (mandatory) and a
	 * description (optional). This method gets all descriptions and assign each
	 * description to the name. If the option starts with at least one '-' all
	 * '-' are removed. Therefore the builded map looks like:
	 * 
	 * <pre>
	 *      {key} -> {value}
	 *      "force" -> "this option forces the deletion of the file"
	 * </pre>
	 * 
	 * @return a Map containing a description for each option defined in
	 *         {@link org.opennms.tools.jmxconfiggenerator.Starter}
	 * @see org.opennms.tools.jmxconfiggenerator.Starter
	 */
	private Map<String, String> getOptionDescriptions() {
		Map<String, String> optionDescriptions = new HashMap<String, String>();
		for (java.lang.reflect.Field f : Starter.class.getDeclaredFields()) {
			Option ann = f.getAnnotation(Option.class);
			if (ann == null || ann.usage() == null) {
				continue;
			}
			optionDescriptions.put(ann.name().replaceAll("-", ""), ann.usage());
		}
		return optionDescriptions;
	}

	@Override
	public void modelChanged(UiModel newModel) {
		setItemDataSource(new BeanItem<ServiceConfig>(newModel.getServiceConfig()));
		setVisibleItemProperties(createVisibleItemProperties());
		initFields();
		updateDescriptions();
		updateAuthenticationFields(false); // default -> hide those fields
	}
}
