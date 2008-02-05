package org.opennms.ovapi;

import org.opennms.ovapi.CLibrary.fd_set;

public abstract class Macros {

    // void FD_SET(int fd, fd_set *fdset);
    public static void FD_SET(int fd, fd_set fdset) {
        fdset.set(fd);
    }

    // void FD_CLR(int fd, fd_set *fdset);
    public static void FD_CLR(int fd, fd_set fdset) {
        fdset.clr(fd);
    }

    // int FD_ISSET(int fd, fd_set *fdset);
    public static boolean FD_ISSET(int fd, fd_set fdset) {
        return fdset.isSet(fd);
    }

    // void FD_ZERO(fd_set *fdset);
    public static void FD_ZERO(fd_set fdset) {
        fdset.zero();
    }
    
    
    
}
