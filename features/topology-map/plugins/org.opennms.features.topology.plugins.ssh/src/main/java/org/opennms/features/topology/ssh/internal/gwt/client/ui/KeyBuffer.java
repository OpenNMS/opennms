package org.opennms.features.topology.ssh.internal.gwt.client.ui;

/**
 * The KeyBuffer class is a two-way linked list that keeps track
 * of keys entered by the user in order of when they typed them.
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class KeyBuffer {

	private Key head; //Key at the beginning of the list
	private Key tail; //Key at the end of the list
	private int size; //Size of the list

	/**
	 * The KeyBuffer() constructor creates a linked list and initalizes the head
	 * and tail to null, and the size to 0
	 */
	public KeyBuffer () {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}

	/**
	 * The add method takes a passed in String and creates a Key object. The new Key
	 * is then added to the tail end of the list. If the list is empty, the new Key is set as
	 * the head of the list.
	 * @param s Value of the Key
	 */
	public void add(String s){
		Key newKey = new Key(s);
		if (head == null){
			head = newKey;
			tail = newKey;
		} else {
			tail.setNext(newKey);
			newKey.setPrev(tail);
			tail = newKey;
		}
		size++;
	}

	/**
	 * The size method returns the current number of Keys in the list
	 * @return Number of Keys in List
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * The toString method appends all of the Keys into one String
	 */
	@Override
	public String toString() {
		String s = "";
		Key current = head;
		while (current != null) {
			s += current.getValue();
			current = current.getNext(); 
		}
		return s;
	}
	
	/**
	 * The drain method returns the toString representation of the List
	 * and then empties the list of all keys
	 * @return toString represention of the List
	 */
	public String drain() {
		String keybufContents = toString();
		free();
		return keybufContents;
	}
	
	/**
	 * The free method releases all objects and resources used in
	 * the List so that any unused memory can be garbage collected.
	 */
	public void free() {
		Key current = head;
		Key next = null;
		while (current != null) {
			if (current.getNext() != null) {
				next = current.getNext();
			}
			current.setNext(null);
			current.setPrev(null);
			current.setValue(null);
			current = next;
			next = null;
		}
		head = tail = null;
	}

}
