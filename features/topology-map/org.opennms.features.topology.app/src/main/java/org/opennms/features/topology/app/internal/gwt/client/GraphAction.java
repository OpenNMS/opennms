package org.opennms.features.topology.app.internal.gwt.client;

import com.vaadin.terminal.gwt.client.ui.Action;
import com.vaadin.terminal.gwt.client.ui.ActionOwner;

public class GraphAction extends Action{

	private String m_targetKey;
	private String m_actionKey;

	public GraphAction(ActionOwner owner) {
		super(owner);
		
	}
	
	public GraphAction(ActionOwner owner, String target, String action) {
		this(owner);
		m_targetKey = target;
		m_actionKey = action;
	}

	@Override
	public void execute() {
		owner.getClient().updateVariable(owner.getPaintableId(), "action", m_targetKey + "," + m_actionKey
				, true);
		owner.getClient().getContextMenu().hide();
		
	}
	
}