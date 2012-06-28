/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2012 ymnk, JCraft,Inc. All rights reserved.

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

@SuppressWarnings("unused")
public class AgentProxy {

  private static final byte SSH_AGENTC_REQUEST_RSA_IDENTITIES = 1;
  private static final byte SSH_AGENT_RSA_IDENTITIES_ANSWER = 2;
  private static final byte SSH_AGENTC_RSA_CHALLENGE = 3;
  private static final byte SSH_AGENT_RSA_RESPONSE = 4;
  private static final byte SSH_AGENT_FAILURE =	5;
  private static final byte SSH_AGENT_SUCCESS =	6;
  private static final byte SSH_AGENTC_ADD_RSA_IDENTITY	= 7;
  private static final byte SSH_AGENTC_REMOVE_RSA_IDENTITY = 8;
  private static final byte SSH_AGENTC_REMOVE_ALL_RSA_IDENTITIES = 9;

  private static final byte SSH2_AGENTC_REQUEST_IDENTITIES = 11;
  private static final byte SSH2_AGENT_IDENTITIES_ANSWER = 12;
  private static final byte SSH2_AGENTC_SIGN_REQUEST = 13;
  private static final byte SSH2_AGENT_SIGN_RESPONSE = 14;
  private static final byte SSH2_AGENTC_ADD_IDENTITY = 17;
  private static final byte SSH2_AGENTC_REMOVE_IDENTITY	= 18;
  private static final byte SSH2_AGENTC_REMOVE_ALL_IDENTITIES = 19;

  private static final byte SSH_AGENTC_ADD_SMARTCARD_KEY = 20;
  private static final byte SSH_AGENTC_REMOVE_SMARTCARD_KEY = 21;

  private static final byte SSH_AGENTC_LOCK = 22;
  private static final byte SSH_AGENTC_UNLOCK =	23;

  private static final byte SSH_AGENTC_ADD_RSA_ID_CONSTRAINED = 24;
  private static final byte SSH2_AGENTC_ADD_ID_CONSTRAINED = 25;
  private static final byte SSH_AGENTC_ADD_SMARTCARD_KEY_CONSTRAINED = 26;

  private static final byte SSH_AGENT_CONSTRAIN_LIFETIME = 1;
  private static final byte SSH_AGENT_CONSTRAIN_CONFIRM	= 2;

  private static final byte SSH2_AGENT_FAILURE = 30;

  private static final byte SSH_COM_AGENT2_FAILURE = 102;

  private static final byte SSH_AGENT_OLD_SIGNATURE = 0x01;

  private final byte[] buf = new byte[1024];
  private final Buffer buffer = new Buffer(buf);

  private Connector connector;

  public AgentProxy(Connector connector){
    this.connector = connector;
  }

  public synchronized Identity[] getIdentities() {
    Identity[] identities = null;

    byte code1 = SSH2_AGENTC_REQUEST_IDENTITIES;
    byte code2 = SSH2_AGENT_IDENTITIES_ANSWER;

    buffer.reset();
    buffer.putByte(code1);
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      buffer.rewind();
      buffer.putByte(SSH_AGENT_FAILURE);
      identities = new Identity[0];
      return identities;
    }

    int rcode = buffer.getByte();

    check_reply(rcode);
//System.out.println(rcode == code2);

    int count = buffer.getInt();

//System.out.println(count);

    identities = new Identity[count];

    for(int i=0; i<identities.length; i++){
      identities[i] = new Identity(buffer.getString(), buffer.getString());
    }

    return identities;
  }

  public synchronized byte[] sign(byte[] blob, byte[] data) {
    byte[] result = null;

    byte code1 = SSH2_AGENTC_SIGN_REQUEST;
    byte code2 = SSH2_AGENT_SIGN_RESPONSE;

    buffer.reset();
    buffer.putByte(code1);
    buffer.putString(blob);
    buffer.putString(data);
    buffer.putInt(0);   // SSH_AGENT_OLD_SIGNATURE
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      buffer.rewind();
      buffer.putByte(SSH_AGENT_FAILURE);
    }

    int rcode = buffer.getByte();

    check_reply(rcode);

//System.out.println(rcode == code2);

    result = buffer.getString();

    return result;
  }

  public synchronized boolean removeIdentity(byte[] blob) {
    byte code1 = SSH2_AGENTC_REMOVE_IDENTITY;

    buffer.reset();
    buffer.putByte(code1);
    buffer.putString(blob);
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      buffer.rewind();
      buffer.putByte(SSH_AGENT_FAILURE);
    }

    check_reply(buffer.getByte());

    // TODO
    return true;
  }

  public synchronized void removeAllIdentities() {
    byte code1 = SSH2_AGENTC_REMOVE_ALL_IDENTITIES;

    buffer.reset();
    buffer.putByte(code1);
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      buffer.rewind();
      buffer.putByte(SSH_AGENT_FAILURE);
    }
    check_reply(buffer.getByte());
  }

  public synchronized boolean addIdentity(byte[] identity) {
    byte code1 = SSH2_AGENTC_ADD_IDENTITY;

    buffer.reset();
    buffer.putByte(code1);
    buffer.putByte(identity);
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      buffer.rewind();
      buffer.putByte(SSH_AGENT_FAILURE);
    }

    check_reply(buffer.getByte());

    return true;
  }

  public synchronized boolean isRunning(){
    if(!connector.isAvailable())
      return false;

    byte code1 = SSH2_AGENTC_REQUEST_IDENTITIES;

    buffer.reset();
    buffer.putByte(code1);
    buffer.insertLength();

    try {
      connector.query(buffer);
    }
    catch(AgentProxyException e){
      return false;
    }
    return buffer.getByte() == SSH2_AGENT_IDENTITIES_ANSWER;
  }

  public synchronized Connector getConnector() {
    return connector;
  }

  // TODO
  private boolean check_reply(int typ) {
    // println("check_reply: "+typ)
    return true;
  }
}
