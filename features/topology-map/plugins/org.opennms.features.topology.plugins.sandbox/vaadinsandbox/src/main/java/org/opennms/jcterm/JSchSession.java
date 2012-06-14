/* -*-mode:java; c-basic-offset:2; -*- */
/* JSchSession
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 *  
 * Written by: ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jcterm;

import com.jcraft.jsch.*;

public class JSchSession{
  private static JSch jsch=null;
  private static SessionFactory sessionFactory=null;

  private static java.util.Hashtable pool=new java.util.Hashtable();

  private String key=null;
  private Session session=null;

  public static JSchSession getSession(String username, String password,
      String hostname, int port, UserInfo userinfo, Proxy proxy)
      throws JSchException{
    String key=getPoolKey(username, hostname, port);
    try{
      JSchSession jschSession=(JSchSession)pool.get(key);
      if(jschSession!=null&&!jschSession.getSession().isConnected()){
        pool.remove(key);
        jschSession=null;
      }
      if(jschSession==null){
        Session session=null;
        try{
          session=createSession(username, password, hostname, port, userinfo,
              proxy);
        }
        catch(JSchException e){
          if(isAuthenticationFailure(e)){
            session=createSession(username, password, hostname, port, userinfo,
                proxy);
          }
          else{
            throw e;
          }
        }

        if(session==null)
          throw new JSchException("The JSch service is not available");

        JSchSession schSession=new JSchSession(session, key);
        pool.put(key, schSession);

        return schSession;
      }
      return jschSession;
    }
    catch(JSchException e){
      pool.remove(key);
      throw e;
    }
  }

  private static synchronized JSch getJSch(){
    if(jsch==null){
      jsch=new JSch();
    }
    return jsch;
  }

  private static Session createSession(String username, String password,
      String hostname, int port, UserInfo userinfo, Proxy proxy)
      throws JSchException{
    Session session=null;
    if(sessionFactory==null){
      session=getJSch().getSession(username, hostname, port);
    }
    else{
      session=sessionFactory.getSession(username, hostname, port);
    }
    session.setTimeout(60000);
    if(password!=null)
      session.setPassword(password);
    session.setUserInfo(userinfo);
    if(proxy!=null)
      session.setProxy(proxy);
    session.connect(60000);
    session.setServerAliveInterval(60000);
    return session;
  }

  private static String getPoolKey(String username, String hostname, int port){
    return username+"@"+hostname+":"+port;
  }

  private JSchSession(Session session, String key){
    this.session=session;
    this.key=key;
  }

  public Session getSession(){
    return session;
  }

  public void dispose(){
    if(session.isConnected()){
      session.disconnect();
    }
    pool.remove(key);
  }

  public static boolean isAuthenticationFailure(JSchException ee){
    return ee.getMessage().equals("Auth fail");
  }

  public static interface SessionFactory{
    Session getSession(String username, String hostname, int port)
        throws JSchException;
  }

  public static void setSessionFactory(SessionFactory sf){
    sessionFactory=sf;
  }

  static void useSSHAgent(boolean use){
    IdentityRepository ir = null;
    if(use){
      try{
        Class c = Class.forName("com.jcraft.jcterm.JCTermIdentityRepository");
        ir = (IdentityRepository)(c.newInstance());
      }
      catch(java.lang.NoClassDefFoundError e){
	System.err.println(e);
      }
      catch(Exception e){
	System.err.println(e);
      }
      if(ir==null){
        System.err.println("JCTermIdentityRepository is not available.");
      }
    }
    if(ir!=null)
      getJSch().setIdentityRepository(ir);
  }
}
