/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 9, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.monitors;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.mailtransporttest.MailTransportTest;
import org.opennms.netmgt.config.mailtransporttest.ReadmailTest;
import org.opennms.netmgt.config.mailtransporttest.SendmailTest;
import org.opennms.netmgt.dao.castor.CastorUtils;

/**
 * This is a wrapper class for handling JavaMail configurations.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class MailTransportParameters {
    
    /** Constant <code>KEY="MailTransportParameters.class.getName()"</code> */
    public static final String KEY = MailTransportParameters.class.getName();
	private static final int DEFAULT_RETRY = 1;
	private static final int DEFAULT_TIMEOUT = 3000;
    private Map<String, String> m_parameterMap;
    private MailTransportTest m_transportTest;
	private String m_testSubjectSuffix;
    private boolean m_end2EndTestInProgress = false;
    private Properties m_javamailProperties = new Properties();

    MailTransportParameters(Map<String, String> parameterMap) {
        m_parameterMap = parameterMap;
        String test = getStringParm("mail-transport-test", null);
        if (test == null) {
            throw new IllegalArgumentException("mail-transport-test must be set in monitor parameters");
        }
        m_transportTest = parseMailTransportTest(test);
    }
    
    @SuppressWarnings("unchecked")
    static synchronized MailTransportParameters get(Map parameterMap) {
        MailTransportParameters parms = (MailTransportParameters)parameterMap.get(KEY);
        if (parms == null) {
            parms = new MailTransportParameters(parameterMap);
            parameterMap.put(KEY, parms);
        }
        return parms;
    }
            
    Map<String, String> getParameterMap() {
        return m_parameterMap;
    }

    MailTransportTest getTransportTest() {
        return m_transportTest;
    }

    @SuppressWarnings("deprecation")
    MailTransportTest parseMailTransportTest(String test) {
        try {
            return CastorUtils.unmarshal(MailTransportTest.class, new StringReader(test));
        } catch (MarshalException e) {
            throw new IllegalArgumentException("Unable to parse mail-test-sequence for MailTransportMonitor: "+test, e);
        } catch (ValidationException e) {
            throw new IllegalArgumentException("Unable to parse page-sequence for HttpMonitor: "+test, e);
        }
    
    }

    private String getStringParm(String key, String deflt) {
        return ParameterMap.getKeyedString(this.getParameterMap(), key, deflt);
    }
    
    private int getIntParm(String key, int defValue) {
        return ParameterMap.getKeyedInteger(getParameterMap()  , key, defValue);
    }

	/**
	 * <p>getRetries</p>
	 *
	 * @return a int.
	 */
	public int getRetries() {
		return getIntParm("retry", MailTransportParameters.DEFAULT_RETRY);
	}

	/**
	 * <p>getTimeout</p>
	 *
	 * @return a int.
	 */
	public int getTimeout() {
		return getIntParm("timeout", MailTransportParameters.DEFAULT_TIMEOUT);
	}
	
    /**
     * <p>getReadTestPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadTestPassword() {
        return getReadTest().getUserAuth().getPassword();
    }

    ReadmailTest getReadTest() {
        return getTransportTest().getMailTest().getReadmailTest();
    }

	/**
	 * <p>getTestSubjectSuffix</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTestSubjectSuffix() {
		return m_testSubjectSuffix;
	}

	/**
	 * <p>setTestSubjectSuffix</p>
	 *
	 * @param suffix a {@link java.lang.String} object.
	 */
	public void setTestSubjectSuffix(String suffix) {
		m_testSubjectSuffix = suffix;
	}

	/**
	 * <p>getComputedTestSubject</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getComputedTestSubject() {
	    try {
	        if (getSendTestSubject() != null) {
	            return new StringBuilder(getSendTestSubject()).append(':').append(m_testSubjectSuffix == null ? null : m_testSubjectSuffix).toString();
	        } else {
	            return null;
	        }
	    } catch (IllegalStateException e) {
	        return null;
	    }
	}
	
    String getSendTestFrom() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getFrom();
    }

    /**
     * <p>isSendTestUseAuth</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestUseAuth() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isUseAuthentication();
    }

    /**
     * <p>getSendTestCharSet</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestCharSet() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getCharSet();        }

    /**
     * <p>getSendTestMessageContentType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestMessageContentType() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMessageContentType();
    }

    /**
     * <p>isSendTestDebug</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestDebug() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isDebug();
    }

    /**
     * <p>getSendTestMessageEncoding</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestMessageEncoding() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMessageEncoding();
    }

    /**
     * <p>getSendTestMailer</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestMailer() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getMailer();
    }

    /**
     * <p>getSendTestHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestHost() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailHost().getHost();        }

    /**
     * <p>getSendTestMessageBody</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestMessageBody() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getBody();
    }

    /**
     * <p>getSendTestPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestPassword() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getUserAuth().getPassword();
    }

    /**
     * <p>isSendTestIsQuitWait</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestIsQuitWait() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isQuitWait();
    }

    /**
     * <p>getSendTestPort</p>
     *
     * @return a int.
     */
    public int getSendTestPort() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return (int)getSendTest().getSendmailHost().getPort();
    }

    /**
     * <p>isSendTestIsSslEnable</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestIsSslEnable() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isSslEnable();
    }

    /**
     * <p>isSendTestStartTls</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestStartTls() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().isStartTls();
    }

    /**
     * <p>getSendTestSubject</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestSubject() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getSubject();
    }

    /**
     * <p>getSendTestRecipeint</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestRecipeint() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailMessage().getTo();
    }

    /**
     * <p>getSendTestTransport</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestTransport() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getSendmailProtocol().getTransport();
    }

    /**
     * <p>isSendTestUseJmta</p>
     *
     * @return a boolean.
     */
    public boolean isSendTestUseJmta() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().isUseJmta();
    }

    /**
     * <p>getSendTestUserName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSendTestUserName() {
        if (getSendTest() == null) {
            throw new IllegalStateException("Request for send mailparmaters invalid due to no sendmail specification in config");
        }
        return getSendTest().getUserAuth().getUserName();
    }

    SendmailTest getSendTest() {
        return getTransportTest().getMailTest().getSendmailTest();
    }

    /**
     * <p>getReadTestHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadTestHost() {
        return getReadTest().getReadmailHost().getHost();
    }

    /**
     * <p>getReadTestPort</p>
     *
     * @return a int.
     */
    public int getReadTestPort() {
        return (int)getReadTest().getReadmailHost().getPort();
    }

    /**
     * <p>getReadTestUserName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadTestUserName() {
        return getReadTest().getUserAuth().getUserName();
    }

    /**
     * <p>getReadTestFolder</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadTestFolder() {
        return getReadTest().getMailFolder();
    }

    /**
     * <p>getReadTestProtocol</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReadTestProtocol() {
        return getReadTest().getReadmailHost().getReadmailProtocol().getTransport();
    }

    /**
     * <p>isReadTestStartTlsEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isReadTestStartTlsEnabled() {
        return getReadTest().getReadmailHost().getReadmailProtocol().isStartTls();
    }
    
    /**
     * <p>isReadTestSslEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isReadTestSslEnabled() {
        return getReadTest().getReadmailHost().getReadmailProtocol().isSslEnable();
    }

    /**
     * <p>setEnd2EndTestInProgress</p>
     *
     * @param b a boolean.
     */
    public void setEnd2EndTestInProgress(boolean b) {
        m_end2EndTestInProgress  = b;
    }
    
    /**
     * <p>isEnd2EndTestInProgress</p>
     *
     * @return a boolean.
     */
    public boolean isEnd2EndTestInProgress() {
        return m_end2EndTestInProgress;
    }
    
    /**
     * <p>getReadTestAttemptInterval</p>
     *
     * @return a long.
     */
    public long getReadTestAttemptInterval() {
        return getReadTest().getAttemptInterval();
    }
    
    /**
     * <p>getSendTestAttemptInterval</p>
     *
     * @return a long.
     */
    public long getSendTestAttemptInterval() {
        return getSendTest().getAttemptInterval();
    }

    /**
     * <p>getJavamailProperties</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getJavamailProperties() {
        return m_javamailProperties;
    }

    /**
     * <p>setJavamailProperties</p>
     *
     * @param props a {@link java.util.Properties} object.
     */
    public void setJavamailProperties(Properties props) {
        m_javamailProperties = props;
    }

    /**
     * <p>setReadTestHost</p>
     *
     * @param host a {@link java.lang.String} object.
     */
    public void setReadTestHost(String host) {
        getReadTest().getReadmailHost().setHost(host);
    }

    /**
     * <p>setSendTestHost</p>
     *
     * @param host a {@link java.lang.String} object.
     */
    public void setSendTestHost(String host) {
        getSendTest().getSendmailHost().setHost(host);
    }
}
