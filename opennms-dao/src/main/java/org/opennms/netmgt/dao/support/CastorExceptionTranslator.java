package org.opennms.netmgt.dao.support;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.springframework.dao.DataAccessException;

/**
 * This is modelled after the Spring SQLExceptionTrnaslator.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class CastorExceptionTranslator {
    public DataAccessException translate(String task, IOException e) {
        return new CastorDataAccessFailureException("Failed to perform IO while " + task + ": " + e, e);
    }
    
    public DataAccessException translate(String task, ValidationException e) {
        return new CastorDataAccessFailureException("Failed to validate XML file while " + task + ": " + e, e);
    }
    
    public DataAccessException translate(String task, MarshalException e) {
        return new CastorDataAccessFailureException("Failed to marshal/unmarshal XML file while " + task + ": " + e, e);
    }
}
