/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.tags;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.opennms.web.alert.Alert;
import org.opennms.web.alert.AlertType;
import org.springframework.web.servlet.ModelAndView;

/**
 * Is used to render messages from any controller in the ui.
 * Another word for alert could be notification, message, information.
 * The tag is used to inform the user e.g. about successful operations.
 */
public class AlertTag extends SimpleTagSupport {

    /**
     * The name of the parameter by which the alert collection is stored inside the (Http)Request.
     */
    private static final String REQUEST_PARAMETER_NAME = "#__ALERTS__#";

    /**
     * Each {@link Alert} is rendered like this.
     */
    private static final String TEMPLATE = "<div class=\"alert {0}\">{1}</div>";

    /**
     * Adds the message as an {@link Alert} with type={@link AlertType#INFO} to the current modelAndView.
     * @param modelAndView The request where to save the alert in.
     * @param message The alert message (e.g. Download successful)
     */
    public static void addAlertToRequest(ModelAndView modelAndView, String message) {
        addAlertToRequest(modelAndView, message, AlertType.INFO);
    }

    /**
     * Adds the message as an {@link Alert} with the given alertType to the current modelAndView.
     * @param modelAndView the request where to save the alert in.
     * @param message  The alert message (e.g. Download failed)
     * @param alertType The alert type (e.g. {@link AlertType#ERROR})
     */
    public static void addAlertToRequest(ModelAndView modelAndView, String message, AlertType alertType) {
        if (modelAndView.getModel().get(REQUEST_PARAMETER_NAME) == null)
            modelAndView.addObject(REQUEST_PARAMETER_NAME, new ArrayList<Alert>());
        Collection<Alert> alerts = ((Collection<Alert>)modelAndView.getModel().get(REQUEST_PARAMETER_NAME));
        alerts.add(new Alert(message, alertType));
    }

    /**
     * Performs an type to css-class mapping.
     */
    private static String getStyle(AlertType type) {
        switch (type) {
        case INFO: return "alert-info";
        case SUCCESS: return "alert-success";
        default: return "alert-danger";
        }
    }

    @Override
    public void doTag() throws JspException, IOException {
        Object alertsObject = getJspContext().findAttribute(REQUEST_PARAMETER_NAME);
        if (alertsObject == null) return;
        if (!(alertsObject instanceof Collection)) return;
        Collection<Alert> alerts = (Collection<Alert>)alertsObject;
        for (Alert eachAlert : alerts) {
            String alertOutput = MessageFormat.format(
                    TEMPLATE,
                    getStyle(eachAlert.getType()),
                    eachAlert.getMessage());
            getJspContext().getOut().write(alertOutput);
        }
    }
}
