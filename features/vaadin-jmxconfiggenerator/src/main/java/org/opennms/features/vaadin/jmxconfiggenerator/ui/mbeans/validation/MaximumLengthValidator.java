package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.data.validator.StringLengthValidator;
import org.opennms.features.vaadin.jmxconfiggenerator.Config;

public class MaximumLengthValidator extends StringLengthValidator {

    protected static final String ERROR_MESSAGE = String.format("Maximal length is %d", Config.ATTRIBUTES_ALIAS_MAX_LENGTH);

    public MaximumLengthValidator() {
        super(ERROR_MESSAGE, 0, Config.ATTRIBUTES_ALIAS_MAX_LENGTH, false);
    }
}
