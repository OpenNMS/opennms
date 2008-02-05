package org.opennms.ovapi;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public interface CLibrary extends Library {
    
    public static final CLibrary INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
    
    public static final int FD_SETSIZE = 1024;
    public static final int NFDBITS = 32 * 8;
    public static final int FD_NFDBITS = NFDBITS;
    
    public static final int howmany = (((FD_SETSIZE)+((FD_NFDBITS)-1))/(FD_NFDBITS));
    
    public static class fd_set extends Structure {
        public int[] fds_bits = new int[howmany]; 
        
        public void set(int fd) {
            int index = fd / FD_NFDBITS;
            int offset = fd % FD_NFDBITS;
            
            fds_bits[index] |= (1 << offset);
        }
        
        public void clr(int fd) {
            int index = fd / FD_NFDBITS;
            int offset = fd % FD_NFDBITS;
            
            fds_bits[index] &= ~(1 << offset);
        }
        
        public boolean isSet(int fd) {
            int index = fd / FD_NFDBITS;
            int offset = fd % FD_NFDBITS;
            
            return (fds_bits[index] & (1 << offset)) != 0;
        }
        
        public void zero() {
            for(int i = 0; i < fds_bits.length; i++) {
                fds_bits[i] = 0;
            }
        }
    }
    
    public static class timeval extends Structure {
        public NativeLong tv_sec = new NativeLong(0);
        public NativeLong tv_usec = new NativeLong(0);
        
        public boolean isSet() {
            return tv_sec.longValue() != 0 || tv_usec.longValue() != 0;
        }
        
        public void clear() {
            tv_sec.setValue(0);
            tv_usec.setValue(0);
        }
        
        public void setTimeInMillis(long millis) {
            tv_sec.setValue(millis / 1000L);
            tv_usec.setValue((millis % 1000L) * 1000L);
        }
        
        public long getTimeInMillis() {
            return (tv_sec.longValue() * 1000L)+((tv_usec.longValue()+500L) / 1000L); 
        }
        
    }
    

    // int select(int  nfds,  fd_set  *readfds,  fd_set  *writefds, fd_set *errorfds, struct timeval *timeout);
    int select(int nfds, fd_set readfds, fd_set writefds, fd_set errorfds, timeval timeout);

}
