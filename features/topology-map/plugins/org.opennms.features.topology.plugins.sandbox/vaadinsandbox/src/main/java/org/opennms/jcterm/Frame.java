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

interface Frame {
  final int SHELL=0;
  final int SFTP=1;
  final int EXEC=2;

  void openFrame(int mode, String configName);
  void setTitle(String name);
  void setVisible(boolean visible);
  void setSize(int w, int h);
  int getWidth();
  int getHeight();
  void dispose();
}