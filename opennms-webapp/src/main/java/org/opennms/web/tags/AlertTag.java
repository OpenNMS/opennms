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
