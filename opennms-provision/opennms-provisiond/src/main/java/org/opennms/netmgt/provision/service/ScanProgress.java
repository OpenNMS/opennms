package org.opennms.netmgt.provision.service;

/**
 * <p>ScanProgress interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface ScanProgress {
    
    /**
     * <p>abort</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public void abort(String message);
    /**
     * <p>isAborted</p>
     *
     * @return a boolean.
     */
    public boolean isAborted();
}
