package org.xjaf2x.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Settings
{
	// TODO : read these from a config file
	public static final int FACILITATOR_PORT = 9123;
	private static Settings instance;
	// saved values
	private Map<String, Object> config;
	private String configFile;
	
	public static synchronized Settings instance()
	{
		if (instance == null)
			instance = new Settings();
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private Settings()
	{
		// load saved values
		configFile = System.getProperty("user.home").replace("\\", "/");
		if (!configFile.endsWith("/"))
			configFile += "/";
		configFile += ".xjaf2x.dat";
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(configFile)))
		{
			config = (Map<String, Object>) in.readObject();
		} catch (Exception ex)
		{
			config = new HashMap<>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T def)
	{
		if (config.containsKey(key))
			return (T) config.get(key);
		return def;
	}
	
	public void set(String key, Object value)
	{
		config.put(key, value);
	}
	
	public void save()
	{
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(configFile)))
		{
			out.writeObject(config);
		} catch (Exception ex)
		{
		}
	}
	
	@SuppressWarnings("unused")
	private String getRootFolder()
	{		
		String root = "";
		java.security.CodeSource codeSource = Settings.class.getProtectionDomain().getCodeSource();
		try
		{
			String path = codeSource.getLocation().toURI().getPath();
			File jarFile = new File(path);
			if (path.lastIndexOf(".jar") > 0)
				root = jarFile.getParentFile().getPath();
			else
				root = jarFile.getParentFile().getParentFile().getPath(); // get out of build/classes
		} catch (Exception ex)
		{
		}
		root = root.replace('\\', '/');
		if (!root.endsWith("/"))
			root += "/";
		return root;
	}
	
	public static boolean isJar()
	{
		java.security.CodeSource codeSource = Settings.class.getProtectionDomain().getCodeSource();
		String path = "";
		try
		{
			path = codeSource.getLocation().toURI().getPath();
		} catch (Exception ex)
		{
		}
		return path.lastIndexOf(".jar") > 0;
	}
}
