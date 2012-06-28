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
			tail.setNext(newKey);
			newKey.setPrev(tail);
			tail = newKey;
		}
		size++;
	}
	
	public String pop(){
		if (head != null){
			String headValue = head.getValue();
			if (head.getNext() != null){
				head = head.getNext();
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

}
