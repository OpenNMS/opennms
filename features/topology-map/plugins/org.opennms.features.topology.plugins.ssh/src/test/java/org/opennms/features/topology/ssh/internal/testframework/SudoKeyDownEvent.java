package org.opennms.features.topology.ssh.internal.testframework;

import com.google.gwt.event.dom.client.KeyDownEvent;



public class SudoKeyDownEvent extends KeyDownEvent {
    private int keyCode;
    private boolean isCtrlDown;
    private boolean isAltDown;
    private boolean isShiftDown;
    
    public SudoKeyDownEvent(int k, boolean isCtrlDown, boolean isAltDown, boolean isShiftDown) {
            keyCode = k;
            this.isCtrlDown = isCtrlDown;
            this.isAltDown = isAltDown;
            this.isShiftDown = isShiftDown;
    }
    
    @Override
    public int getNativeKeyCode() {
            return keyCode;
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