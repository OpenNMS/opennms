package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.data.MetaConfigModel;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ServiceConfig;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;

import java.util.HashMap;
import java.util.Map;

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
			this.configFieldGroup.setItemDataSource(new BeanItem<>(new ServiceConfig()));

			initFields();

			setImmediate(true);
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
			authenticateField.setCaption(MetaConfigModel.AUTHENTICATE);
			authenticateField.addListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					updateAuthenticationFields((Boolean) event.getProperty().getValue());
				}
			});

			TextField userField = new TextField();
			userField.setCaption("User");
			userField.setNullRepresentation("");

			PasswordField passwordField = new PasswordField();
			passwordField.setCaption("Password");
			passwordField.setNullRepresentation("");

			final TextField serviceNameField = new TextField();
			serviceNameField.setCaption("Service name");
			serviceNameField.setNullRepresentation("");
			serviceNameField.setRequired(true);
			serviceNameField.setRequiredError("required");
			serviceNameField.addValidator(new RegexpValidator("^[A-Za-z0-9_-]+$",
					"You must specify a valid name. Allowed characters: (A-Za-z0-9_-)"));

			final TextField hostNameField = new TextField();
			hostNameField.setCaption("Host");
			hostNameField.setRequired(true);
			hostNameField.setRequiredError("required");

			final TextField portField = new TextField();
			portField.setCaption("Port");
			portField.setRequired(true);
			portField.setRequiredError("required");

			addComponent(serviceNameField);
			addComponent(hostNameField);
			addComponent(portField);
			addComponent(authenticateField);
			addComponent(userField);
			addComponent(passwordField);

			final Field<?> skipDefaultVMField = configFieldGroup.buildAndBind(MetaConfigModel.SKIP_DEFAULT_VM);
			skipDefaultVMField.setCaption("Skip JVM MBeans");
			addComponent(skipDefaultVMField);

			final Field<?> runWritableMbeansField = configFieldGroup.buildAndBind(MetaConfigModel.RUN_WRITABLE_MBEANS);
			runWritableMbeansField.setCaption("Run writable MBeans");
			addComponent(runWritableMbeansField);

			addComponent(configFieldGroup.buildAndBind(MetaConfigModel.JMXMP));

			configFieldGroup.bind(serviceNameField, MetaConfigModel.SERVICE_NAME);
			configFieldGroup.bind(hostNameField, MetaConfigModel.HOST);
			configFieldGroup.bind(portField, MetaConfigModel.PORT);
			configFieldGroup.bind(passwordField, MetaConfigModel.PASSWORD);
			configFieldGroup.bind(authenticateField, MetaConfigModel.AUTHENTICATE);
			configFieldGroup.bind(userField, MetaConfigModel.USER);

			updateDescriptions();
			authenticateField.setDescription("Connection requires authentication");
		}

		/**
		 * Updates the descriptions (tool tips) of each field in the form using
		 * {@link #getOptionDescriptions()
		 * }
		 */
		private void updateDescriptions() {
			final Map<String, String> optionDescriptions = getOptionDescriptions();
			for (Object property : configFieldGroup.getBoundPropertyIds()) {
				String propName = property.toString();
				if (configFieldGroup.getField(propName) != null && optionDescriptions.get(propName) != null) {
					Field field = configFieldGroup.getField(propName);
					if (field instanceof AbstractComponent) {
						((AbstractComponent) field).setDescription(optionDescriptions.get(propName));
					}
				}
			}
		}

		/**
		 * In class {@link org.opennms.features.jmxconfiggenerator.Starter} are several
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
		 *         {@link org.opennms.features.jmxconfiggenerator.Starter}
		 * @see org.opennms.features.jmxconfiggenerator.Starter
		 */
		private Map<String, String> getOptionDescriptions() {
			Map<String, String> optionDescriptions = new HashMap<>();
			for (java.lang.reflect.Field f : Starter.class.getDeclaredFields()) {
				Option ann = f.getAnnotation(Option.class);
				if (ann == null || ann.usage() == null) {
					continue;
				}
				optionDescriptions.put(f.getName(), ann.usage());
				optionDescriptions.put(ann.name().replaceAll("-", ""), ann.usage());
			}
			return optionDescriptions;
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
		UiModel model = ((JmxConfigGeneratorUI) UI.getCurrent()).getUiModel();
		configForm.configFieldGroup.setItemDataSource(new BeanItem<>(model.getServiceConfig()));
		configForm.updateAuthenticationFields(false); // default -> hide those fields
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
