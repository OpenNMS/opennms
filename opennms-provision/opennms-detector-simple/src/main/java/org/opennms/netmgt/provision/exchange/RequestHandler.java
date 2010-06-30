package org.opennms.netmgt.provision.exchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>RequestHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface RequestHandler {
    /**
     * <p>doRequest</p>
     *
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.io.IOException if any.
     */
    public void doRequest(OutputStream out) throws IOException;
}
