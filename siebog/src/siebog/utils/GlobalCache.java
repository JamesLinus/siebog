package siebog.utils;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import siebog.agents.AID;
import siebog.agents.Agent;
import siebog.jasonee.control.ExecutionControl;
import siebog.jasonee.environment.Environment;

public class GlobalCache {
	private static final String CACHE_CONTAINER = "java:jboss/infinispan/container/siebog-cache";
	private static GlobalCache instance;
	private CacheContainer cacheContainer;
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
		cacheContainer = ObjectFactory.lookup(CACHE_CONTAINER, CacheContainer.class);
	}

	public Cache<AID, Agent> getRunningAgents() {
		return cacheContainer.getCache(RUNNING_AGENTS);
	}

	public Cache<String, ExecutionControl> getExecutionControls() {
		return cacheContainer.getCache(EXECUTION_CONTROLS);
	}

	public Cache<String, Environment> getEnvironments() {
		return cacheContainer.getCache(ENVIRONMENTS);
	}

	public Cache<?, ?> getCache(String name) {
		return cacheContainer.getCache(name);
	}
}
