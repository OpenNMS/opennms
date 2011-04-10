package org.opennms.netmgt.scriptd.helper;

import java.util.regex.Pattern;

import org.opennms.netmgt.xml.event.Event;

public class UeiEventFilter implements EventFilter {

	private String ueimatch;
	private boolean allow;
	
	public UeiEventFilter(boolean allow) {
		super();
		this.ueimatch = null;
		this.allow = allow;
	}
	
	
	public UeiEventFilter(String ueimatch, boolean allow) {
		super();
		this.ueimatch = ueimatch;
		this.allow = allow;
	}

	
	public boolean match(Event event) {
		if (event == null) return false;
		if (event.getUei() == null ) return false;
		if (this.ueimatch == null) return false;
		if (this.ueimatch.startsWith("~"))
			return rematch(event.getUei(), this.ueimatch.substring(1));
		else
			return (event.getUei().equals(this.ueimatch));
		
	}

	private boolean rematch(String text, String regex) {
		Pattern p = Pattern.compile(regex);
		return p.matcher(text).matches();
	}


	public Event filter(Event event) {
		if (allow)
			return event;
		return null;
	}

}
