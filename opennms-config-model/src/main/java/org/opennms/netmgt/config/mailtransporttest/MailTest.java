/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.mailtransporttest;

  import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a use case for testing mail. If both a send and a read
 * test are
 *  configured, then the use case (d) will be executed.
 */

@XmlRootElement(name="mail-test")
@XmlAccessorType(XmlAccessType.FIELD)
public class MailTest implements Serializable {
    private static final long serialVersionUID = -1481837134706190322L;

    /**
     * This entity defines the test for sending mail. Attributes
     * are used to
     *  derive values of java mail properties, or, they can be
     * specified directly
     *  as key value pairs. Attributes will are easier to read but
     * there isn't 
     *  an attribute for every javamail property possible (some are
     * fairly obscure).
     *  
     *  
     */
    @XmlElement(name="sendmail-test")
    private SendmailTest m_sendmailTest;

    /**
     * Field m_readmailTest.
     */
    @XmlElement(name="readmail-test")
    private ReadmailTest m_readmailTest;


      //----------------/
     //- Constructors -/
    //----------------/

    public MailTest() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof MailTest) {
            final MailTest temp = (MailTest)obj;
            if (m_sendmailTest != null) {
                if (temp.m_sendmailTest == null) {
                    return false;
                } else if (!(m_sendmailTest.equals(temp.m_sendmailTest))) {
                    return false;
                }
            } else if (temp.m_sendmailTest != null) {
                return false;
            }
            if (m_readmailTest != null) {
                if (temp.m_readmailTest == null) {
                    return false;
                } else if (!(m_readmailTest.equals(temp.m_readmailTest))) {
                    return false;
                }
            } else if (temp.m_readmailTest != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'readmailTest'.
     * 
     * @return the value of field 'ReadmailTest'.
     */
    public ReadmailTest getReadmailTest() {
        return m_readmailTest;
    }

    /**
     * Returns the value of field 'sendmailTest'. The field
     * 'sendmailTest' has the following description: This entity
     * defines the test for sending mail. Attributes are used to
     *  derive values of java mail properties, or, they can be
     * specified directly
     *  as key value pairs. Attributes will are easier to read but
     * there isn't 
     *  an attribute for every javamail property possible (some are
     * fairly obscure).
     *  
     *  
     * 
     * @return the value of field 'SendmailTest'.
     */
    public SendmailTest getSendmailTest() {
        return m_sendmailTest;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_sendmailTest != null) {
           result = 37 * result + m_sendmailTest.hashCode();
        }
        if (m_readmailTest != null) {
           result = 37 * result + m_readmailTest.hashCode();
        }
        
        return result;
    }

    /**
     * Sets the value of field 'readmailTest'.
     * 
     * @param readmailTest the value of field 'readmailTest'.
     */
    public void setReadmailTest(final ReadmailTest readmailTest) {
        m_readmailTest = readmailTest;
    }

    /**
     * Sets the value of field 'sendmailTest'. The field
     * 'sendmailTest' has the following description: This entity
     * defines the test for sending mail. Attributes are used to
     *  derive values of java mail properties, or, they can be
     * specified directly
     *  as key value pairs. Attributes will are easier to read but
     * there isn't 
     *  an attribute for every javamail property possible (some are
     * fairly obscure).
     *  
     *  
     * 
     * @param sendmailTest the value of field 'sendmailTest'.
     */
    public void setSendmailTest(final SendmailTest sendmailTest) {
        m_sendmailTest = sendmailTest;
    }

}
