package org.opennms.client;

/**
 * This exception is used in cases where the user attempted to invoke an
 * RPC service that requires ownership (like changing the admin password,
 * saving database settings, etc.) and the ownership touchfile has not been
 * detected in the OpenNMS home directory.
 *
 * @author ranger
 * @version $Id: $
 */
public class OwnershipNotConfirmedException extends Exception {
    private static final long serialVersionUID = 1504250595088693606L;
}
