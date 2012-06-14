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

/**
 * This interface abstracts where the configuration is stored to and
 * retrived from.
 *
 * @see com.jcraft.jcterm.Configuration
 * @see com.jcraft.jcterm.ConfigurationRepositoryFS
 */
public interface ConfigurationRepository {
  Configuration load(String name);
  void save(Configuration conf);
}
