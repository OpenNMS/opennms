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
package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import org.opennms.netmgt.vaadin.core.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.data.MetaConfigModel;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ServiceConfig;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.data.validator.RegexpValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.PasswordField;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * This form handles editing of a {@link ServiceConfig} model.
 *
 * @author Markus von Rüden <mvr@opennms.com>
 * @see ServiceConfig
 */
public class ConfigView extends VerticalLayout implements View, Button.ClickListener {

	private static final long serialVersionUID = -9179098093927051983L;

	// Inner class for layouting the input fields
	private class ConfigForm extends FormLayout {

		private FieldGroup configFieldGroup;

		public ConfigForm() {
			this.configFieldGroup = new FieldGroup();
			this.configFieldGroup.setItemDataSource(new BeanItem<>(UIHelper.getCurrent(JmxConfigGeneratorUI.class).getUiModel().getServiceConfig()));

			initFields();
			updateAuthenticationFields(false); // default -> hide those fields

			setMargin(true);
			setSpacing(true);
		}

		/**
		 * Toggles the visibility of user and password fields. The fields are shown
		 * if "authenticate" checkbox is presssed. Otherwise they are not shown.
		 */
		private void updateAuthenticationFields(boolean visible) throws Property.ReadOnlyException, Converter.ConversionException {
			((Field<Boolean>) configFieldGroup.getField(MetaConfigModel.AUTHENTICATE)).setValue(visible);
			configFieldGroup.getField(MetaConfigModel.USER).setVisible(visible);
			configFieldGroup.getField(MetaConfigModel.PASSWORD).setVisible(visible);
			if (!visible) {
				configFieldGroup.getField(MetaConfigModel.USER).setValue(null);
				configFieldGroup.getField(MetaConfigModel.PASSWORD).setValue(null);
			}
		}

		/**
		 * DefaultFieldFactory works for us, we only add some additional stuff to
		 * each field -> if needed.
		 *
		 */
		private void initFields() {
			CheckBox authenticateField = new CheckBox();
			authenticateField.setCaption("Authentication");
			authenticateField.setId("authenticate");
			authenticateField.addValueChangeListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					updateAuthenticationFields((Boolean) event.getProperty().getValue());
				}
			});
			authenticateField.setDescription("Connection requires authentication");

			TextField userField = new TextField();
			userField.setId("authenticateUser");
			userField.setCaption("User");
			userField.setNullRepresentation("");
			userField.setDescription("Username for JMX-RMI Authentication");

			PasswordField passwordField = new PasswordField();
			passwordField.setId("authenticatePassword");
			passwordField.setCaption("Password");
			passwordField.setNullRepresentation("");
			passwordField.setDescription("Password for JMX-RMI Authentication");

			final TextField serviceNameField = new TextField();
			serviceNameField.setWidth(400, Unit.PIXELS);
			serviceNameField.setCaption("Service name");
			serviceNameField.setNullRepresentation("");
			serviceNameField.setRequired(true);
			serviceNameField.setRequiredError("required");
			serviceNameField.addValidator(new RegexpValidator("^[A-Za-z0-9_-]+$",
					"You must specify a valid name. Allowed characters: (A-Za-z0-9_-)"));
			serviceNameField.setDescription("The service name of the JMX data collection config, e.g. cassandra, jboss, tomcat");

			final TextField connectionTextField = new TextField();
			connectionTextField.setWidth(400, Unit.PIXELS);
			connectionTextField.setCaption("Connection");
			connectionTextField.setRequired(true);
			connectionTextField.setRequiredError("required");
			connectionTextField.setDescription("The JMX connection string, e.g.: <hostname>:<port> OR service:jmx:<protocol>:<sap>");

			addComponent(serviceNameField);
			addComponent(connectionTextField);
			addComponent(authenticateField);
			addComponent(userField);
			addComponent(passwordField);

			final Field<?> skipDefaultVMField = configFieldGroup.buildAndBind(MetaConfigModel.SKIP_DEFAULT_VM);
			skipDefaultVMField.setCaption("Skip JVM MBeans");
			skipDefaultVMField.setId(MetaConfigModel.SKIP_DEFAULT_VM);
			((AbstractComponent) skipDefaultVMField).setDescription("Set to include/exclude default JavaVM MBeans");
			addComponent(skipDefaultVMField);

			final Field<?> skipNonNumberField = configFieldGroup.buildAndBind(MetaConfigModel.SKIP_NON_NUMBER);
			skipNonNumberField.setCaption("Skip non-number values");
			((AbstractComponent) skipNonNumberField).setDescription("Set to include/exclude non-number values");
			addComponent(skipNonNumberField);

			configFieldGroup.bind(serviceNameField, MetaConfigModel.SERVICE_NAME);
			configFieldGroup.bind(connectionTextField, MetaConfigModel.CONNECTION);
			configFieldGroup.bind(passwordField, MetaConfigModel.PASSWORD);
			configFieldGroup.bind(authenticateField, MetaConfigModel.AUTHENTICATE);
			configFieldGroup.bind(userField, MetaConfigModel.USER);
		}
	}

	private final ConfigForm configForm;

	private final JmxConfigGeneratorUI app;

	private final ButtonPanel buttonPanel;

	public ConfigView(JmxConfigGeneratorUI app) {
		this.app = app;
		this.configForm = new ConfigForm();
		this.buttonPanel = new ButtonPanel(this);

		// we do not have a previous button
		buttonPanel.getPrevious().setVisible(false);

		addComponent(configForm);
		addComponent(buttonPanel);
		setExpandRatio(configForm, 1);
		setSizeFull();
	}


	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {

	}

	@Override
	public void buttonClick(Button.ClickEvent event) {
		if (buttonPanel.getNext().equals(event.getButton())) {
			if (!configForm.configFieldGroup.isValid()) {
				UIHelper.showValidationError(
						"There are still errors on this page. You cannot continue. Please check if all required fields have been filled.");
				return;
			}
			try {
				configForm.configFieldGroup.commit();
				app.updateView(UiState.MbeansDetection);
			} catch (FieldGroup.CommitException e) {
				UIHelper.showNotification("An unexpected error occurred.");
			}
		}
	}
}
