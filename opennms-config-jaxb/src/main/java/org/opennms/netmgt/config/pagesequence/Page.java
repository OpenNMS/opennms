/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.pagesequence;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * This element specifies all the possible attributes in as fine
 * grained detail as possible. All that
 *  is really required (as you can see below) is the "path"
 * attribute. From that one attribute,
 *  the IP address passed in through the ServiceMonitor and
 * ServiceCollector interface, the URL will be
 *  fully generated using the supplied defaults in this config.
 * Configure attributes these attributes to
 *  the level of detail you need to fully control the behavior.
 *  
 *  A little bit of indirection is possible here with the host
 * attribute. If the host attribute is anything
 *  other than the default, that value will be used instead of the
 * IP address passed in through the API (Interface).
 */

@XmlRootElement(name="page")
@XmlAccessorType(XmlAccessType.FIELD)
public class Page implements Serializable {
    private static final long serialVersionUID = -2844759082823034444L;

    private static final SessionVariable[] EMPTY_SESSION_VARIABLE_LIST = new SessionVariable[0];
    private static final Parameter[] EMPTY_PARAMETER_LIST = new Parameter[0];

    /**
     * Field m_method.
     */
    @XmlAttribute(name="method")
    private String m_method = "GET";

    /**
     * Field m_httpVersion.
     */
    @XmlAttribute(name="http-version")
    private String m_httpVersion = "1.1";

    /**
     * Field m_userAgent.
     */
    @XmlAttribute(name="user-agent")
    private String m_userAgent;

    /**
     * Field m_virtualHost.
     */
    @XmlAttribute(name="virtual-host")
    private String m_virtualHost;

    /**
     * Field m_scheme.
     */
    @XmlAttribute(name="scheme")
    private String m_scheme = "http";

    /**
     * Field m_userInfo.
     */
    @XmlAttribute(name="user-info")
    private String m_userInfo;

    /**
     * Field m_host.
     */
    @XmlAttribute(name="host")
    private String m_host = "${ipaddr}";

    /**
     * Field m_requireIPv6.
     */
    @XmlAttribute(name="requireIPv6")
    private Boolean m_requireIPv6;

    /**
     * Field m_requireIPv4.
     */
    @XmlAttribute(name="requireIPv4")
    private Boolean m_requireIPv4;

    /**
     * This element is used to enable or disable SSL host and
     * certificate verification. Default: true (verification is
     * disabled)
     *  
     */
    @XmlAttribute(name="disable-ssl-verification")
    private String m_disableSslVerification = "true";

    /**
     * Field m_port.
     */
    @XmlAttribute(name="port")
    private Integer m_port = 80;

    /**
     * Field m_path.
     */
    @XmlAttribute(name="path")
    private String m_path;

    /**
     * Field m_query.
     */
    @XmlAttribute(name="query")
    private String m_query;

    /**
     * Field m_fragment.
     */
    @XmlAttribute(name="fragment")
    private String m_fragment;

    /**
     * Field m_failureMatch.
     */
    @XmlAttribute(name="failureMatch")
    private String m_failureMatch;

    /**
     * Field m_failureMessage.
     */
    @XmlAttribute(name="failureMessage")
    private String m_failureMessage;

    /**
     * Field m_successMatch.
     */
    @XmlAttribute(name="successMatch")
    private String m_successMatch;

    /**
     * Field m_locationMatch.
     */
    @XmlAttribute(name="locationMatch")
    private String m_locationMatch;

    /**
     * Field m_responseRange.
     */
    @XmlAttribute(name="response-range")
    private String m_responseRange = "100-399";

    /**
     * Field m_dsName.
     */
    @XmlAttribute(name="ds-name")
    private String m_dsName;

    /**
     * Currently only used for HTTP form parameters.
     */
    @XmlElement(name="parameter")
    private List<Parameter> m_parameters = new ArrayList<Parameter>();

    /**
     * Assign the value of a regex match group to a
     *  session variable with a user-defined name. The
     *  match group is identified by number and must
     *  be zero or greater.
     */
    @XmlElement(name="session-variable")
    private List<SessionVariable> m_sessionVariables = new ArrayList<SessionVariable>();


    //----------------/
    //- Constructors -/
    //----------------/

    public Page() {
        super();
        setMethod("GET");
        setHttpVersion("1.1");
        setScheme("http");
        setHost("${ipaddr}");
        setDisableSslVerification("true");
        setResponseRange("100-399");
    }


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(index, parameter);
    }

    /**
     * 
     * 
     * @param sessionVariable
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSessionVariable(final SessionVariable sessionVariable) throws IndexOutOfBoundsException {
        m_sessionVariables.add(sessionVariable);
    }

    /**
     * 
     * 
     * @param index
     * @param sessionVariable
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSessionVariable(final int index, final SessionVariable sessionVariable) throws IndexOutOfBoundsException {
        m_sessionVariables.add(index, sessionVariable);
    }

    /**
     */
    public void deletePort() {
        m_port = null;
    }

    public void deleteRequireIPv4() {
        m_requireIPv4 = null;
    }

    /**
     */
    public void deleteRequireIPv6() {
        m_requireIPv6 = null;
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(m_parameters);
    }

    /**
     * Method enumerateSessionVariable.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<SessionVariable> enumerateSessionVariable() {
        return Collections.enumeration(m_sessionVariables);
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

        if (obj instanceof Page) {
            final Page temp = (Page)obj;
            if (m_method != null) {
                if (temp.m_method == null) {
                    return false;
                } else if (!(m_method.equals(temp.m_method))) {
                    return false;
                }
            } else if (temp.m_method != null) {
                return false;
            }
            if (m_httpVersion != null) {
                if (temp.m_httpVersion == null) {
                    return false;
                } else if (!(m_httpVersion.equals(temp.m_httpVersion))) {
                    return false;
                }
            } else if (temp.m_httpVersion != null) {
                return false;
            }
            if (m_userAgent != null) {
                if (temp.m_userAgent == null) {
                    return false;
                } else if (!(m_userAgent.equals(temp.m_userAgent))) {
                    return false;
                }
            } else if (temp.m_userAgent != null) {
                return false;
            }
            if (m_virtualHost != null) {
                if (temp.m_virtualHost == null) {
                    return false;
                } else if (!(m_virtualHost.equals(temp.m_virtualHost))) {
                    return false;
                }
            } else if (temp.m_virtualHost != null) {
                return false;
            }
            if (m_scheme != null) {
                if (temp.m_scheme == null) {
                    return false;
                } else if (!(m_scheme.equals(temp.m_scheme))) {
                    return false;
                }
            } else if (temp.m_scheme != null) {
                return false;
            }
            if (m_userInfo != null) {
                if (temp.m_userInfo == null) {
                    return false;
                } else if (!(m_userInfo.equals(temp.m_userInfo))) {
                    return false;
                }
            } else if (temp.m_userInfo != null) {
                return false;
            }
            if (m_host != null) {
                if (temp.m_host == null) {
                    return false;
                } else if (!(m_host.equals(temp.m_host))) {
                    return false;
                }
            } else if (temp.m_host != null) {
                return false;
            }
            if (m_requireIPv6 != null) {
                if (temp.m_requireIPv6 == null) {
                    return false;
                } else if (!(m_requireIPv6.equals(temp.m_requireIPv6))) {
                    return false;
                }
            } else if (temp.m_requireIPv6 != null) {
                return false;
            }
            if (m_requireIPv4 != null) {
                if (temp.m_requireIPv4 == null) {
                    return false;
                } else if (!(m_requireIPv4.equals(temp.m_requireIPv4))) {
                    return false;
                }
            } else if (temp.m_requireIPv4 != null) {
                return false;
            }
            if (m_disableSslVerification != null) {
                if (temp.m_disableSslVerification == null) {
                    return false;
                } else if (!(m_disableSslVerification.equals(temp.m_disableSslVerification))) {
                    return false;
                }
            } else if (temp.m_disableSslVerification != null) {
                return false;
            }
            if (m_port != null) {
                if (temp.m_port == null) {
                    return false;
                } else if (!(m_port.equals(temp.m_port))) {
                    return false;
                }
            } else if (temp.m_port != null) {
                return false;
            }
            if (m_path != null) {
                if (temp.m_path == null) {
                    return false;
                } else if (!(m_path.equals(temp.m_path))) {
                    return false;
                }
            } else if (temp.m_path != null) {
                return false;
            }
            if (m_query != null) {
                if (temp.m_query == null) {
                    return false;
                } else if (!(m_query.equals(temp.m_query))) {
                    return false;
                }
            } else if (temp.m_query != null) {
                return false;
            }
            if (m_fragment != null) {
                if (temp.m_fragment == null) {
                    return false;
                } else if (!(m_fragment.equals(temp.m_fragment))) {
                    return false;
                }
            } else if (temp.m_fragment != null) {
                return false;
            }
            if (m_failureMatch != null) {
                if (temp.m_failureMatch == null) {
                    return false;
                } else if (!(m_failureMatch.equals(temp.m_failureMatch))) {
                    return false;
                }
            } else if (temp.m_failureMatch != null) {
                return false;
            }
            if (m_failureMessage != null) {
                if (temp.m_failureMessage == null) {
                    return false;
                } else if (!(m_failureMessage.equals(temp.m_failureMessage))) {
                    return false;
                }
            } else if (temp.m_failureMessage != null) {
                return false;
            }
            if (m_successMatch != null) {
                if (temp.m_successMatch == null) {
                    return false;
                } else if (!(m_successMatch.equals(temp.m_successMatch))) {
                    return false;
                }
            } else if (temp.m_successMatch != null) {
                return false;
            }
            if (m_locationMatch != null) {
                if (temp.m_locationMatch == null) {
                    return false;
                } else if (!(m_locationMatch.equals(temp.m_locationMatch))) {
                    return false;
                }
            } else if (temp.m_locationMatch != null) {
                return false;
            }
            if (m_responseRange != null) {
                if (temp.m_responseRange == null) {
                    return false;
                } else if (!(m_responseRange.equals(temp.m_responseRange))) {
                    return false;
                }
            } else if (temp.m_responseRange != null) {
                return false;
            }
            if (m_dsName != null) {
                if (temp.m_dsName == null) {
                    return false;
                } else if (!(m_dsName.equals(temp.m_dsName))) {
                    return false;
                }
            } else if (temp.m_dsName != null) {
                return false;
            }
            if (m_parameters != null) {
                if (temp.m_parameters == null) {
                    return false;
                } else if (!(m_parameters.equals(temp.m_parameters))) {
                    return false;
                }
            } else if (temp.m_parameters != null) {
                return false;
            }
            if (m_sessionVariables != null) {
                if (temp.m_sessionVariables == null) {
                    return false;
                } else if (!(m_sessionVariables.equals(temp.m_sessionVariables))) {
                    return false;
                }
            } else if (temp.m_sessionVariables != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'disableSslVerification'. The
     * field 'disableSslVerification' has the following
     * description: This element is used to enable or disable SSL
     * host and certificate verification. Default: true
     * (verification is disabled)
     *  
     * 
     * @return the value of field 'DisableSslVerification'.
     */
    public String getDisableSslVerification() {
        return m_disableSslVerification == null? "true" : m_disableSslVerification;
    }

    /**
     * Returns the value of field 'dsName'.
     * 
     * @return the value of field 'DsName'.
     */
    public String getDsName() {
        return m_dsName;
    }

    /**
     * Returns the value of field 'failureMatch'.
     * 
     * @return the value of field 'FailureMatch'.
     */
    public String getFailureMatch() {
        return m_failureMatch;
    }

    /**
     * Returns the value of field 'failureMessage'.
     * 
     * @return the value of field 'FailureMessage'.
     */
    public String getFailureMessage() {
        return m_failureMessage;
    }

    /**
     * Returns the value of field 'fragment'.
     * 
     * @return the value of field 'Fragment'.
     */
    public String getFragment() {
        return m_fragment;
    }

    /**
     * Returns the value of field 'host'.
     * 
     * @return the value of field 'Host'.
     */
    public String getHost() {
        return m_host == null? "${ipaddr}" : m_host;
    }

    /**
     * Returns the value of field 'httpVersion'.
     * 
     * @return the value of field 'HttpVersion'.
     */
    public String getHttpVersion() {
        return m_httpVersion == null? "1.1" : m_httpVersion;
    }

    /**
     * Returns the value of field 'locationMatch'.
     * 
     * @return the value of field 'LocationMatch'.
     */
    public String getLocationMatch() {
        return m_locationMatch;
    }

    /**
     * Returns the value of field 'method'.
     * 
     * @return the value of field 'Method'.
     */
    public String getMethod() {
        return m_method == null? "GET" : m_method;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Parameter at the
     * given index
     */
    public Parameter getParameter(final int index) throws IndexOutOfBoundsException {
        return m_parameters.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Parameter[] getParameter() {
        return m_parameters.toArray(EMPTY_PARAMETER_LIST);
    }

    /**
     * Method getParameterCollection.Returns a reference to
     * 'm_parameters'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return new ArrayList<Parameter>(m_parameters);
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return m_parameters.size();
    }

    /**
     * Returns the value of field 'path'.
     * 
     * @return the value of field 'Path'.
     */
    public String getPath() {
        return m_path;
    }

    /**
     * Returns the value of field 'port'.
     * 
     * @return the value of field 'Port'.
     */
    public Integer getPort() {
        return m_port == null? 80 : m_port;
    }

    /**
     * Returns the value of field 'query'.
     * 
     * @return the value of field 'Query'.
     */
    public String getQuery() {
        return m_query;
    }

    /**
     * Returns the value of field 'requireIPv4'.
     * 
     * @return the value of field 'RequireIPv4'.
     */
    public Boolean getRequireIPv4() {
        return m_requireIPv4 == null? false : m_requireIPv4;
    }

    /**
     * Returns the value of field 'requireIPv6'.
     * 
     * @return the value of field 'RequireIPv6'.
     */
    public Boolean getRequireIPv6() {
        return m_requireIPv6 == null? false : m_requireIPv6;
    }

    /**
     * Returns the value of field 'responseRange'.
     * 
     * @return the value of field 'ResponseRange'.
     */
    public String getResponseRange() {
        return m_responseRange == null? "100-399" : m_responseRange;
    }

    /**
     * Returns the value of field 'scheme'.
     * 
     * @return the value of field 'Scheme'.
     */
    public String getScheme() {
        return m_scheme == null? "http" : m_scheme;
    }

    /**
     * Method getSessionVariable.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * SessionVariable at
     * the given index
     */
    public SessionVariable getSessionVariable(final int index) throws IndexOutOfBoundsException {
        return m_sessionVariables.get(index);
    }

    /**
     * Method getSessionVariable.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public SessionVariable[] getSessionVariable() {
        return m_sessionVariables.toArray(EMPTY_SESSION_VARIABLE_LIST);
    }

    /**
     * Method getSessionVariableCollection.Returns a reference to
     * 'm_sessionVariables'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<SessionVariable> getSessionVariableCollection() {
        return new ArrayList<SessionVariable>(m_sessionVariables);
    }

    /**
     * Method getSessionVariableCount.
     * 
     * @return the size of this collection
     */
    public int getSessionVariableCount() {
        return m_sessionVariables.size();
    }

    /**
     * Returns the value of field 'successMatch'.
     * 
     * @return the value of field 'SuccessMatch'.
     */
    public String getSuccessMatch() {
        return m_successMatch;
    }

    /**
     * Returns the value of field 'userAgent'.
     * 
     * @return the value of field 'UserAgent'.
     */
    public String getUserAgent() {
        return m_userAgent;
    }

    /**
     * Returns the value of field 'userInfo'.
     * 
     * @return the value of field 'UserInfo'.
     */
    public String getUserInfo() {
        return m_userInfo;
    }

    /**
     * Returns the value of field 'virtualHost'.
     * 
     * @return the value of field 'VirtualHost'.
     */
    public String getVirtualHost(
            ) {
        return m_virtualHost;
    }

    /**
     * Method hasPort.
     * 
     * @return true if at least one Port has been added
     */
    public boolean hasPort() {
        return m_port != null;
    }

    /**
     * Method hasRequireIPv4.
     * 
     * @return true if at least one RequireIPv4 has been added
     */
    public boolean hasRequireIPv4() {
        return m_requireIPv4 != null;
    }

    /**
     * Method hasRequireIPv6.
     * 
     * @return true if at least one RequireIPv6 has been added
     */
    public boolean hasRequireIPv6() {
        return m_requireIPv6 != null;
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

        if (m_method != null) {
            result = 37 * result + m_method.hashCode();
        }
        if (m_httpVersion != null) {
            result = 37 * result + m_httpVersion.hashCode();
        }
        if (m_userAgent != null) {
            result = 37 * result + m_userAgent.hashCode();
        }
        if (m_virtualHost != null) {
            result = 37 * result + m_virtualHost.hashCode();
        }
        if (m_scheme != null) {
            result = 37 * result + m_scheme.hashCode();
        }
        if (m_userInfo != null) {
            result = 37 * result + m_userInfo.hashCode();
        }
        if (m_host != null) {
            result = 37 * result + m_host.hashCode();
        }
        if (m_requireIPv6 != null) {
            result = 37 * result + m_requireIPv6.hashCode();
        }
        if (m_requireIPv4 != null) {
            result = 37 * result + m_requireIPv4.hashCode();
        }
        if (m_disableSslVerification != null) {
            result = 37 * result + m_disableSslVerification.hashCode();
        }
        if (m_port != null) {
            result = 37 * result + m_port.hashCode();
        }
        if (m_path != null) {
            result = 37 * result + m_path.hashCode();
        }
        if (m_query != null) {
            result = 37 * result + m_query.hashCode();
        }
        if (m_fragment != null) {
            result = 37 * result + m_fragment.hashCode();
        }
        if (m_failureMatch != null) {
            result = 37 * result + m_failureMatch.hashCode();
        }
        if (m_failureMessage != null) {
            result = 37 * result + m_failureMessage.hashCode();
        }
        if (m_successMatch != null) {
            result = 37 * result + m_successMatch.hashCode();
        }
        if (m_locationMatch != null) {
            result = 37 * result + m_locationMatch.hashCode();
        }
        if (m_responseRange != null) {
            result = 37 * result + m_responseRange.hashCode();
        }
        if (m_dsName != null) {
            result = 37 * result + m_dsName.hashCode();
        }
        if (m_parameters != null) {
            result = 37 * result + m_parameters.hashCode();
        }
        if (m_sessionVariables != null) {
            result = 37 * result + m_sessionVariables.hashCode();
        }

        return result;
    }

    /**
     * Returns the value of field 'requireIPv4'.
     * 
     * @return the value of field 'RequireIPv4'.
     */
    public boolean isRequireIPv4() {
        return m_requireIPv4 == null? false : m_requireIPv4;
    }

    /**
     * Returns the value of field 'requireIPv6'.
     * 
     * @return the value of field 'RequireIPv6'.
     */
    public boolean isRequireIPv6() {
        return m_requireIPv6 == null? false : m_requireIPv6;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Parameter> iterateParameter() {
        return m_parameters.iterator();
    }

    /**
     * Method iterateSessionVariable.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<SessionVariable> iterateSessionVariable() {
        return m_sessionVariables.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllParameter() {
        m_parameters.clear();
    }

    /**
     */
    public void removeAllSessionVariable() {
        m_sessionVariables.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param parameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        return m_parameters.remove(index);
    }

    /**
     * Method removeSessionVariable.
     * 
     * @param sessionVariable
     * @return true if the object was removed from the collection.
     */
    public boolean removeSessionVariable(final SessionVariable sessionVariable) {
        return m_sessionVariables.remove(sessionVariable);
    }

    /**
     * Method removeSessionVariableAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public SessionVariable removeSessionVariableAt(final int index) {
        return m_sessionVariables.remove(index);
    }

    /**
     * Sets the value of field 'disableSslVerification'. The field
     * 'disableSslVerification' has the following description: This
     * element is used to enable or disable SSL host and
     * certificate verification. Default: true (verification is
     * disabled)
     *  
     * 
     * @param disableSslVerification the value of field
     * 'disableSslVerification'.
     */
    public void setDisableSslVerification(final String disableSslVerification) {
        m_disableSslVerification = disableSslVerification;
    }

    /**
     * Sets the value of field 'dsName'.
     * 
     * @param dsName the value of field 'dsName'.
     */
    public void setDsName(final String dsName) {
        m_dsName = dsName;
    }

    /**
     * Sets the value of field 'failureMatch'.
     * 
     * @param failureMatch the value of field 'failureMatch'.
     */
    public void setFailureMatch(final String failureMatch) {
        m_failureMatch = failureMatch;
    }

    /**
     * Sets the value of field 'failureMessage'.
     * 
     * @param failureMessage the value of field 'failureMessage'.
     */
    public void setFailureMessage(final String failureMessage) {
        m_failureMessage = failureMessage;
    }

    /**
     * Sets the value of field 'fragment'.
     * 
     * @param fragment the value of field 'fragment'.
     */
    public void setFragment(final String fragment) {
        m_fragment = fragment;
    }

    /**
     * Sets the value of field 'host'.
     * 
     * @param host the value of field 'host'.
     */
    public void setHost(final String host) {
        m_host = host;
    }

    /**
     * Sets the value of field 'httpVersion'.
     * 
     * @param httpVersion the value of field 'httpVersion'.
     */
    public void setHttpVersion(final String httpVersion) {
        m_httpVersion = httpVersion;
    }

    /**
     * Sets the value of field 'locationMatch'.
     * 
     * @param locationMatch the value of field 'locationMatch'.
     */
    public void setLocationMatch(final String locationMatch) {
        m_locationMatch = locationMatch;
    }

    /**
     * Sets the value of field 'method'.
     * 
     * @param method the value of field 'method'.
     */
    public void setMethod(final String method) {
        m_method = method;
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.set(index, parameter);
    }

    /**
     * 
     * 
     * @param parameters
     */
    public void setParameter(final Parameter[] parameters) {
        m_parameters.clear();
        for (final Parameter parameter : parameters) {
            m_parameters.add(parameter);
        }
    }

    /**
     * Sets the value of 'm_parameters' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param parameters the Vector to copy.
     */
    public void setParameter(final List<Parameter> parameters) {
        if (parameters != m_parameters) {
            m_parameters.clear();
            m_parameters.addAll(parameters);
        }
    }

    /**
     * Sets the value of 'm_parameters' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param parameters the Vector to set.
     */
    public void setParameterCollection(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    /**
     * Sets the value of field 'path'.
     * 
     * @param path the value of field 'path'.
     */
    public void setPath(final String path) {
        m_path = path;
    }

    /**
     * Sets the value of field 'port'.
     * 
     * @param port the value of field 'port'.
     */
    public void setPort(final Integer port) {
        m_port = port;
    }

    /**
     * Sets the value of field 'query'.
     * 
     * @param query the value of field 'query'.
     */
    public void setQuery(final String query) {
        m_query = query;
    }

    /**
     * Sets the value of field 'requireIPv4'.
     * 
     * @param requireIPv4 the value of field 'requireIPv4'.
     */
    public void setRequireIPv4(final Boolean requireIPv4) {
        m_requireIPv4 = requireIPv4;
    }

    /**
     * Sets the value of field 'requireIPv6'.
     * 
     * @param requireIPv6 the value of field 'requireIPv6'.
     */
    public void setRequireIPv6(final Boolean requireIPv6) {
        m_requireIPv6 = requireIPv6;
    }

    /**
     * Sets the value of field 'responseRange'.
     * 
     * @param responseRange the value of field 'responseRange'.
     */
    public void setResponseRange(final String responseRange) {
        m_responseRange = responseRange;
    }

    /**
     * Sets the value of field 'scheme'.
     * 
     * @param scheme the value of field 'scheme'.
     */
    public void setScheme(final String scheme) {
        m_scheme = scheme;
    }

    /**
     * 
     * 
     * @param index
     * @param sessionVariable
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSessionVariable(final int index, final SessionVariable sessionVariable) throws IndexOutOfBoundsException {
        m_sessionVariables.set(index, sessionVariable);
    }

    /**
     * 
     * 
     * @param sessionVariables
     */
    public void setSessionVariable(final SessionVariable[] sessionVariables) {
        m_sessionVariables.clear();
        for (final SessionVariable sessionVariable : sessionVariables) {
            m_sessionVariables.add(sessionVariable);
        }
    }

    /**
     * Sets the value of 'm_sessionVariables' by copying the
     * given Vector. All elements will be checked for type safety.
     * 
     * @param sessionVariables the Vector to copy.
     */
    public void setSessionVariable(final List<SessionVariable> sessionVariables) {
        if (sessionVariables != m_sessionVariables) {
            m_sessionVariables.clear();
            m_sessionVariables.addAll(sessionVariables);
        }
    }

    /**
     * Sets the value of 'm_sessionVariables' by setting it to
     * the given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param sessionVariables the Vector to set.
     */
    public void setSessionVariableCollection(final List<SessionVariable> sessionVariables) {
        m_sessionVariables = new ArrayList<SessionVariable>(sessionVariables);
    }

    /**
     * Sets the value of field 'successMatch'.
     * 
     * @param successMatch the value of field 'successMatch'.
     */
    public void setSuccessMatch(final String successMatch) {
        m_successMatch = successMatch;
    }

    /**
     * Sets the value of field 'userAgent'.
     * 
     * @param userAgent the value of field 'userAgent'.
     */
    public void setUserAgent(final String userAgent) {
        m_userAgent = userAgent;
    }

    /**
     * Sets the value of field 'userInfo'.
     * 
     * @param userInfo the value of field 'userInfo'.
     */
    public void setUserInfo(final String userInfo) {
        m_userInfo = userInfo;
    }

    /**
     * Sets the value of field 'virtualHost'.
     * 
     * @param virtualHost the value of field 'virtualHost'.
     */
    public void setVirtualHost(final String virtualHost) {
        m_virtualHost = virtualHost;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Page
     */
    public static Page unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Page) Unmarshaller.unmarshal(Page.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
        return "Page[method=" + m_method +
                ",httpVersion=" + m_httpVersion +
                ",userAgent=" + m_userAgent +
                ",virtualHost=" + m_virtualHost +
                ",scheme=" + m_scheme +
                ",userInfo=" + m_userInfo +
                ",host=" + m_host +
                ",requireIPv4=" + m_requireIPv4 +
                ",requireIPv6=" + m_requireIPv6 +
                ",disableSslVerification=" + m_disableSslVerification +
                ",port=" + m_port +
                ",path=" + m_path +
                ",query=" + m_query +
                ",fragment=" + m_fragment +
                ",failureMatch=" + m_failureMatch +
                ",failureMessage=" + m_failureMessage +
                ",successMatch=" + m_successMatch +
                ",locationMatch=" + m_locationMatch +
                ",responseRange=" + m_responseRange +
                ",dsName=" + m_dsName +
                ",parameters=" + m_parameters +
                ",sessionVariables=" + m_sessionVariables +
                "]";
    }
}
