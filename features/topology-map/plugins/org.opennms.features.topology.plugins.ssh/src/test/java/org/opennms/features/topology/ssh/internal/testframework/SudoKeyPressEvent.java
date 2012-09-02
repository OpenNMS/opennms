package org.opennms.features.topology.ssh.internal.testframework;

import com.google.gwt.event.dom.client.KeyPressEvent;

public class SudoKeyPressEvent extends KeyPressEvent {
    private int charCode;
    private boolean isCtrlDown;
    private boolean isAltDown;
    private boolean isShiftDown;
    
    public SudoKeyPressEvent(int k, boolean isCtrlDown, boolean isAltDown, boolean isShiftDown) {
            charCode = k;
            this.isCtrlDown = isCtrlDown;
            this.isAltDown = isAltDown;
            this.isShiftDown = isShiftDown;
    }
    
    @Override
    public int getUnicodeCharCode() {
            return charCode;
    }
    
    @Override
    public boolean isControlKeyDown(){
            return isCtrlDown;
    }
    
    @Override
    public boolean isAltKeyDown(){
            return isAltDown;
    }
    
    @Override 
    public boolean isShiftKeyDown(){
            return isShiftDown;
    }
}