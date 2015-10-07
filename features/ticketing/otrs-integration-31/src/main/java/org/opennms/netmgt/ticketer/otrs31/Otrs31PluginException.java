package org.opennms.netmgt.ticketer.otrs31;

import org.opennms.api.integration.ticketing.PluginException;
import org.otrs.ticketconnector.OTRSError;

/**
 * Created by mvrueden on 25.11.14.
 */
public class Otrs31PluginException extends PluginException {

    private final String errorCode;
    private final String errorMessage;

    public Otrs31PluginException(OTRSError error) {
        this(error.getErrorCode(), error.getErrorMessage());
    }

    private Otrs31PluginException(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getMessage() {
        return String.format("%s: %s", errorCode, errorMessage);
    }
}
