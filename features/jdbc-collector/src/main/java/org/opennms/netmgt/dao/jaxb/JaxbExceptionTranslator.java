package org.opennms.netmgt.dao.jaxb;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.opennms.netmgt.dao.JAXBDataAccessFailureException;
import org.springframework.dao.DataAccessException;


public class JaxbExceptionTranslator {
    public DataAccessException translate(String task, IOException e) {
        return new JAXBDataAccessFailureException("Failed to perform IO while " + task + ": " + e, e);
    }

    public DataAccessException translate(String task, JAXBException e) {
        return new JAXBDataAccessFailureException("Failed to validate XML file while " + task + ": " + e, e);
    }
}
