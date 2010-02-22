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

@XmlRootElement(name="mobile-sequence")
public class MobileSequenceConfig implements Serializable, Comparable<MobileSequenceConfig> {
    
    private static final long serialVersionUID = 1L;

    /* attributes and elements */
	private List<SequenceSessionVariable> m_sessionVariables;
    private List<MobileSequenceTransaction> m_transactions;

	
	public void addSessionVariable(SequenceSessionVariable var) {
		getSessionVariables().add(var);
	}

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

	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables) {
		m_sessionVariables = sessionVariables;
	}

	public void addTransaction(MobileSequenceTransaction transaction) {
		getTransactions().add(transaction);
	}

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

    public void setTransactions(List<MobileSequenceTransaction> transactions) {
        List<MobileSequenceTransaction> oldTransactions = getTransactions();
        if (oldTransactions != transactions) {
            oldTransactions.clear();
            oldTransactions.addAll(transactions);
        }
	}

	public int compareTo(MobileSequenceConfig o) {
		return new CompareToBuilder()
			.append(this.getTransactions(), o.getTransactions())
			.toComparison();
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("transactions", getTransactions())
			.toString();
	}

	public void computeDefaultGateways() {
		String defaultGatewayId = "*";
		for (final MobileSequenceTransaction t : getTransactions()) {
			if (t.getGatewayId() != null) {
				defaultGatewayId = t.getGatewayId();
			}
			
			t.setDefaultGatewayId(defaultGatewayId);
		}
	}

	public MobileSequenceTransaction createTransaction(MobileSequenceRequest request, MobileSequenceResponse response) {

        MobileSequenceTransaction t = new MobileSequenceTransaction();
		addTransaction(t);
		
		t.setRequest(request);
		
		t.addResponse(response);
        return t;
    }

	public Map<String, Number> executeSequence(MobileSequenceSession session, DefaultTaskCoordinator coordinator) throws SequencerException, Throwable {
	    
        MobileSequenceExecution execution = start(session, coordinator);
        
        waitFor(session, execution);
		
		return execution.getResponseTimes();
	}

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

    public void waitFor(MobileSequenceSession session, MobileSequenceExecution execution) throws InterruptedException, ExecutionException, Throwable {

        execution.waitFor();

        execution.updateResults(session);
    }

    public boolean hasTransactions() {
        return getTransactions() != null && getTransactions().size() != 0;
    }


}

