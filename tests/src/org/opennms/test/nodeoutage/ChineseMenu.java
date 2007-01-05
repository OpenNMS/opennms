//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.test.nodeoutage;

/**
 * This class is package only, because I haven't decided where it should live
 */
final class ChineseMenu
{
	private String[] _menuOpts;

	public ChineseMenu(String[] menuOpts)
	{
		this._menuOpts = menuOpts;
	}

	public int numberOfOptions()
	{
		return this._menuOpts.length;
	}
	
	public int numberOfPossibilities()
	{
		return (int)Math.pow(2, this.numberOfOptions());
	}

	public String getPossibilityAsString(int i) throws IndexOutOfBoundsException
	{
		if (i > this.numberOfPossibilities())
			throw new IndexOutOfBoundsException(i + " is outside the range of possibilities.");

		String ret="";

		boolean[] poss = this.getPossibilityAt(i);


		for (int j=0; j < poss.length; j++)
		{
			ret += this.getOption(j) + "=" + poss[j] + "; ";
		}

		return ret;
	}

	public boolean[] getPossibilityAt(int i) throws IndexOutOfBoundsException
	{
		if (i > this.numberOfPossibilities())
			throw new IndexOutOfBoundsException(i + " is outside the range of possibilities.");

		boolean[] ret = new boolean[this.numberOfOptions()];

		String bstr = Integer.toBinaryString(i);
		for (int j=bstr.length(); j < this.numberOfOptions(); j++)
		{
			bstr = "0" + bstr;
		}
		for (int j=0; j < this.numberOfOptions(); j++)
		{
			if (bstr.charAt(j) == '0')
				ret[j] = false;
			else if (bstr.charAt(j) == '1')
				ret[j] = true;
		}

		return ret;
	}
	
	public String getOption(int i)
	{
		return this._menuOpts[i];
	}

	public void dump(java.io.PrintStream out) throws java.io.IOException
	{
		out.print("ID");
		out.print("\t\t");
		for (int i=0; i < this.numberOfOptions(); i++)
		{
			out.print(this.getOption(i));
			out.print("\t\t");
		}
		out.println();
		
		for (int j=0; j < this.numberOfPossibilities(); j++)
		{
			out.print(j);
			out.print("\t\t");

			boolean[] poss = this.getPossibilityAt(j);
			for (int k=0; k < this.numberOfOptions(); k++)
			{
				out.print(poss[k]);
				out.print("\t\t");
			}
			out.println();
		}
	}
	
	public static void main(String[] args) throws java.io.IOException
	{
		ChineseMenu menu = new ChineseMenu(args);
		
		menu.dump(System.out);
	}
}
