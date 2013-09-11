package org.opennms.web.tags;

import org.opennms.web.alert.Alert;
import org.opennms.web.alert.AlertType;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AlertTag extends SimpleTagSupport {

    private static final String REQUEST_PARAMETER_NAME = "#__ALERTS__#";

    private static final String TEMPLATE = "<div class=\"alert {0}\">{1}</div>";

    public static void addAlertToRequest(ModelAndView modelAndView, String message) {
        addAlertToRequest(modelAndView, message, AlertType.INFO);
    }

    public static void addAlertToRequest(ModelAndView modelAndView, String message, AlertType alertType) {
        if (modelAndView.getModel().get(REQUEST_PARAMETER_NAME) == null)
            modelAndView.addObject(REQUEST_PARAMETER_NAME, new ArrayList<Alert>());
        Collection<Alert> alerts = ((Collection<Alert>)modelAndView.getModel().get(REQUEST_PARAMETER_NAME));
        alerts.add(new Alert(message, alertType));
    }

    @Override
    public void doTag() throws JspException, IOException {
        Object alertsObject = getJspContext().findAttribute(REQUEST_PARAMETER_NAME);
        if (alertsObject == null) return;
        if (!(alertsObject instanceof Collection)) return;
        Collection<Alert> alerts = (Collection)alertsObject;
        for (Alert eachAlert : alerts) {
            String alertOutput = MessageFormat.format(
                    TEMPLATE,
                    getStyle(eachAlert.getType()),
                    eachAlert.getMessage());
            getJspContext().getOut().write(alertOutput);
        }
    }

    private static String getStyle(AlertType type) {
        return "alert-" + type.name().toLowerCase();
    }
}
