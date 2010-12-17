package org.opennms.web.controller.admin.support;

public class FormatterNotFoundException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public FormatterNotFoundException(final String message) {
        super(message);
    }
}
