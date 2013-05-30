/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.monitor.internal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.monitor.SequencerException;
import org.opennms.sms.monitor.internal.MobileSequenceExecution;
import org.springframework.util.Assert;

/**
 * <p>MobileSequenceConfig class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="mobile-sequence")
public class MobileSequenceConfig implements Serializable, Comparable<MobileSequenceConfig> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 142043644615784730L;
    /* attributes and elements */
	private List<SequenceSessionVariable> m_sessionVariables;
    private List<MobileSequenceTransaction> m_transactions;

	
	/**
	 * <p>addSessionVariable</p>
	 *
	 * @param var a {@link org.opennms.sms.monitor.internal.config.SequenceSessionVariable} object.
	 */
	public void addSessionVariable(SequenceSessionVariable var) {
		getSessionVariables().add(var);
	}

	/**
	 * <p>getSessionVariables</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@XmlElement(name="session-variable")
	public List<SequenceSessionVariable> getSessionVariables() {
		if (m_sessionVariables == null) {
			m_sessionVariables = createSessionVariableList();
		}
		return m_sessionVariables;
	}

    private List<SequenceSessionVariable> createSessionVariableList() {
        return Collections.synchronizedList(new ArrayList<SequenceSessionVariable>());
    }

	/**
	 * <p>setSessionVariables</p>
	 *
	 * @param sessionVariables a {@link java.util.List} object.
	 */
	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables) {
		m_sessionVariables = sessionVariables;
	}

	/**
	 * <p>addTransaction</p>
	 *
	 * @param transaction a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
	 */
	public void addTransaction(MobileSequenceTransaction transaction) {
		getTransactions().add(transaction);
	}

	/**
	 * <p>getTransactions</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@XmlElement(name="transaction")
	public List<MobileSequenceTransaction> getTransactions() {
        if (m_transactions == null) {
            m_transactions = createTransactionList();
        }
		return m_transactions;
	}

	private List<MobileSequenceTransaction> createTransactionList() {
	    return new TriggeredList<MobileSequenceTransaction>() {

            @Override
            protected void onAdd(int index, MobileSequenceTransaction element) {
                element.setSequenceConfig(MobileSequenceConfig.this);
            }

            @Override
            protected void onRemove(int index, MobileSequenceTransaction element) {
                element.setSequenceConfig(null);
            }
	    };
    }

    /**
     * <p>setTransactions</p>
     *
     * @param transactions a {@link java.util.List} object.
     */
    public void setTransactions(List<MobileSequenceTransaction> transactions) {
        final List<MobileSequenceTransaction> oldTransactions = getTransactions();
        if (oldTransactions == transactions) return;
        oldTransactions.clear();
        oldTransactions.addAll(transactions);
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param o a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
	 * @return a int.
	 */
    @Override
	public int compareTo(MobileSequenceConfig o) {
		return new CompareToBuilder()
			.append(this.getTransactions(), o.getTransactions())
			.toComparison();
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("transactions", getTransactions())
			.toString();
	}

	/**
	 * <p>computeDefaultGateways</p>
	 */
	public void computeDefaultGateways() {
		String defaultGatewayId = "*";
		for (final MobileSequenceTransaction t : getTransactions()) {
			if (t.getGatewayId() != null) {
				defaultGatewayId = t.getGatewayId();
			}
			
			t.setDefaultGatewayId(defaultGatewayId);
		}
	}

	/**
	 * <p>createTransaction</p>
	 *
	 * @param request a {@link org.opennms.sms.monitor.internal.config.MobileSequenceRequest} object.
	 * @param response a {@link org.opennms.sms.monitor.internal.config.MobileSequenceResponse} object.
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
	 */
	public MobileSequenceTransaction createTransaction(MobileSequenceRequest request, MobileSequenceResponse response) {

        MobileSequenceTransaction t = new MobileSequenceTransaction();
		addTransaction(t);
		
		t.setRequest(request);
		
		t.addResponse(response);
        return t;
    }

	/**
	 * <p>executeSequence</p>
	 *
	 * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
	 * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
	 * @return a {@link java.util.Map} object.
	 * @throws org.opennms.sms.monitor.SequencerException if any.
	 * @throws java.lang.Throwable if any.
	 */
	public Map<String, Number> executeSequence(MobileSequenceSession session, DefaultTaskCoordinator coordinator) throws SequencerException, Throwable {
	    
        MobileSequenceExecution execution = start(session, coordinator);
        
        waitFor(session, execution);
		
		return execution.getResponseTimes();
	}

    /**
     * <p>start</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     * @return a {@link org.opennms.sms.monitor.internal.MobileSequenceExecution} object.
     * @throws org.opennms.sms.monitor.SequencerException if any.
     */
    public MobileSequenceExecution start(MobileSequenceSession session, DefaultTaskCoordinator coordinator) throws SequencerException {
        
        Assert.notNull(coordinator);

        computeDefaultGateways();
        
        MobileSequenceExecution execution = createExecution();
        
        execution.start(session, coordinator);
        
        return execution;

    }

    private MobileSequenceExecution createExecution() {
        return new MobileSequenceExecution(this);
    }

    /**
     * <p>waitFor</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param execution a {@link org.opennms.sms.monitor.internal.MobileSequenceExecution} object.
     * @throws java.lang.InterruptedException if any.
     * @throws java.util.concurrent.ExecutionException if any.
     * @throws java.lang.Throwable if any.
     */
    public void waitFor(MobileSequenceSession session, MobileSequenceExecution execution) throws InterruptedException, ExecutionException, Throwable {

        execution.waitFor();

        execution.updateResults(session);
    }

    /**
     * <p>hasTransactions</p>
     *
     * @return a boolean.
     */
    public boolean hasTransactions() {
        return getTransactions() != null && getTransactions().size() != 0;
    }


}

