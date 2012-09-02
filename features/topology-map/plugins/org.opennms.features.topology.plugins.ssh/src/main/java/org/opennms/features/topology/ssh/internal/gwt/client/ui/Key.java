package org.opennms.features.topology.ssh.internal.gwt.client.ui;

/**
 * The Key class serves as a Node for the KeyBuffer class
 * Each Key contains a String and a pointer to the next and previous
 * Key in the linked list
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class Key {

	private Key prev; //Pointer to the Key before this one in the list
	private Key next; //Pointer to the Key after this one in the list
	private String k; //Value of the Key
	
	/**
	 * The Key(String K) constructor creates a Key with null pointers to
	 * next and previous Keys, and the value is set to the passed in value
	 * @param k initial value of the Key
	 */
	public Key(String k) {
		this.setValue(k);
		this.prev = null;
		this.next = null;
	}

	/**
	 * The getNext method returns the pointer to the next Key
	 * @return Next Key in list
	 */
	public Key getNext() {
		return next;
	}

	/**
	 * The setNext method sets the pointer to the next Key
	 * @param Pointer to the next Key in the list
	 */
	public void setNext(Key next) {
		this.next = next;
	}

	/**
	 * The getPrev method returns the pointer to the previous Key
	 * @return Previous Key in list
	 */
	public Key getPrev() {
		return prev;
	}

	/**
	 * The setPrev method sets the pointer to the previous Key
	 * @param Pointer to the previous Key in the list
	 */
	public void setPrev(Key prev) {
		this.prev = prev;
	}
	
	/**
	 * The getValue method returns the string value of the Key
	 * @return String value of Key
	 */
	public String getValue() {
		return k;
	}

	/**
	 * The setValue method sets the string value of the Key
	 * @param k Value to set
	 */
	public void setValue(String k) {
		this.k = k;
	}
	
}
