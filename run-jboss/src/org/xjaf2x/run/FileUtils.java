package org.xjaf2x.run;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileUtils
{
	public static String read(String fileName) throws IOException
	{
		return read(new FileInputStream(fileName));
	}
	
	public static String read(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try
		{
			StringBuilder str = new StringBuilder(in.available());
			String line;
			String nl = "";
			while ((line = reader.readLine()) != null)
			{
				str.append(nl);
				if (nl.length() == 0)
					nl = "\n";
				str.append(line);
			}
			return str.toString();
		} finally
		{
			try
			{
				reader.close();
			} catch (IOException ex)
			{
			}
		}
	}
	
	public static void write(String fileName, String data) throws IOException
	{
		PrintWriter out = new PrintWriter(fileName);
		out.print(data);
		out.close();
	}
}
