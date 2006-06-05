//
// This file is part of the OpenNMS(R) Application.
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.utils.ParameterMap;


/**
 * This plugin does nothing more than return true for all
 * discovered interfaces so that we have an entry point
 * in the OpenNMS GUI for accessing K5 information.
 * 
 * @author <A HREF="mailto:rchung@k5systems.com">rchung</A>
 * 
 * @version 1.1.1.1
 * 
 */
public class K5Plugin extends AbstractPlugin {

    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "K5Systems";

    /**
     * Default value for whether or not to use K5
     */
    private final static String DEFAULT_ACTIVE = "false";

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * Default to returning false unless K5 is set as active.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address) {
        return false;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address, Map qualifiers) {
        if (qualifiers != null) {
            String active = ParameterMap.getKeyedString(qualifiers, "active", DEFAULT_ACTIVE);
            if (active.equalsIgnoreCase("true")) {
                return true;
            }
        }

        return false;
    }
}

