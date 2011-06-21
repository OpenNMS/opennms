package org.opennms.netmgt.scriptd.helper;

import java.util.regex.Pattern;

import org.opennms.netmgt.xml.event.Event;

public class UeiEventMatch implements EventMatch {

	private String ueimatch;
	
	public UeiEventMatch() {
		super();
		this.ueimatch = null;
	}
	
	
	public UeiEventMatch(String ueimatch) {
		super();
		this.ueimatch = ueimatch;
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
}
