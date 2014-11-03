/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal;

import org.opennms.sms.monitor.internal.config.MobileSequenceConfig;
import org.opennms.sms.monitor.internal.config.MobileSequenceRequest;
import org.opennms.sms.monitor.internal.config.MobileSequenceResponse;
import org.opennms.sms.monitor.internal.config.MobileSequenceTransaction;
import org.opennms.sms.monitor.internal.config.SequenceResponseMatcher;
import org.opennms.sms.monitor.internal.config.SequenceSessionVariable;
import org.opennms.sms.monitor.internal.config.SmsSequenceRequest;
import org.opennms.sms.monitor.internal.config.SmsSequenceResponse;
import org.opennms.sms.monitor.internal.config.SmsSourceMatcher;
import org.opennms.sms.monitor.internal.config.TextResponseMatcher;
import org.opennms.sms.monitor.internal.config.UssdSequenceRequest;
import org.opennms.sms.monitor.internal.config.UssdSequenceResponse;
import org.opennms.sms.monitor.internal.config.UssdSessionStatusMatcher;
import org.opennms.sms.monitor.session.SessionVariableGenerator;
import org.smslib.USSDSessionStatus;

/**
 * MobileSequenceConfigBuilder
 *
 * @author brozow
 * @version $Id: $
 */
public class MobileSequenceConfigBuilder {
    
    private MobileSequenceConfig m_sequence;
    
    /**
     * <p>Constructor for MobileSequenceConfigBuilder.</p>
     */
    public MobileSequenceConfigBuilder() {
        this(new MobileSequenceConfig());
    }
    
    /**
     * <p>Constructor for MobileSequenceConfigBuilder.</p>
     *
     * @param sequence a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
     */
    public MobileSequenceConfigBuilder(MobileSequenceConfig sequence) {
        m_sequence = sequence;
    }
    
    /**
     * <p>getSequence</p>
     *
     * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
     */
    public MobileSequenceConfig getSequence() {
        return m_sequence;
    }
    
    /**
     * <p>addTransaction</p>
     *
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder.MobileSequenceTransactionBuilder} object.
     */
    public MobileSequenceTransactionBuilder addTransaction() {
        MobileSequenceTransaction t = new MobileSequenceTransaction();
        getSequence().addTransaction(t);
        return new MobileSequenceTransactionBuilder(t);
    }
    
    /**
     * <p>request</p>
     *
     * @param request a {@link org.opennms.sms.monitor.internal.config.MobileSequenceRequest} object.
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder.MobileSequenceTransactionBuilder} object.
     */
    public MobileSequenceTransactionBuilder request(MobileSequenceRequest request) {
        return addTransaction().setRequest(request);
    }
    
    
    /**
     * <p>smsRequest</p>
     *
     * @param label a {@link java.lang.String} object.
     * @param gatewayId a {@link java.lang.String} object.
     * @param recipient a {@link java.lang.String} object.
     * @param text a {@link java.lang.String} object.
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder.MobileSequenceTransactionBuilder} object.
     */
    public MobileSequenceTransactionBuilder smsRequest(String label, String gatewayId, String recipient, String text) {
        
        SmsSequenceRequest smsRequest = new SmsSequenceRequest();
        smsRequest.setLabel(label);
        smsRequest.setGatewayId(gatewayId);
        smsRequest.setRecipient(recipient);
        smsRequest.setText(text);
        
        return request(smsRequest);
    }

    /**
     * <p>ussdRequest</p>
     *
     * @param label a {@link java.lang.String} object.
     * @param gatewayId a {@link java.lang.String} object.
     * @param text a {@link java.lang.String} object.
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder.MobileSequenceTransactionBuilder} object.
     */
    public MobileSequenceTransactionBuilder ussdRequest(String label, String gatewayId, String text) {
        
        MobileSequenceRequest ussdRequest = new UssdSequenceRequest();
        ussdRequest.setLabel(label);
        ussdRequest.setGatewayId(gatewayId);
        ussdRequest.setText(text);
        
        return request(ussdRequest);

    }

    public static class MobileSequenceTransactionBuilder {
        private MobileSequenceTransaction m_transaction;
        
        public MobileSequenceTransactionBuilder(MobileSequenceTransaction transaction) {
            m_transaction = transaction;
        }
        
        public MobileSequenceTransactionBuilder setRequest(MobileSequenceRequest sequenceRequest) {
            m_transaction.setRequest(sequenceRequest);
            return this;
        }

        public MobileSequenceTransaction getTransaction() {
            return m_transaction;
        }

        public void addResponse(MobileSequenceResponse response) {
            m_transaction.addResponse(response);
        }

        public SmsResponseBuilder expectSmsResponse() {
            return expectSmsResponse(null);
        }

        public SmsResponseBuilder expectSmsResponse(String label) {
            MobileSequenceResponse response = new SmsSequenceResponse(label);
        
            addResponse(response);
            return new SmsResponseBuilder(response);
        }

        public UssdResponseBuilder expectUssdResponse() {
            return expectUssdResponse(null);
        }

        public UssdResponseBuilder expectUssdResponse(String label) {
            UssdSequenceResponse response = new UssdSequenceResponse(label);
            addResponse(response);
            return new UssdResponseBuilder(response);
        }

        public MobileSequenceTransactionBuilder withTransactionLabel(String transactionLabel) {
            m_transaction.setLabel(transactionLabel);
            return this;
        }
        
        public MobileSequenceTransactionBuilder withGatewayId(String gatewayId) {
            m_transaction.setGatewayId(gatewayId);
            return this;
        }


    }
    

    /**
     * SmsResponseBuilder
     *
     * @author brozow
     */
    public static class SmsResponseBuilder {
        private MobileSequenceResponse m_response;

        public SmsResponseBuilder(MobileSequenceResponse response) {
            m_response = response;
        }

        public SmsResponseBuilder addMatcher(SequenceResponseMatcher matcher) {
            m_response.addMatcher(matcher);
            return this;
        }

        public SmsResponseBuilder matching(String regex) {
            return addMatcher(new TextResponseMatcher(regex));
        }
        
        public SmsResponseBuilder srcMatches(String originator) {
            addMatcher(new SmsSourceMatcher(originator));
            return this;
        }

        public SmsResponseBuilder onGateway(String gatewayId) {
            m_response.setGatewayId(gatewayId);
            return this;
        }

    }

    

    /**
     * SmsResponseBuilder
     *
     * @author brozow
     */
    public static class UssdResponseBuilder {
        private UssdSequenceResponse m_response;

        public UssdResponseBuilder(UssdSequenceResponse response) {
            m_response = response;
        }

        public UssdResponseBuilder addMatcher(SequenceResponseMatcher matcher) {
            m_response.addMatcher(matcher);
            return this;
        }

        public UssdResponseBuilder matching(String regex) {
            return addMatcher(new TextResponseMatcher(regex));
        }

        public UssdResponseBuilder withSessionStatus(USSDSessionStatus sessionStatus) {
            return addMatcher(new UssdSessionStatusMatcher(sessionStatus));
        }

        public UssdResponseBuilder onGateway(String gatewayId) {
            m_response.setGatewayId(gatewayId);
            return this;
        }

    }



    /**
     * <p>variable</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param variableClass a {@link java.lang.Class} object.
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceConfigBuilder.SessionVariableBuilder} object.
     */
    public SessionVariableBuilder variable(String name, Class<? extends SessionVariableGenerator> variableClass) {
        SequenceSessionVariable var = new SequenceSessionVariable(name, variableClass.getName());
        m_sequence.addSessionVariable(var);
        return new SessionVariableBuilder(var);
        
    }
    
    public static class SessionVariableBuilder {
        private SequenceSessionVariable m_var;

        public SessionVariableBuilder(SequenceSessionVariable var) {
            m_var = var;
        }

        public SessionVariableBuilder parameter(String key, Object val) {
            m_var.addParameter(key, val == null ? null : val.toString());
            return this;
        }
        
    }

    

}
