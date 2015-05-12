package siebog.starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import siebog.starter.FileUtils;

public class FileUtils {
	public static String read(File file) throws IOException {
		return read(new FileInputStream(file));
	}

	public static String read(String resource, int dummy) throws IOException {
		return read(FileUtils.class.getResourceAsStream(resource));
	}

	public static String read(InputStream in) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			StringBuilder str = new StringBuilder(in.available());
			String line;
			String nl = "";
			while ((line = reader.readLine()) != null) {
				str.append(nl);
				if (nl.length() == 0)
					nl = "\n";
				str.append(line);
			}
			return str.toString();
		}
	}

	public static void write(File file, String data) throws IOException {
		try (PrintWriter out = new PrintWriter(file)) {
			out.print(data);
		}
	}

	public static File createTempFile(String data) throws IOException {
		File f = File.createTempFile("siebog", null);
		if (data != null)
			write(f, data);
		return f;
	}
}
