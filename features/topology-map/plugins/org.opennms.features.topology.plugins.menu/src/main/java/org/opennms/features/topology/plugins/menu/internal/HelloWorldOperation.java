package org.opennms.features.topology.plugins.menu.internal;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

public class HelloWorldOperation implements Operation {

	@Override
	public Undoer execute(final List<Object> targets, OperationContext operationContext) {
		System.out.println("Hello, world!");
		return null;
	}

	@Override
	public boolean display(final List<Object> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public boolean enabled(final List<Object> targets, OperationContext operationContext) {
		return true;
	}

	@Override
	public String getId() {
		return "helloworld";
	}

}
