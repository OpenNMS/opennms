/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 1, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.springframework.dao.DataAccessException;

/**
 * This is modeled after the Spring SQLExceptionTrnaslator.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class CastorExceptionTranslator {
    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e a {@link java.io.IOException} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(String task, IOException e) {
        return new CastorDataAccessFailureException("Failed to perform IO while " + task + ": " + e, e);
    }
    
    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e a {@link org.exolab.castor.xml.ValidationException} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(String task, ValidationException e) {
        return new CastorDataAccessFailureException("Failed to validate XML file while " + task + ": " + e, e);
    }
    
    /**
     * <p>translate</p>
     *
     * @param task a {@link java.lang.String} object.
     * @param e a {@link org.exolab.castor.xml.MarshalException} object.
     * @return a {@link org.springframework.dao.DataAccessException} object.
     */
    public DataAccessException translate(String task, MarshalException e) {
        return new CastorDataAccessFailureException("Failed to marshal/unmarshal XML file while " + task + ": " + e, e);
    }
}
