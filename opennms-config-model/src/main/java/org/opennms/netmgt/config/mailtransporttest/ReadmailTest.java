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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class ReadmailTest.
 */

@XmlRootElement(name="readmail-test")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReadmailTest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Field m_debug.
     */
    @XmlAttribute(name="debug")
    private Boolean m_debug;

    /**
     * Field m_mailFolder.
     */
    @XmlAttribute(name="mail-folder")
    private String m_mailFolder;

    /**
     * Field m_subjectMatch.
     */
    @XmlAttribute(name="subject-match")
    private String m_subjectMatch;

    /**
     * Field m_attemptInterval.
     */
    @XmlAttribute(name="attempt-interval")
    private Long m_attemptInterval;

    /**
     * Field m_deleteAllMail.
     */
    @XmlAttribute(name="delete-all-mail")
    private Boolean m_deleteAllMail;

    /**
     * Use these name value pairs to configure freeform properties
     * from the JavaMail class.
     *  
     *  
     */
    @XmlElement(name="javamail-property")
    private List<JavamailProperty> m_javamailProperties = new ArrayList<JavamailProperty>();

    /**
     * Define the host and port of the sendmail server. If you
     * don't, defaults will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     */
    @XmlElement(name="readmail-host", required=true)
    private ReadmailHost m_readmailHost;

    /**
     * Configure user based authentication.
     *  
     *  
     */
    @XmlElement(name="user-auth")
    private UserAuth m_userAuth;

    public ReadmailTest() {
        super();
        setMailFolder("INBOX");
    }

    public ReadmailTest(final Long attemptInterval, final Boolean debug, final String mailFolder, final String subjectMatch, final Boolean deleteAllMail) {
        super();
        m_attemptInterval = attemptInterval;
        m_debug = debug;
        m_mailFolder = mailFolder;
        m_subjectMatch = subjectMatch;
        m_deleteAllMail = deleteAllMail;
    }

    /**
     * 
     * 
     * @param javamailProperty
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addJavamailProperty(final JavamailProperty javamailProperty) throws IndexOutOfBoundsException {
        m_javamailProperties.add(javamailProperty);
    }

    /**
     * 
     * 
     * @param index
     * @param javamailProperty
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addJavamailProperty(final int index, final JavamailProperty javamailProperty) throws IndexOutOfBoundsException {
        m_javamailProperties.add(index, javamailProperty);
    }

    public void addJavamailProperty(final String name, final String value) {
        m_javamailProperties.add(new JavamailProperty(name, value));
    }

    /**
     */
    public void deleteAttemptInterval() {
        m_attemptInterval = null;
    }

    /**
     */
    public void deleteDebug() {
        m_debug = null;
    }

    /**
     */
    public void deleteDeleteAllMail() {
        m_deleteAllMail = null;
    }

    /**
     * Method enumerateJavamailProperty.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<JavamailProperty> enumerateJavamailProperty() {
        return Collections.enumeration(m_javamailProperties);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof ReadmailTest) {
            final ReadmailTest temp = (ReadmailTest)obj;
            
            if (m_debug != null) {
                if (temp.m_debug == null) {
                    return false;
                } else if (!(m_debug.equals(temp.m_debug))) {
                    return false;
                }
            } else if (temp.m_debug != null) {
                return false;
            }
            if (m_mailFolder != null) {
                if (temp.m_mailFolder == null) {
                    return false;
                } else if (!(m_mailFolder.equals(temp.m_mailFolder))) {
                    return false;
                }
            } else if (temp.m_mailFolder != null) {
                return false;
            }
            if (m_subjectMatch != null) {
                if (temp.m_subjectMatch == null) {
                    return false;
                } else if (!(m_subjectMatch.equals(temp.m_subjectMatch))) {
                    return false;
                }
            } else if (temp.m_subjectMatch != null) {
                return false;
            }
            if (m_attemptInterval != null) {
                if (temp.m_attemptInterval == null) {
                    return false;
                } else if (!(m_attemptInterval.equals(temp.m_attemptInterval))) {
                    return false;
                }
            } else if (temp.m_attemptInterval != null) {
                return false;
            }
            if (m_deleteAllMail != null) {
                if (temp.m_deleteAllMail == null) {
                    return false;
                } else if (!(m_deleteAllMail.equals(temp.m_deleteAllMail))) {
                    return false;
                }
            } else if (temp.m_deleteAllMail != null) {
                return false;
            }
            if (m_javamailProperties != null) {
                if (temp.m_javamailProperties == null) {
                    return false;
                } else if (!(m_javamailProperties.equals(temp.m_javamailProperties))) {
                    return false;
                }
            } else if (temp.m_javamailProperties != null) {
                return false;
            }
            if (m_readmailHost != null) {
                if (temp.m_readmailHost == null) {
                    return false;
                } else if (!(m_readmailHost.equals(temp.m_readmailHost))) {
                    return false;
                }
            } else if (temp.m_readmailHost != null) {
                return false;
            }
            if (m_userAuth != null) {
                if (temp.m_userAuth == null) {
                    return false;
                } else if (!(m_userAuth.equals(temp.m_userAuth))) {
                    return false;
                }
            } else if (temp.m_userAuth != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'attemptInterval'.
     * 
     * @return the value of field 'AttemptInterval'.
     */
    public Long getAttemptInterval() {
        return m_attemptInterval == null? 1000L : m_attemptInterval;
    }

    /**
     * Returns the value of field 'debug'.
     * 
     * @return the value of field 'Debug'.
     */
    public Boolean getDebug() {
        return m_debug == null? true : m_debug;
    }

    /**
     * Returns the value of field 'deleteAllMail'.
     * 
     * @return the value of field 'DeleteAllMail'.
     */
    public Boolean getDeleteAllMail() {
        return m_deleteAllMail == null? false : m_deleteAllMail;
    }

    /**
     * Method getJavamailPropertyCollection.Returns a reference to
     * 'm_javamailProperties'. No type checking is performed on
     * any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<JavamailProperty> getJavamailProperties() {
        return new ArrayList<JavamailProperty>(m_javamailProperties);
    }

    /**
     * Returns the value of field 'mailFolder'.
     * 
     * @return the value of field 'MailFolder'.
     */
    public String getMailFolder() {
        return m_mailFolder == null? "INBOX" : m_mailFolder;
    }

    /**
     * Returns the value of field 'readmailHost'. The field
     * 'readmailHost' has the following description: Define the
     * host and port of the sendmail server. If you don't, defaults
     * will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     * 
     * @return the value of field 'ReadmailHost'.
     */
    public ReadmailHost getReadmailHost() {
        return m_readmailHost;
    }

    /**
     * Returns the value of field 'subjectMatch'.
     * 
     * @return the value of field 'SubjectMatch'.
     */
    public String getSubjectMatch() {
        return m_subjectMatch;
    }

    /**
     * Returns the value of field 'userAuth'. The field 'userAuth'
     * has the following description: Configure user based
     * authentication.
     *  
     *  
     * 
     * @return the value of field 'UserAuth'.
     */
    public Optional<UserAuth> getUserAuth() {
        return Optional.ofNullable(m_userAuth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_debug, m_mailFolder, m_subjectMatch, m_attemptInterval, m_deleteAllMail, m_javamailProperties, m_readmailHost, m_userAuth);
    }

    /**
     * Returns the value of field 'debug'.
     * 
     * @return the value of field 'Debug'.
     */
    public boolean isDebug() {
        return m_debug == null? true : m_debug;
    }

    /**
     * Returns the value of field 'deleteAllMail'.
     * 
     * @return the value of field 'DeleteAllMail'.
     */
    public boolean isDeleteAllMail() {
        return m_deleteAllMail == null? false : m_deleteAllMail;
    }

    /**
     * Method iterateJavamailProperty.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<JavamailProperty> iterateJavamailProperty() {
        return m_javamailProperties.iterator();
    }

    /**
     * Sets the value of field 'attemptInterval'.
     * 
     * @param attemptInterval the value of field 'attemptInterval'.
     */
    public void setAttemptInterval(final Long attemptInterval) {
        m_attemptInterval = attemptInterval;
    }

    /**
     * Sets the value of field 'debug'.
     * 
     * @param debug the value of field 'debug'.
     */
    public void setDebug(final Boolean debug) {
        m_debug = debug;
    }

    /**
     * Sets the value of field 'deleteAllMail'.
     * 
     * @param deleteAllMail the value of field 'deleteAllMail'.
     */
    public void setDeleteAllMail(final Boolean deleteAllMail) {
        m_deleteAllMail = deleteAllMail;
    }

    /**
     * Sets the value of 'm_javamailProperties' by setting it to
     * the given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param javamailProperties the Vector to set.
     */
    public void setJavamailProperties(final List<JavamailProperty> javamailProperties) {
        if (javamailProperties == null) {
            m_javamailProperties.clear();
        } else {
            m_javamailProperties = new ArrayList<>(javamailProperties);
        }
    }

    /**
     * Sets the value of field 'mailFolder'.
     * 
     * @param mailFolder the value of field 'mailFolder'.
     */
    public void setMailFolder(final String mailFolder) {
        m_mailFolder = mailFolder;
    }

    /**
     * Sets the value of field 'readmailHost'. The field
     * 'readmailHost' has the following description: Define the
     * host and port of the sendmail server. If you don't, defaults
     * will be used and
     *  ${ipaddr} is replaced with the IP address of the service.
     *  
     *  
     * 
     * @param readmailHost the value of field 'readmailHost'.
     */
    public void setReadmailHost(final ReadmailHost readmailHost) {
        if (readmailHost == null) {
            throw new IllegalArgumentException("'readmail-host' is a required element!");
        }
        m_readmailHost = readmailHost;
    }

    /**
     * Sets the value of field 'subjectMatch'.
     * 
     * @param subjectMatch the value of field 'subjectMatch'.
     */
    public void setSubjectMatch(final String subjectMatch) {
        m_subjectMatch = subjectMatch;
    }

    /**
     * Sets the value of field 'userAuth'. The field 'userAuth' has
     * the following description: Configure user based
     * authentication.
     *  
     *  
     * 
     * @param userAuth the value of field 'userAuth'.
     */
    public void setUserAuth(final UserAuth userAuth) {
        m_userAuth = userAuth;
    }

    public void setUserAuth(final String username, final String password) {
        m_userAuth = new UserAuth(username, password);
    }

}
