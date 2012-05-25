package org.opennms.features.topology.plugins.menu.internal;

import java.util.List;

import org.opennms.features.topology.api.Operation;

public class HelloWorldOperation implements Operation {

	@Override
	public Undoer execute(final List<Object> targets) {
		System.out.println("Hello, world!");
		return null;
	}

	@Override
	public boolean display(final List<Object> targets) {
		return true;
	}

	@Override
	public boolean enabled(final List<Object> targets) {
		return true;
	}

	@Override
	public String getLabel() {
		return "Hello World";
	}

	@Override
	public String getId() {
		return "helloworld";
	}

}
