/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 20012 ymnk, JCraft,Inc.
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

import java.awt.Color;
import java.util.Vector;

/**
 * This class abstracts settings from JCTerm.
 * - font size
 * - pairs of foreground and background color
 * - list of destinations for the prompt.
 *
 * @see com.jcraft.jcterm.ConfigurationRepository
 */
public class Configuration {
  public static int FONT_SIZE = 14;
  public static String[] FG_BG = {"#000000:#ffffff", "#ffffff:#000000"};
  public static String[] DESTINATIONS = new String[0];

  public String name = "default";
  public int font_size = FONT_SIZE;
  public String[] fg_bg = FG_BG.clone();
  public String[] destinations = DESTINATIONS;

  public synchronized void addDestination(String d){
    destinations = add(d, destinations);
  }

  public synchronized void addFgBg(String d){
    fg_bg = add(d, fg_bg);
  }

  private String[] add(String d, String[]array){
    int i=0;
    while(i<array.length){
      if(d.equals(array[i])){
        if(i!=0){
          System.arraycopy(array, 0, array, 1, i);
          array[0]=d;
        }  
        return array;
      }
      i++;
    }
    String[] foo = new String[array.length+1];
    if(array.length>0){
      System.arraycopy(array, 0, foo, 1, array.length);
    }
    foo[0]=d;
    array=foo;
    return array;
  }

  static String[] parseDestinations(String d){
    String[] tmp = d.split(",");
    if(tmp.length==1 && tmp[0].length()==0)
      tmp = new String[0];
    return tmp;
  }

  static String[] parseFgBg(String fg_bg){
    Vector<String> v = new Vector<String>();
    String[] _fg_bg = fg_bg.split(",");
    for(int i=0; i < _fg_bg.length; i++){
      String[] tmp = _fg_bg[i].split(":");
      if(tmp.length!=2)
        continue;
      Color fg = JCTermSwing.toColor(tmp[0]);
      Color bg = JCTermSwing.toColor(tmp[1]);
      if(fg!=null && bg!=null){ 
        v.addElement(_fg_bg[i]);
      }
    }
    if(v.size()==0) return null;
    return v.toArray(new String[0]);
  }
}
