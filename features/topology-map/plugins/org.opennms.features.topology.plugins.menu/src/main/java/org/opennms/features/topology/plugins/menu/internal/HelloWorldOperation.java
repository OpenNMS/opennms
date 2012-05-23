package org.opennms.features.topology.plugins.menu.internal;

import java.util.List;

import org.opennms.features.topology.api.Operation;

public class HelloWorldOperation implements Operation {

	@Override
	public Undoer execute(List<Object> targets) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean display(List<Object> targets) {
		return true;
	}

	@Override
	public boolean enabled(List<Object> targets) {
		return true;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

}
