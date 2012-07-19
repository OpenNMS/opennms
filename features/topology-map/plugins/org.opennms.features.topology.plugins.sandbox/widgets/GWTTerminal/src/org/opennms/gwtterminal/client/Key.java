package org.opennms.gwtterminal.client;

public class Key {

	private Key prev;
	private Key next;
	private String k;
	
	public Key(String k) {
		this.setValue(k);
		this.prev = null;
		this.next = null;
	}

	public Key getNext() {
		return next;
	}

	public void setNext(Key next) {
		this.next = next;
	}

	public Key getPrev() {
		return prev;
	}

	public void setPrev(Key prev) {
		this.prev = prev;
	}

	public String getValue() {
		return k;
	}

	public void setValue(String k) {
		this.k = k;
	}
}
