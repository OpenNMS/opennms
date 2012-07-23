package org.opennms.debug;


public aspect SelectorTracker2 {
	
	pointcut selectorOpen() : call(* java.nio.channels.Selector.open(..));
	pointcut selectorClose() : call(* java.nio.channels.Selector.close(..));
	
	before(): selectorOpen() {
		System.err.println("ASPECTJ2!!! opening selector");
		Thread.dumpStack();
	}

	after(): selectorClose() {
		System.err.println("ASPECTJ2!!! closing selector");
		Thread.dumpStack();
	}



}
