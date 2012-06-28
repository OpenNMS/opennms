/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.opennms.sandbox.jcterm.agentproxy.usocket;

import org.opennms.sandbox.jcterm.agentproxy.*;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.io.IOException;

public class JNAUSocketFactory implements USocketFactory {

  public interface CLibrary extends Library {
    CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
    int socket(int domain, int type, int protocol);
    int fcntl(int fd, int cmd, Object... args);
    int connect(int sockfd, Pointer addr, int addrlen);
    int close(int fd);
    int read(int fd, byte[] buf, int count);
    int write(int fd, byte[] buf, int count);
  }

  public static class SockAddr extends Structure {
    public short sun_family;
    public byte[] sun_path;
  }

  public JNAUSocketFactory() throws AgentProxyException {
  }

  public class MySocket extends Socket {
    private int sock;

    public int readFull(byte[] buf, int s, int len) throws IOException {
      byte[] _buf = buf;
      int _len = len;
      int _s = s;

      while(_len > 0){
        if(_s != 0){
          _buf = new byte[_len];
        }
        int i = CLibrary.INSTANCE.read(sock, _buf, _len);
        if(i <= 0){
          return -1;
          // throw new IOException("failed to read usocket");
        }
        if(_s != 0)
          System.arraycopy(_buf, 0, buf, _s, i);
        _s += i;
        _len -= i;
      }
      return len;
    }

    public void write(byte[] buf, int s, int len) throws IOException {
      byte[] _buf = buf;
      if(s != 0){
        _buf = new byte[len];
        System.arraycopy(buf, s, _buf, 0, len);
      }
      CLibrary.INSTANCE.write(sock, _buf, len);
    }

    MySocket(int sock) throws IOException {
      this.sock = sock;
    }

    public void close() throws IOException {
      CLibrary.INSTANCE.close(sock);
    }
  }

  public Socket open(String path) throws IOException {

    int sock = CLibrary.INSTANCE.socket(1,  // AF_UNIX
                                        1,  // SOCK_STREAM
                                        0);
    if(sock < 0){
      throw new IOException("failed to allocate usocket");
    }

    if(CLibrary.INSTANCE.fcntl(sock,  2, 8) < 0){
      CLibrary.INSTANCE.close(sock);
      throw new IOException("failed to fctrl usocket");
    }

    SockAddr sockaddr = new SockAddr();
    sockaddr.sun_family = 1; 
    sockaddr.sun_path = new byte[108];
    System.arraycopy(path.getBytes(), 0,
                     sockaddr.sun_path, 0,
                     path.length());
    sockaddr.write();

    if(CLibrary.INSTANCE.connect(sock, sockaddr.getPointer(), 110)<0){
      throw new IOException("failed to fctrl usocket");
    }

    return new MySocket(sock);
  }
}
