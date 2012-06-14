/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
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
import java.io.*;
import java.util.Vector;

/**
 * This class will save the configuration into  ~/.ssh/jcterm.properties
 * file in property file format.
 *
 * @see com.jcraft.jcterm.Configuration
 * @see com.jcraft.jcterm.ConfigurationRepository
 */
public class ConfigurationRepositoryFS implements ConfigurationRepository {

  private File ssh_home = new File(System.getProperty("user.home"), ".ssh");
  private File jcterm_prop = new File(ssh_home, "jcterm.properties");

  public Configuration load(String name){
    Configuration conf = new Configuration();
    conf.name = name;

    java.util.Properties prop = new java.util.Properties();
    InputStream in = null;

    try{
      in = new FileInputStream(jcterm_prop);
      prop.load(in);

      String key="jcterm."+name+".font_size";
      if(prop.get(key)!=null){
        try{
          conf.font_size = Integer.parseInt((String)prop.get(key));
        }
        catch(Exception ee){
          // ignore it because of loading incompatible data.
        }
      }

      try{
        key = "jcterm."+name+".fg_bg";
        if(prop.get(key) != null)
          conf.fg_bg = ((String)prop.get(key)).split(",");
      }
      catch(Exception ee){
        // ignore it because of loading incompatible data.
      }

      try{
        key = "jcterm."+name+".destination";
        if(prop.get(key) != null)
          conf.destinations = ((String)prop.get(key)).split(",");
      }
      catch(Exception ee){
        // ignore it because of loading incompatible data.
      }

      in.close();
    }
    catch(Exception e){
      // the file does not exist.
    }

    return conf;
  }

  public void save(Configuration conf){
    java.util.Properties prop = new java.util.Properties();
    InputStream in = null;
    try{
      in = new FileInputStream(jcterm_prop);
      prop.load(in);
      in.close();
    }
    catch(IOException e){
      // the file does not exist.
    }

    String name = conf.name;

    prop.setProperty("jcterm."+name+".destination", join(conf.destinations));

    prop.setProperty("jcterm."+name+".font_size",
                     new Integer(conf.font_size).toString());

    prop.setProperty("jcterm."+name+".fg_bg", join(conf.fg_bg));

    try{
     OutputStream out = new FileOutputStream(jcterm_prop);
     prop.store(out, "");
     out.close();
    }
    catch(IOException e){
      // failed to save file.
    }
  }

  String join(String[] array){
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      builder.append(array[i]);
      if(i+1<array.length)
        builder.append(",");
    }
    return builder.toString();
  }
}
