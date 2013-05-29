package com.github.abrarsyed.jastyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.github.abrarsyed.exceptions.MalformedOptionException;
import com.github.abrarsyed.jastyle.constants.EnumBracketMode;
import com.github.abrarsyed.jastyle.constants.EnumFormatStyle;
import com.github.abrarsyed.jastyle.constants.SourceMode;

public class OptParser
{
	public final ASFormatter	formatter;

	/**
	 * @param formatter The formatter to which the parsed options will apply. Options are parsed and applied immediately.
	 */
	public OptParser(ASFormatter formatter)
	{
		this.formatter = formatter;
	}

	/**
	 * A method for no other reason than to help throw the exception.
	 * This is used because its helps cut down boilerplate.
	 * it is is used instead of chaining return statements for booleans, and many unnecessary ifs.
	 * Options are parsed and applied immediately.
	 * @throws MalformedOptionException
	 */
	private static void error() throws MalformedOptionException
	{
		throw new MalformedOptionException();
	}
	
	/*
	 * TODO: MAKE THIS THE ACTUAL JAVADOC OF THE FILE.
	 * Read jAstyle options from a file <br>
	 * Read a file form the file system, it skips the lines that start with #<br>
	 * <p/>
	 * 
	 * <pre>
	 * A default options file may be used to set your favorite source style options.
	 * The command line options have precedence. If there is a conflict between a command line option and an option
	 * in the default options file, the command line option will be used.
	 * Artistic Style looks for this file in the following locations (in order):
	 * the file indicated by the --options= command line option;
	 * the file and directory indicated by the environment variable ARTISTIC_STYLE_OPTIONS (if it exists);
	 * This option file lookup can be disabled by specifying --options=none on the command line.
	 * Options may be set apart by new-lines, tabs, commas, or spaces.
	 * Long options in the options file may be written without the preceding '--'.
	 * Lines within the options file that begin with '#' are considered line-comments.
	 * Example of a default options file:
	 * <em>
	 * # this line is a comment
	 * --brackets=attach   # this is a line-end comment
	 * # long options can be written without the preceding '--'
	 * indent-switches     # cannot do this on the command line
	 * # short options must have the preceding '-'
	 * -t -p
	 * # short options can be concatenated together
	 * -M65Ucv
	 * </em>
	 * </pre>
	 * @param filename The name of the file that will be readed
	 * @return
	 * @throws IOException
	 */

	/**
	 * Parses an Astyle Options file.
	 * Unsupported, illegal, or malformed options will be logged and ignored.
	 * Options are parsed and applied immediately.
	 * @param file Options file to parse. Extension is irrelevant.
	 * @param log A logger to output stuff to. if this is null, the Global logger will be used.
	 * @return A list of all the errored lines. Empty list if there were no errored lines. NULL if something went wrong reading the file.
	 */
	public ArrayList<String> parseOptionFile(File file)
	{
		try
		{
			ArrayList<String> errors = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();

			while (line != null)
			{
				// comment or empty.
				if (!(line.isEmpty() || line.startsWith("#")))
				{
					try
					{
						parseOption(line.trim());
					}
					catch (MalformedOptionException ex)
					{
						errors.add(line);
					}
				}
				line = reader.readLine();
			}

			reader.close();
			return errors;
		}
		catch (Exception e)
		{
			// error? return null.
			return null;
		}
	}

	/**
	 * @param opt This string is assumed to be starting with one or two hyphens.
	 */
	public void parseOption(final String opt) throws MalformedOptionException
	{
		// long option
		if (opt.startsWith("--"))
		{
			parseLongOption(opt.replaceFirst("[-]{2}", ""));
		}
		// only 1? short option
		else if (opt.startsWith("-"))
		{
			parseShortOption(opt.replaceFirst("[-]", ""));
		}
		// must be a long-option from the file.
		// if its a file path from the command line, itl be handled outside here, after this returns false.
		else
		{
			parseLongOption(opt);
		}
	}

	private void parseLongOption(String opt) throws MalformedOptionException
	{
		if (opt.startsWith("--"))
			throw new IllegalArgumentException("Trying to parse long option " + opt + " while it still cotnains a -");

		String temp1;

		// Style checking
		if (opt.startsWith("style="))
		{
			// 6 = length of "style="
			temp1 = opt.substring(6);
			temp1 = temp1.toUpperCase();
			temp1 = temp1.replace("&", "");
			temp1 = temp1.replace("/", "");

			try
			{
				formatter.setFormattingStyle(EnumFormatStyle.valueOf(temp1));
			}
			catch (Exception e)
			{
				// no possible style. fail.
				error();
			}
		}

		// indent checking
		else if (opt.startsWith("indent="))
		{
			temp1 = opt.substring(7);

			// spaces.
			if (temp1.startsWith("spaces"))
			{
				formatter.setSpaceIndentation(getLongOptNum(temp1, 4));
			}

			// tabs.
			else if (temp1.startsWith("tab"))
			{
				formatter.setTabIndentation(getLongOptNum(temp1, 4), false);
			}
			else if (temp1.startsWith("force-tab"))
			{
				formatter.setTabIndentation(getLongOptNum(temp1, 4), true);
			}
		}

		// bracket checking
		else if (opt.startsWith("brackets="))
		{
			temp1 = opt.substring(9);
			temp1 = temp1.toUpperCase();

			try
			{
				formatter.setBracketFormatMode(EnumBracketMode.valueOf(temp1));
			}
			catch (Exception e)
			{
				// no possible Bracket Format mode. fail.
				error();
			}
		}
		
		// bracket checking
		else if (opt.startsWith("break-"))
		{
			temp1 = opt.substring(6);
			
			if (temp1.startsWith("blocks"))
			{
				formatter.setBreakBlocksMode(true);
				
				if (temp1.contains("=all"))
				{
					formatter.setBreakClosingHeaderBlocksMode(true);
				}
			}
			else if (temp1.equals("closing-brackets"))
			{
				formatter.setBreakClosingHeaderBracketsMode(true);
			}
			else if (temp1.equals("elseifs"))
			{
				formatter.setBreakElseIfsMode(true);
			}
		}
		
		// padding stuff
		else if (opt.startsWith("pad"))
		{
			temp1 = opt.substring(3);
			
			if (temp1.equals("oper"))
			{
				formatter.setOperatorPaddingMode(true);
			}
			else if (temp1.startsWith("paren"))
			{
				temp1 = temp1.substring(5);
				if (temp1.isEmpty())
				{
					formatter.setParensOutsidePaddingMode(true);
					formatter.setParensInsidePaddingMode(true);
				}
				else if (temp1.equals("out"))
				{
					formatter.setParensOutsidePaddingMode(true);
				}
				else if (temp1.equals("in"))
				{
					formatter.setParensInsidePaddingMode(true);
				}
				else
					error();
			}
		}
		else if (opt.equals("unpad-paren"))
		{
			formatter.setParensUnPaddingMode(true);
		}
		
		// source mode stuff
		if (opt.startsWith("mode="))
		{
			// 6 = length of "style="
			temp1 = opt.substring(5);
			temp1 = temp1.toUpperCase();

			try
			{
				formatter.setSourceStyle(SourceMode.valueOf(temp1));
			}
			catch (Exception e)
			{
				// no possible style. fail.
				error();
			}
		}
		
		
		// misc stuff.
		
		
		else if (opt.equals("delete-empty-lines"))
		{
			formatter.setDeleteEmptyLinesMode(true);
		}
		else if (opt.startsWith("keep-one-line"))
		{
			temp1 = opt.substring(13);
			
			if (temp1.equals("statements"))
			{
				formatter.setSingleStatementsMode(false);
			}
			else if (temp1.equals("blocks"))
			{
				formatter.setBreakOneLineBlocksMode(false);
			}
			else
				error();
		}
		
		
		else
		{
			error();
		}
	}

	/**
	 * This method is used to parse short options. Short options should be no less than 1 and no more than 5 characters long.
	 * @param opt Should not start with a hyphen.
	 * @return
	 */
	private void parseShortOption(String opt) throws MalformedOptionException
	{
		if (opt.startsWith("-"))
			throw new IllegalArgumentException("Trying to parse short option " + opt + " while it still cotnains a -");

		char optStart = opt.charAt(0);
		String start = "" + optStart;

		int tempNum;

		switch (optStart)
			{
			// style stuff
				case 'A':
					tempNum = getShortOptNum(start, opt, 0);
					if (tempNum >= EnumFormatStyle.values().length || tempNum < 0)
					{
						error();
					}
					formatter.setFormattingStyle(EnumFormatStyle.values()[tempNum]);
					break;

				// spaces
				case 's':
					formatter.setSpaceIndentation(getShortOptNum(start, opt, 4));
					break;

				// tabs
				case 't':
					formatter.setTabIndentation(getShortOptNum(start, opt, 4), false);
					break;
				case 'T':
					formatter.setTabIndentation(getShortOptNum(start, opt, 4), true);
					break;

				// brackets
				case 'b':
					formatter.setBracketFormatMode(EnumBracketMode.BREAK);
					break;
				case 'a':
					formatter.setBracketFormatMode(EnumBracketMode.ATTACH);
					break;
				case 'l':
					formatter.setBracketFormatMode(EnumBracketMode.LINUX);
					break;
				case 'u':
					formatter.setBracketFormatMode(EnumBracketMode.STROUSTRUP);
					break;
					
				// "break" options
				case 'f':
					formatter.setBreakBlocksMode(true);
					break;
				case 'F':
					formatter.setBreakBlocksMode(true);
					formatter.setBreakClosingHeaderBlocksMode(true);
					break;
				case 'y':
					formatter.setBreakClosingHeaderBracketsMode(true);
					break;
				case 'e':
					formatter.setBreakElseIfsMode(true);
					break;
					
					// X options.
				case 'x':
					if (opt.length() == 1)
					{
						formatter.setDeleteEmptyLinesMode(true);
						break;
					}
					// TODO: MORE OPTIONS STARTING IN X HERE
					
					// no possible matching x thingy
					else
						error();
					
				// perintheses and padding.
				case 'p' :
					formatter.setOperatorPaddingMode(true);
					break;
				case 'P' :
					formatter.setParensOutsidePaddingMode(true);
					formatter.setParensInsidePaddingMode(true);
					break;
				case 'd':
					formatter.setParensOutsidePaddingMode(true);
					break;
				case 'D':
					formatter.setParensInsidePaddingMode(true);
					break;
				case 'U':
					formatter.setParensUnPaddingMode(true);
					break;
					
				// misc stuff.
				case 'o':
					formatter.setSingleStatementsMode(false);
					break;
				case'O':
					formatter.setBreakOneLineBlocksMode(false);
					break;
					
			}

		// nothing else we can parse? throw de exception.
		error();
	}

	/**
	 * Parses a string matching pattern *=* and tries to convert the characters after the equal sign to an integer.
	 * @param str
	 * @param the default number to return if there is no equal sign.
	 * @return
	 * @throws MalformedOptionException
	 */
	private int getLongOptNum(String str, int def) throws MalformedOptionException
	{
		if (str.contains("="))
		{
			try
			{
				String[] split = str.split("=");
				return Integer.parseInt(split[1]);
			}
			catch (Exception e)
			{
				error();
			}
		}

		return def;
	}

	/**
	 * Parses a string matching pattern *# and tries to conver the # chars to a number given the * chars.
	 * @param start The section of the option occurring before the number.
	 * @param str The entire option.
	 * @param def The number to be returned if there is no number section.
	 * @return
	 * @throws MalformedOptionException
	 */
	private int getShortOptNum(String start, String str, int def) throws MalformedOptionException
	{
		if (!str.startsWith(start))
			throw new IllegalArgumentException("Param start must be the portion of param str before the number!");

		if (str.length() > start.length())
		{
			try
			{
				return Integer.parseInt(str.substring(start.length()));
			}
			catch (Exception e)
			{
				error();
			}
		}

		return def;
	}

	//...
}
