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
