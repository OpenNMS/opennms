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
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.newsclub.net.unix.AFUNIXSocketException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class JUnixDomainSocketFactory implements USocketFactory {

  public JUnixDomainSocketFactory() throws AgentProxyException {
  }

  public class MySocket extends Socket {
    private AFUNIXSocket sock;
    private InputStream is;
    private OutputStream os;

    public int readFull(byte[] buf, int s, int len) throws IOException {
      int _len = len; 
      while(len>0){
        int j = is.read(buf, s, len);
        if(j<=0)
          return -1;
        if(j>0){
          s+=j;
          len-=j;
        }
      }
      return _len;
    }

    public void write(byte[] buf, int s, int len) throws IOException {
      os.write(buf, s, len);
    }

    MySocket(AFUNIXSocket sock) throws IOException {
      this.sock = sock;
      this.os = sock.getOutputStream();
      this.is = sock.getInputStream();
    }

    public void close() throws IOException {
      is.close();
      os.close();
      sock.close();
    }
  }

  public Socket open(String path) throws IOException {

    AFUNIXSocket sock = null;
    try {
      sock = AFUNIXSocket.newInstance();
      sock.connect(new AFUNIXSocketAddress(new File(path)));
    }
    catch (AFUNIXSocketException e){
      throw new IOException(e.toString());
    }
    catch (NoClassDefFoundError e){
      throw new IOException(e.toString());
    }
    return new MySocket(sock);
  }
}
