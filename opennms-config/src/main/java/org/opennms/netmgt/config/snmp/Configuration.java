/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

import java.io.Reader;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Class Configuration.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration implements Serializable {
	private static final long serialVersionUID = -6800972339377512259L;

	/**
	 * If set, overrides UDP port 161 as the port where SNMP GET/GETNEXT/GETBULK
	 * requests are sent.
	 */
	@XmlAttribute(name="port")
	private Integer _port;

	/**
	 * Default number of retries
	 */
	@XmlAttribute(name="retry")
	private Integer _retry;

	/**
	 * Default timeout (in milliseconds)
	 */
	@XmlAttribute(name="timeout")
	private Integer _timeout;

	/**
	 * Default read community string
	 */
	@XmlAttribute(name="read-community")
	private String _readCommunity;

	/**
	 * Default write community string
	 */
	@XmlAttribute(name="write-community")
	private String _writeCommunity;

	/**
	 * The proxy host to use when communiciating with this agent
	 */
	@XmlAttribute(name="proxy-host")
	private String _proxyHost;

	/**
	 * If set, forces SNMP data collection to the specified version.
	 */
	@XmlAttribute(name="version")
	private String _version;

	/**
	 * Number of variables to send per SNMP request.
	 * 
	 */
	@XmlAttribute(name="max-vars-per-pdu")
	private Integer _maxVarsPerPdu = 10;

	/**
	 * Number of repetitions to send per get-bulk request.
	 * 
	 */
	@XmlAttribute(name="max-repititions")
	private Integer _maxRepetitions = 2;

	/**
	 * (SNMP4J specific) Specifies the maximum number of bytes that may be
	 * encoded into an individual SNMP PDU request by Collectd. Provides a means
	 * to limit the size of outgoing PDU requests. Default is 65535, must be at
	 * least 484.
	 */
	private Integer _maxRequestSize = 65535;

	/**
	 * SNMPv3
	 */
	private String _securityName;

	/**
	 * SNMPv3
	 */
	private Integer _securityLevel;

	/**
	 * SNMPv3
	 */
	private String _authPassphrase;

	/**
	 * SNMPv3
	 */
	private String _authProtocol;

	/**
	 * SNMPv3
	 */
	private String _engineId;

	/**
	 * SNMPv3
	 */
	private String _contextEngineId;

	/**
	 * SNMPv3
	 */
	private String _contextName;

	/**
	 * SNMPv3
	 */
	private String _privacyPassphrase;

	/**
	 * SNMPv3
	 */
	private String _privacyProtocol;

	/**
	 * SNMPv3
	 */
	private String _enterpriseId;

	public Configuration() {
		super();
	}

	public void deleteMaxRepetitions() {
		_maxRepetitions = null;
	}

	public void deleteMaxRequestSize() {
		_maxRequestSize = null;
	}

	public void deleteMaxVarsPerPdu() {
		_maxVarsPerPdu = null;
	}

	public void deletePort() {
		_port = null;
	}

	public void deleteRetry() {
		_retry = null;
	}

	public void deleteSecurityLevel() {
		_securityLevel = null;
	}

	public void deleteTimeout() {
		_timeout = null;
	}

	/**
	 * Overrides the Object.equals method.
	 * 
	 * @param obj
	 * @return true if the objects are equal.
	 */
	@Override()
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (obj instanceof Configuration) {

			final Configuration temp = (Configuration) obj;
			if (_port != temp._port)
				return false;
			if (_retry != temp._retry)
				return false;
			if (_timeout != temp._timeout)
				return false;
			if (_readCommunity != null) {
				if (temp._readCommunity == null)
					return false;
				else if (!(_readCommunity.equals(temp._readCommunity))) {
					return false;
				}
			} else if (temp._readCommunity != null) {
				return false;
			}
			if (_writeCommunity != null) {
				if (temp._writeCommunity == null)
					return false;
				else if (!(_writeCommunity.equals(temp._writeCommunity))) {
					return false;
				}
			} else if (temp._writeCommunity != null) {
				return false;
			}
			if (_proxyHost != null) {
				if (temp._proxyHost == null) {
					return false;
				} else if (!(_proxyHost.equals(temp._proxyHost))) {
					return false;
				}
			} else if (temp._proxyHost != null) {
				return false;
			}
			if (_version != null) {
				if (temp._version == null) {
					return false;
				} else if (!(_version.equals(temp._version))) {
					return false;
				}
			} else if (temp._version != null) {
				return false;
			}
			if (_maxVarsPerPdu != temp._maxVarsPerPdu) {
				return false;
			}
			if (_maxRepetitions != temp._maxRepetitions) {
				return false;
			}
			if (_maxRequestSize != temp._maxRequestSize) {
				return false;
			}
			if (_securityName != null) {
				if (temp._securityName == null) {
					return false;
				} else if (!(_securityName.equals(temp._securityName))) {
					return false;
				}
			} else if (temp._securityName != null) {
				return false;
			}
			if (_securityLevel != temp._securityLevel) {
				return false;
			}
			if (_authPassphrase != null) {
				if (temp._authPassphrase == null) {
					return false;
				} else if (!(_authPassphrase.equals(temp._authPassphrase))) {
					return false;
				}
			} else if (temp._authPassphrase != null) {
				return false;
			}
			if (_authProtocol != null) {
				if (temp._authProtocol == null) {
					return false;
				} else if (!(_authProtocol.equals(temp._authProtocol))) {
					return false;
				}
			} else if (temp._authProtocol != null) {
				return false;
			}
			if (_engineId != null) {
				if (temp._engineId == null) {
					return false;
				} else if (!(_engineId.equals(temp._engineId))) {
					return false;
				}
			} else if (temp._engineId != null) {
				return false;
			}
			if (_contextEngineId != null) {
				if (temp._contextEngineId == null) {
					return false;
				} else if (!(_contextEngineId.equals(temp._contextEngineId))) {
					return false;
				}
			} else if (temp._contextEngineId != null) {
				return false;
			}
			if (_contextName != null) {
				if (temp._contextName == null) {
					return false;
				} else if (!(_contextName.equals(temp._contextName))) {
					return false;
				}
			} else if (temp._contextName != null) {
				return false;
			}
			if (_privacyPassphrase != null) {
				if (temp._privacyPassphrase == null) {
					return false;
				} else if (!(_privacyPassphrase.equals(temp._privacyPassphrase))) {
					return false;
				}
			} else if (temp._privacyPassphrase != null) {
				return false;
			}
			if (_privacyProtocol != null) {
				if (temp._privacyProtocol == null) {
					return false;
				} else if (!(_privacyProtocol.equals(temp._privacyProtocol))) {
					return false;
				}
			} else if (temp._privacyProtocol != null) {
				return false;
			}
			if (_enterpriseId != null) {
				if (temp._enterpriseId == null) {
					return false;
				} else if (!(_enterpriseId.equals(temp._enterpriseId))) {
					return false;
				}
			} else if (temp._enterpriseId != null) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns the value of field 'authPassphrase'. The field 'authPassphrase'
	 * has the following description: SNMPv3
	 * 
	 * @return the value of field 'AuthPassphrase'.
	 */
	public String getAuthPassphrase() {
		return _authPassphrase;
	}

	/**
	 * Returns the value of field 'authProtocol'. The field 'authProtocol' has
	 * the following description: SNMPv3
	 * 
	 * @return the value of field 'AuthProtocol'.
	 */
	public String getAuthProtocol() {
		return _authProtocol;
	}

	/**
	 * Returns the value of field 'contextEngineId'. The field 'contextEngineId'
	 * has the following description: SNMPv3
	 * 
	 * @return the value of field 'ContextEngineId'.
	 */
	public String getContextEngineId() {
		return _contextEngineId;
	}

	/**
	 * Returns the value of field 'contextName'. The field 'contextName' has the
	 * following description: SNMPv3
	 * 
	 * @return the value of field 'ContextName'.
	 */
	public String getContextName() {
		return _contextName;
	}

	/**
	 * Returns the value of field 'engineId'. The field 'engineId' has the
	 * following description: SNMPv3
	 * 
	 * @return the value of field 'EngineId'.
	 */
	public String getEngineId() {
		return _engineId;
	}

	/**
	 * Returns the value of field 'enterpriseId'. The field 'enterpriseId' has
	 * the following description: SNMPv3
	 * 
	 * @return the value of field 'EnterpriseId'.
	 */
	public String getEnterpriseId() {
		return _enterpriseId;
	}

	/**
	 * Returns the value of field 'maxRepetitions'. The field 'maxRepetitions'
	 * has the following description: Number of repetitions to send per get-bulk
	 * request.
	 * 
	 * 
	 * @return the value of field 'MaxRepetitions'.
	 */
	public Integer getMaxRepetitions() {
		return _maxRepetitions == null? 0 : _maxRepetitions;
	}

	/**
	 * Returns the value of field 'maxRequestSize'. The field 'maxRequestSize'
	 * has the following description: (SNMP4J specific) Specifies the maximum
	 * number of bytes that may be encoded into an individual SNMP PDU request
	 * by Collectd. Provides a means to limit the size of outgoing PDU requests.
	 * Default is 65535, must be at least 484.
	 * 
	 * @return the value of field 'MaxRequestSize'.
	 */
	public Integer getMaxRequestSize() {
		return _maxRequestSize == null? 0 : _maxRequestSize;
	}

	/**
	 * Returns the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
	 * the following description: Number of variables to send per SNMP request.
	 * 
	 * 
	 * @return the value of field 'MaxVarsPerPdu'.
	 */
	public Integer getMaxVarsPerPdu() {
		return _maxVarsPerPdu == null? 0 : _maxVarsPerPdu;
	}

	/**
	 * Returns the value of field 'port'. The field 'port' has the following
	 * description: If set, overrides UDP port 161 as the port where SNMP
	 * GET/GETNEXT/GETBULK requests are sent.
	 * 
	 * @return the value of field 'Port'.
	 */
	public Integer getPort() {
		return _port == null? 0 : _port;
	}

	/**
	 * Returns the value of field 'privacyPassphrase'. The field
	 * 'privacyPassphrase' has the following description: SNMPv3
	 * 
	 * @return the value of field 'PrivacyPassphrase'.
	 */
	public String getPrivacyPassphrase() {
		return _privacyPassphrase;
	}

	/**
	 * Returns the value of field 'privacyProtocol'. The field 'privacyProtocol'
	 * has the following description: SNMPv3
	 * 
	 * @return the value of field 'PrivacyProtocol'.
	 */
	public String getPrivacyProtocol() {
		return _privacyProtocol;
	}

	/**
	 * Returns the value of field 'proxyHost'. The field 'proxyHost' has the
	 * following description: The proxy host to use when communiciating with
	 * this agent
	 * 
	 * @return the value of field 'ProxyHost'.
	 */
	public String getProxyHost() {
		return _proxyHost;
	}

	/**
	 * Returns the value of field 'readCommunity'. The field 'readCommunity' has
	 * the following description: Default read community string
	 * 
	 * @return the value of field 'ReadCommunity'.
	 */
	public String getReadCommunity() {
		return _readCommunity;
	}

	/**
	 * Returns the value of field 'retry'. The field 'retry' has the following
	 * description: Default number of retries
	 * 
	 * @return the value of field 'Retry'.
	 */
	public Integer getRetry() {
		return _retry == null? 0 : _retry;
	}

	/**
	 * Returns the value of field 'securityLevel'. The field 'securityLevel' has
	 * the following description: SNMPv3
	 * 
	 * @return the value of field 'SecurityLevel'.
	 */
	public Integer getSecurityLevel() {
		return _securityLevel == null? 0 : _securityLevel;
	}

	/**
	 * Returns the value of field 'securityName'. The field 'securityName' has
	 * the following description: SNMPv3
	 * 
	 * @return the value of field 'SecurityName'.
	 */
	public String getSecurityName() {
		return _securityName;
	}

	/**
	 * Returns the value of field 'timeout'. The field 'timeout' has the
	 * following description: Default timeout (in milliseconds)
	 * 
	 * @return the value of field 'Timeout'.
	 */
	public Integer getTimeout() {
		return _timeout == null? 0 : _timeout;
	}

	/**
	 * Returns the value of field 'version'. The field 'version' has the
	 * following description: If set, forces SNMP data collection to the
	 * specified version.
	 * 
	 * @return the value of field 'Version'.
	 */
	public String getVersion() {
		return _version;
	}

	/**
	 * Returns the value of field 'writeCommunity'. The field 'writeCommunity'
	 * has the following description: Default write community string
	 * 
	 * @return the value of field 'WriteCommunity'.
	 */
	public String getWriteCommunity() {
		return _writeCommunity;
	}

	/**
	 * Method hasMaxRepetitions.
	 * 
	 * @return true if at least one MaxRepetitions has been added
	 */
	public boolean hasMaxRepetitions() {
		return _maxRepetitions != null;
	}

	/**
	 * Method hasMaxRequestSize.
	 * 
	 * @return true if at least one MaxRequestSize has been added
	 */
	public boolean hasMaxRequestSize() {
		return _maxRequestSize != null;
	}

	/**
	 * Method hasMaxVarsPerPdu.
	 * 
	 * @return true if at least one MaxVarsPerPdu has been added
	 */
	public boolean hasMaxVarsPerPdu() {
		return _maxVarsPerPdu != null;
	}

	/**
	 * Method hasPort.
	 * 
	 * @return true if at least one Port has been added
	 */
	public boolean hasPort() {
		return _port != null;
	}

	/**
	 * Method hasRetry.
	 * 
	 * @return true if at least one Retry has been added
	 */
	public boolean hasRetry() {
		return _retry != null;
	}

	/**
	 * Method hasSecurityLevel.
	 * 
	 * @return true if at least one SecurityLevel has been added
	 */
	public boolean hasSecurityLevel() {
		return _securityLevel != null;
	}

	/**
	 * Method hasTimeout.
	 * 
	 * @return true if at least one Timeout has been added
	 */
	public boolean hasTimeout() {
		return _timeout != null;
	}

	/**
	 * Overrides the Object.hashCode method.
	 * <p>
	 * The following steps came from <b>Effective Java Programming Language
	 * Guide</b> by Joshua Bloch, Chapter 3
	 * 
	 * @return a hash code value for the object.
	 */
	public int hashCode() {
		int result = 17;

		result = 37 * result + _port;
		result = 37 * result + _retry;
		result = 37 * result + _timeout;
		if (_readCommunity != null) {
			result = 37 * result + _readCommunity.hashCode();
		}
		if (_writeCommunity != null) {
			result = 37 * result + _writeCommunity.hashCode();
		}
		if (_proxyHost != null) {
			result = 37 * result + _proxyHost.hashCode();
		}
		if (_version != null) {
			result = 37 * result + _version.hashCode();
		}
		result = 37 * result + _maxVarsPerPdu;
		result = 37 * result + _maxRepetitions;
		result = 37 * result + _maxRequestSize;
		if (_securityName != null) {
			result = 37 * result + _securityName.hashCode();
		}
		result = 37 * result + _securityLevel;
		if (_authPassphrase != null) {
			result = 37 * result + _authPassphrase.hashCode();
		}
		if (_authProtocol != null) {
			result = 37 * result + _authProtocol.hashCode();
		}
		if (_engineId != null) {
			result = 37 * result + _engineId.hashCode();
		}
		if (_contextEngineId != null) {
			result = 37 * result + _contextEngineId.hashCode();
		}
		if (_contextName != null) {
			result = 37 * result + _contextName.hashCode();
		}
		if (_privacyPassphrase != null) {
			result = 37 * result + _privacyPassphrase.hashCode();
		}
		if (_privacyProtocol != null) {
			result = 37 * result + _privacyProtocol.hashCode();
		}
		if (_enterpriseId != null) {
			result = 37 * result + _enterpriseId.hashCode();
		}

		return result;
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
	 * 
	 * 
	 * @param out
	 * @throws MarshalException
	 *             if object is null or if any SAXException is thrown during
	 *             marshaling
	 * @throws ValidationException
	 *             if this object is an invalid instance according to the schema
	 */
	public void marshal(final java.io.Writer out) throws MarshalException, ValidationException {
		Marshaller.marshal(this, out);
	}

	/**
	 * 
	 * 
	 * @param handler
	 * @throws java.io.IOException
	 *             if an IOException occurs during marshaling
	 * @throws ValidationException
	 *             if this object is an invalid instance according to the schema
	 * @throws MarshalException
	 *             if object is null or if any SAXException is thrown during
	 *             marshaling
	 */
	public void marshal(final ContentHandler handler) throws java.io.IOException, MarshalException, ValidationException {
		Marshaller.marshal(this, handler);
	}

	/**
	 * Sets the value of field 'authPassphrase'. The field 'authPassphrase' has
	 * the following description: SNMPv3
	 * 
	 * @param authPassphrase
	 *            the value of field 'authPassphrase'.
	 */
	public void setAuthPassphrase(final String authPassphrase) {
		_authPassphrase = authPassphrase;
	}

	/**
	 * Sets the value of field 'authProtocol'. The field 'authProtocol' has the
	 * following description: SNMPv3
	 * 
	 * @param authProtocol
	 *            the value of field 'authProtocol'.
	 */
	public void setAuthProtocol(final String authProtocol) {
		_authProtocol = authProtocol;
	}

	/**
	 * Sets the value of field 'contextEngineId'. The field 'contextEngineId'
	 * has the following description: SNMPv3
	 * 
	 * @param contextEngineId
	 *            the value of field 'contextEngineId'.
	 */
	public void setContextEngineId(final String contextEngineId) {
		_contextEngineId = contextEngineId;
	}

	/**
	 * Sets the value of field 'contextName'. The field 'contextName' has the
	 * following description: SNMPv3
	 * 
	 * @param contextName
	 *            the value of field 'contextName'.
	 */
	public void setContextName(final String contextName) {
		_contextName = contextName;
	}

	/**
	 * Sets the value of field 'engineId'. The field 'engineId' has the
	 * following description: SNMPv3
	 * 
	 * @param engineId
	 *            the value of field 'engineId'.
	 */
	public void setEngineId(final String engineId) {
		_engineId = engineId;
	}

	/**
	 * Sets the value of field 'enterpriseId'. The field 'enterpriseId' has the
	 * following description: SNMPv3
	 * 
	 * @param enterpriseId
	 *            the value of field 'enterpriseId'.
	 */
	public void setEnterpriseId(final String enterpriseId) {
		_enterpriseId = enterpriseId;
	}

	/**
	 * Sets the value of field 'maxRepetitions'. The field 'maxRepetitions' has
	 * the following description: Number of repetitions to send per get-bulk
	 * request.
	 * 
	 * 
	 * @param maxRepetitions
	 *            the value of field 'maxRepetitions'.
	 */
	public void setMaxRepetitions(final Integer maxRepetitions) {
		_maxRepetitions = maxRepetitions;
	}

	/**
	 * Sets the value of field 'maxRequestSize'. The field 'maxRequestSize' has
	 * the following description: (SNMP4J specific) Specifies the maximum number
	 * of bytes that may be encoded into an individual SNMP PDU request by
	 * Collectd. Provides a means to limit the size of outgoing PDU requests.
	 * Default is 65535, must be at least 484.
	 * 
	 * @param maxRequestSize
	 *            the value of field 'maxRequestSize'.
	 */
	public void setMaxRequestSize(final Integer maxRequestSize) {
		_maxRequestSize = maxRequestSize;
	}

	/**
	 * Sets the value of field 'maxVarsPerPdu'. The field 'maxVarsPerPdu' has
	 * the following description: Number of variables to send per SNMP request.
	 * 
	 * 
	 * @param maxVarsPerPdu
	 *            the value of field 'maxVarsPerPdu'.
	 */
	public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
		_maxVarsPerPdu = maxVarsPerPdu;
	}

	/**
	 * Sets the value of field 'port'. The field 'port' has the following
	 * description: If set, overrides UDP port 161 as the port where SNMP
	 * GET/GETNEXT/GETBULK requests are sent.
	 * 
	 * @param port
	 *            the value of field 'port'.
	 */
	public void setPort(final Integer port) {
		_port = port;
	}

	/**
	 * Sets the value of field 'privacyPassphrase'. The field
	 * 'privacyPassphrase' has the following description: SNMPv3
	 * 
	 * @param privacyPassphrase
	 *            the value of field 'privacyPassphrase'.
	 */
	public void setPrivacyPassphrase(final String privacyPassphrase) {
		_privacyPassphrase = privacyPassphrase;
	}

	/**
	 * Sets the value of field 'privacyProtocol'. The field 'privacyProtocol'
	 * has the following description: SNMPv3
	 * 
	 * @param privacyProtocol
	 *            the value of field 'privacyProtocol'.
	 */
	public void setPrivacyProtocol(final String privacyProtocol) {
		_privacyProtocol = privacyProtocol;
	}

	/**
	 * Sets the value of field 'proxyHost'. The field 'proxyHost' has the
	 * following description: The proxy host to use when communiciating with
	 * this agent
	 * 
	 * @param proxyHost
	 *            the value of field 'proxyHost'.
	 */
	public void setProxyHost(final String proxyHost) {
		_proxyHost = proxyHost;
	}

	/**
	 * Sets the value of field 'readCommunity'. The field 'readCommunity' has
	 * the following description: Default read community string
	 * 
	 * @param readCommunity
	 *            the value of field 'readCommunity'.
	 */
	public void setReadCommunity(final String readCommunity) {
		_readCommunity = readCommunity;
	}

	/**
	 * Sets the value of field 'retry'. The field 'retry' has the following
	 * description: Default number of retries
	 * 
	 * @param retry
	 *            the value of field 'retry'.
	 */
	public void setRetry(final Integer retry) {
		_retry = retry;
	}

	/**
	 * Sets the value of field 'securityLevel'. The field 'securityLevel' has
	 * the following description: SNMPv3
	 * 
	 * @param securityLevel
	 *            the value of field 'securityLevel'.
	 */
	public void setSecurityLevel(final Integer securityLevel) {
		_securityLevel = securityLevel;
	}

	/**
	 * Sets the value of field 'securityName'. The field 'securityName' has the
	 * following description: SNMPv3
	 * 
	 * @param securityName
	 *            the value of field 'securityName'.
	 */
	public void setSecurityName(final String securityName) {
		_securityName = securityName;
	}

	/**
	 * Sets the value of field 'timeout'. The field 'timeout' has the following
	 * description: Default timeout (in milliseconds)
	 * 
	 * @param timeout
	 *            the value of field 'timeout'.
	 */
	public void setTimeout(final Integer timeout) {
		_timeout = timeout;
	}

	/**
	 * Sets the value of field 'version'. The field 'version' has the following
	 * description: If set, forces SNMP data collection to the specified
	 * version.
	 * 
	 * @param version
	 *            the value of field 'version'.
	 */
	public void setVersion(final String version) {
		_version = version;
	}

	/**
	 * Sets the value of field 'writeCommunity'. The field 'writeCommunity' has
	 * the following description: Default write community string
	 * 
	 * @param writeCommunity
	 *            the value of field 'writeCommunity'.
	 */
	public void setWriteCommunity(final String writeCommunity) {
		_writeCommunity = writeCommunity;
	}

	/**
	 * Method unmarshal.
	 * 
	 * @param reader
	 * @throws MarshalException
	 *             if object is null or if any SAXException is thrown during
	 *             marshaling
	 * @throws ValidationException
	 *             if this object is an invalid instance according to the schema
	 * @return the unmarshaled Configuration
	 */
	public static Configuration unmarshal(final Reader reader) throws MarshalException, ValidationException {
		return (Configuration) Unmarshaller.unmarshal(Configuration.class, reader);
	}

	/**
	 * 
	 * 
	 * @throws ValidationException
	 *             if this object is an invalid instance according to the schema
	 */
	public void validate() throws ValidationException {
		new Validator().validate(this);
	}

}
