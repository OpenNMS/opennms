/**
 * 
 */
package org.opennms.netmgt.dao;

import org.springframework.orm.ObjectRetrievalFailureException;

public class CastorObjectRetrievalFailureException extends ObjectRetrievalFailureException {
    private static final long serialVersionUID = -5906087948002738350L;

    public CastorObjectRetrievalFailureException(String message, Throwable throwable) {
        super(message, throwable);
    }
}