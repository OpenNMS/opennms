package org.opennms.client;

/**
 * This exception is used in cases where the user attempted to invoke an
 * RPC service that requires ownership (like changing the admin password,
 * saving database settings, etc.) and the ownership touchfile has not been
 * detected in the OpenNMS home directory.
 */
public class OwnershipNotConfirmedException extends Exception {

}
