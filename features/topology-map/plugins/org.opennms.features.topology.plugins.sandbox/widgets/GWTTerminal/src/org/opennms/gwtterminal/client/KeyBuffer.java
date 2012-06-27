package org.opennms.gwtterminal.client;

public class KeyBuffer {

	private Key head;
	private Key tail;
	private int size;

	public KeyBuffer () {
		this.head = null;
		this.tail = null;
		this.size = 0;
	}

	public void add(String s){
		Key newKey = new Key(s);
		if (head == null){
			head = newKey;
			tail = newKey;
		} else {
			tail.setPrev(newKey);
			newKey.setNext(tail);
			tail = newKey;
		}
		size++;
	}
	
	public String pop(){
		if (head != null){
			String headValue = head.getValue();
			if (head.getPrev() != null){
				head = head.getPrev();
			} else {
				head = null;
				tail = null;
			}
			size--;
			return headValue;
		} else return null;
	}

	public int size() {
		return this.size;
	}

}
