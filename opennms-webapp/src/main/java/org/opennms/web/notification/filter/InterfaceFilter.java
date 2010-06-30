package org.opennms.web.notification.filter;

import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;



/**
 * Encapsulates all interface filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class InterfaceFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="interface"</code> */
    public static final String TYPE = "interface";

    /**
     * <p>Constructor for InterfaceFilter.</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public InterfaceFilter(String ipAddress) {
        super(TYPE, SQLType.STRING, "INTERFACEID", "ipAddress", ipAddress);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<WebNotificationRepository.InterfaceFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return getValue();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
}
