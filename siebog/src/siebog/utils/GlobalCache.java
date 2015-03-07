package siebog.utils;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import siebog.xjaf.core.AID;
import siebog.xjaf.core.Agent;

public class GlobalCache {
	private EmbeddedCacheManager manager;
	private static GlobalCache instance;
	private static final String RUNNING_AGENTS = "running-agents";
	private static final String EXECUTION_CONTROLS = "execution-controls";
	private static final String ENVIRONMENTS = "environments";

	public static GlobalCache get() {
		if (instance == null) {
			synchronized (GlobalCache.class) {
				if (instance == null)
					instance = new GlobalCache();
			}
		}
		return instance;
	}

	private GlobalCache() {
		manager = new DefaultCacheManager(true);
		createConfigurations();
	}

	public Cache<AID, Agent> getRunningAgents() {
		return manager.getCache(RUNNING_AGENTS);
	}

	private void createConfigurations() {
		manager.defineConfiguration(RUNNING_AGENTS, getConfig());
		manager.defineConfiguration(EXECUTION_CONTROLS, getConfig());
		manager.defineConfiguration(ENVIRONMENTS, getConfig());
	}

	private Configuration getConfig() {
		return new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build();
	}
}
