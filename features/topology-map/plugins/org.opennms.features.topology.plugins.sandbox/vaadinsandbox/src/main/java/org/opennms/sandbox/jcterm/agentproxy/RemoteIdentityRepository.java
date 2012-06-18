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

package org.opennms.sandbox.jcterm.agentproxy;

import java.util.Vector;


import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSchException;

public class RemoteIdentityRepository implements IdentityRepository {

  private AgentProxy agent;
  public RemoteIdentityRepository(Connector connector) {
    this.agent = new AgentProxy(connector);
  }

  public Vector<com.jcraft.jsch.Identity> getIdentities() {
    Vector<com.jcraft.jsch.Identity> result = 
     new Vector<com.jcraft.jsch.Identity>(); 

    Identity[] identities = agent.getIdentities();

    for(int i=0; i<identities.length; i++){

      final Identity _identity = identities[i];

      com.jcraft.jsch.Identity id = new com.jcraft.jsch.Identity(){
        byte[] blob = _identity.getBlob();
        String algname = new String((new Buffer(blob)).getString());
        public boolean setPassphrase(byte[] passphrase) throws JSchException{
          return true;
        }
        public byte[] getPublicKeyBlob() { return blob; }
        public byte[] getSignature(byte[] data){
          return agent.sign(blob, data);
        }
        public boolean decrypt() { return true; }
        public String getAlgName() { return algname; }
        public String getName() { return new String(_identity.getComment()); }
        public boolean isEncrypted() { return false; }
        public void clear() { }
      };

      result.addElement(id);
    }

    return result;
  }

  public boolean add(byte[] identity) {
    return agent.addIdentity(identity);
  }

  public boolean remove(byte[] blob) {
    return agent.removeIdentity(blob);
  }

  public void removeAll() {
    agent.removeAllIdentities();
  }

  public String getName() {
    return agent.getConnector().getName();
  }

  public int getStatus() {
    if(agent.getConnector().isAvailable()){
      return NOTRUNNING;
    }
    else {
      return RUNNING;
    }
  }
}
