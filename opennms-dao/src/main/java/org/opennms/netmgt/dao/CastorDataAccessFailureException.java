/**
 * 
 */
package org.opennms.netmgt.dao;

import org.springframework.dao.DataAccessResourceFailureException;

public class CastorDataAccessFailureException extends DataAccessResourceFailureException {
    private static final long serialVersionUID = -5546624359373413751L;
    
    public CastorDataAccessFailureException(String message) {
        super(message);
    }
    
    public CastorDataAccessFailureException(String message, Throwable throwable) {
        super(message, throwable);
    }
}