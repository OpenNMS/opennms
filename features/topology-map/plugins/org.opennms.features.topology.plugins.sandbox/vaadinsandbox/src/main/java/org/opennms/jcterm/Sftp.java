/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
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
import java.io.*;

public class Sftp implements Runnable{
  InputStream in;
  OutputStream out;
  ChannelSftp c;

  private byte[] lf= {0x0a, 0x0d};
  private byte[] del= {0x08, 0x20, 0x08};

  public Sftp(ChannelSftp c, InputStream in, OutputStream out){
    this.c=c;
    this.in=in;
    this.out=out;
  }

  public void run(){
    try{
      java.util.Vector cmds=new java.util.Vector();
      byte[] buf=new byte[1024];
      int i;
      String str;
      String lhome=c.lpwd();

      StringBuffer sb=new StringBuffer();
      while(true){
        //out.print("sftp> ");
        out.write("sftp> ".getBytes());
        cmds.removeAllElements();

        sb.setLength(0);

        loop: while(true){
          i=in.read(buf, 0, 1024);
          if(i<=0)
            break;
          if(i!=1)
            continue;
          //System.out.println(Integer.toHexString(i)+" "+Integer.toHexString(buf[0]&0xff));
          if(buf[0]==0x08){
            if(sb.length()>0){
              sb.setLength(sb.length()-1);
              out.write(del, 0, del.length);
              out.flush();
            }
            continue;
          }

          if(buf[0]==0x0d){
            out.write(lf, 0, lf.length);
          }
          else if(buf[0]==0x0a){
            out.write(lf, 0, lf.length);
          }
          else if(buf[0]<0x20||(buf[0]&0x80)!=0){
            continue;
          }
          else{
            out.write(buf, 0, i);
          }
          out.flush();

          for(int j=0; j<i; j++){
            sb.append((char)buf[j]);
            if(buf[j]==0x0d){
              System
                  .arraycopy(sb.toString().getBytes(), 0, buf, 0, sb.length());
              i=sb.length();
              break loop;
            }
            if(buf[j]==0x0a){
              System
                  .arraycopy(sb.toString().getBytes(), 0, buf, 0, sb.length());
              i=sb.length();
              break loop;
            }
          }
        }
        if(i<=0)
          break;

        i--;
        if(i>0&&buf[i-1]==0x0d)
          i--;
        if(i>0&&buf[i-1]==0x0a)
          i--;
        //str=new String(buf, 0, i);
        //System.out.println("|"+str+"|");
        int s=0;
        for(int ii=0; ii<i; ii++){
          if(buf[ii]==' '){
            if(ii-s>0){
              cmds.addElement(new String(buf, s, ii-s));
            }
            while(ii<i){
              if(buf[ii]!=' ')
                break;
              ii++;
            }
            s=ii;
          }
        }
        if(s<i){
          cmds.addElement(new String(buf, s, i-s));
        }
        if(cmds.size()==0)
          continue;

        String cmd=(String)cmds.elementAt(0);
        if(cmd.equals("quit")){
          c.quit();
          break;
        }
        if(cmd.equals("exit")){
          c.exit();
          break;
        }
        if(cmd.equals("cd")||cmd.equals("lcd")){
          String path=null;
          if(cmds.size()<2){
            if(cmd.equals("cd"))
              path=c.getHome();
            else
              path=lhome;
          }
          else{
            path=(String)cmds.elementAt(1);
          }
          try{
            if(cmd.equals("cd"))
              c.cd(path);
            else
              c.lcd(path);
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("rm")||cmd.equals("rmdir")||cmd.equals("mkdir")){
          if(cmds.size()<2)
            continue;
          String path=(String)cmds.elementAt(1);
          try{
            if(cmd.equals("rm"))
              c.rm(path);
            else if(cmd.equals("rmdir"))
              c.rmdir(path);
            else
              c.mkdir(path);
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("lmkdir")){
          if(cmds.size()<2)
            continue;
          String path=(String)cmds.elementAt(1);

          java.io.File d=new java.io.File(c.lpwd(), path);
          if(!d.mkdir()){

            //System.out.println(e.getMessage());
            out.write("failed to make directory".getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }

        if(cmd.equals("chgrp")||cmd.equals("chown")||cmd.equals("chmod")){
          if(cmds.size()!=3)
            continue;
          String path=(String)cmds.elementAt(2);
          int foo=0;
          if(cmd.equals("chmod")){
            byte[] bar=((String)cmds.elementAt(1)).getBytes();
            int k;
            for(int j=0; j<bar.length; j++){
              k=bar[j];
              if(k<'0'||k>'7'){
                foo=-1;
                break;
              }
              foo<<=3;
              foo|=(k-'0');
            }
            if(foo==-1)
              continue;
          }
          else{
            try{
              foo=Integer.parseInt((String)cmds.elementAt(1));
            }
            catch(Exception e){
              continue;
            }
          }
          try{
            if(cmd.equals("chgrp")){
              c.chgrp(foo, path);
            }
            else if(cmd.equals("chown")){
              c.chown(foo, path);
            }
            else if(cmd.equals("chmod")){
              c.chmod(foo, path);
            }
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("pwd")||cmd.equals("lpwd")){
          str=(cmd.equals("pwd") ? "Remote" : "Local");
          str+=" working directory: ";
          if(cmd.equals("pwd"))
            str+=c.pwd();
          else
            str+=c.lpwd();
          //out.print(str+"\n");
          out.write(str.getBytes());
          out.write(lf);
          out.flush();
          continue;
        }
        if(cmd.equals("ls")||cmd.equals("dir")){
          String path=".";
          if(cmds.size()==2)
            path=(String)cmds.elementAt(1);
          try{
            java.util.Vector vv=c.ls(path);
            if(vv!=null){
              for(int ii=0; ii<vv.size(); ii++){
                //out.print(vv.elementAt(ii)+"\n");
                //out.write(((String)(vv.elementAt(ii))).getBytes());
                out.write(((String)(vv.elementAt(ii).toString())).getBytes());
                out.write(lf);
              }
              out.flush();
            }
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("lls")){
          String path=c.lpwd();
          if(cmds.size()==2)
            path=(String)cmds.elementAt(1);
          try{
            java.io.File d=new java.io.File(path);
            String[] list=d.list();
            for(int ii=0; ii<list.length; ii++){
              out.write(list[ii].getBytes());
              out.write(lf);
            }
            out.flush();
          }
          catch(IOException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("get")||cmd.equals("put")){
          if(cmds.size()!=2&&cmds.size()!=3)
            continue;
          String p1=(String)cmds.elementAt(1);
          //	  String p2=p1;
          String p2=".";
          if(cmds.size()==3)
            p2=(String)cmds.elementAt(2);
          try{
            SftpProgressMonitor monitor=new MyProgressMonitor(out);
            if(cmd.equals("get"))
              c.get(p1, p2, monitor);
            else
              c.put(p1, p2, monitor);
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("ln")||cmd.equals("symlink")||cmd.equals("rename")){
          if(cmds.size()!=3)
            continue;
          String p1=(String)cmds.elementAt(1);
          String p2=(String)cmds.elementAt(2);
          try{
            if(cmd.equals("rename"))
              c.rename(p1, p2);
            else
              c.symlink(p1, p2);
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          continue;
        }
        if(cmd.equals("stat")||cmd.equals("lstat")){
          if(cmds.size()!=2)
            continue;
          String p1=(String)cmds.elementAt(1);
          SftpATTRS attrs=null;
          try{
            if(cmd.equals("stat"))
              attrs=c.stat(p1);
            else
              attrs=c.lstat(p1);
          }
          catch(SftpException e){
            //System.out.println(e.getMessage());
            out.write(e.getMessage().getBytes());
            out.write(lf);
            out.flush();
          }
          if(attrs!=null){
            //out.println(attrs);
            out.write(attrs.toString().getBytes());
            out.write(lf);
            out.flush();
          }
          else{
          }
          continue;
        }
        if(cmd.equals("version")){
          //out.print("SFTP protocol version "+c.version()+"\n");
          out.write(("SFTP protocol version "+c.version()).getBytes());
          out.write(lf);
          out.flush();
          continue;
        }
        if(cmd.equals("help")||cmd.equals("help")){
          //	  out.print(help+"\n");
          for(int j=0; j<help.length; j++){
            out.write((help[j]).getBytes());
            out.write(lf);
          }
          out.flush();
          continue;
        }
        //out.print("unimplemented command: "+cmd+"\n");
        out.write(("unimplemented command: "+cmd).getBytes());
        out.write(lf);
        out.flush();
      }
      try{
        in.close();
      }
      catch(Exception ee){
      }
      try{
        out.close();
      }
      catch(Exception ee){
      }
    }
    catch(Exception e){
      System.out.println(e);
    }
  }

  private Thread thread=null;

  public void kick(){
    if(thread==null){
      thread=new Thread(this);
      thread.start();
    }
  }

  public static class MyProgressMonitor implements SftpProgressMonitor{
    OutputStream out;
    //    ProgressMonitor monitor;
    long count=0;
    long max=0;
    String src;
    int percent=0;

    MyProgressMonitor(OutputStream out){
      this.out=out;
    }

    public void init(int op, String src, String dest, long max){
      this.max=max;
      this.src=src;
      count=0;
      percent=0;
      status();
      //      monitor=new ProgressMonitor(null,
      //                                  ((op==SftpProgressMonitor.PUT)?
      //                                   "put" : "get")+": "+src,
      //                                  "",  0, (int)max);
      //      monitor.setProgress((int)this.count);
      //      monitor.setMillisToDecideToPopup(1000);
    }

    public boolean count(long count){
      this.count+=count;
      //      monitor.setProgress((int)this.count);
      //      monitor.setNote("Completed "+this.count+" out of "+max+".");
      //      return !(monitor.isCanceled());
      percent=(int)(((((float)this.count)/((float)max)))*100.0);
      status();
      return true;
    }

    public void end(){
      //      monitor.close();
      percent=(int)(((((float)count)/((float)max)))*100.0);
      status();
      try{
        out.write((byte)0x0d);
        out.write((byte)0x0a);
        out.flush();
      }
      catch(Exception e){
      }
    }

    private void status(){
      try{
        out.write((byte)0x0d);

        out.write((byte)0x1b);
        out.write((byte)'[');
        out.write((byte)'K');

        out.write((src+": "+percent+"% "+count+"/"+max).getBytes());
        out.flush();
      }
      catch(Exception e){
      }
    }
  }

  private static String[] help= {
      "      Available commands:",
      "      * means unimplemented command.",
      "cd [path]                     Change remote directory to 'path'",
      "lcd [path]                    Change local directory to 'path'",
      "chgrp grp path                Change group of file 'path' to 'grp'",
      "chmod mode path               Change permissions of file 'path' to 'mode'",
      "chown own path                Change owner of file 'path' to 'own'",
      "help                          Display this help text",
      "get remote-path [local-path]  Download file",
      "lls [path]                    Display local directory listing",
      "ln oldpath newpath            Symlink remote file",
      "lmkdir path                   Create local directory",
      "lpwd                          Print local working directory",
      "ls [path]                     Display remote directory listing",
      "*lumask umask                 Set local umask to 'umask'",
      "mkdir path                    Create remote directory",
      "put local-path [remote-path]  Upload file",
      "pwd                           Display remote working directory",
      "stat path                     Display info about path\n"
          +"exit                          Quit sftp",
      "quit                          Quit sftp",
      "rename oldpath newpath        Rename remote file",
      "rmdir path                    Remove remote directory",
      "rm path                       Delete remote file",
      "symlink oldpath newpath       Symlink remote file",
      "version                       Show SFTP version",
      "?                             Synonym for help"};
}
