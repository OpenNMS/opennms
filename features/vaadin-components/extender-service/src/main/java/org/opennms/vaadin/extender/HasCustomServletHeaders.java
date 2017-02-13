package org.opennms.vaadin.extender;

import java.util.Map;

/**
 * Implement this interface on your {@link ApplicationFactory} to return a list of custom headers
 * that should be added to the AJAX responses of your Vaadin application.

 * @author ranger
 */
public interface HasCustomServletHeaders {
    public Map<String,String> getHeaders();
}
