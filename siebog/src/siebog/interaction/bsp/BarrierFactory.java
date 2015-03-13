package siebog.interaction.bsp;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import org.infinispan.Cache;
import siebog.core.Global;
import siebog.utils.GlobalCache;
import siebog.utils.ObjectFactory;

@Singleton
@LocalBean
public class BarrierFactory {
	private static final String CACHE_NAME = "bsp-barrier-cache";
	private Cache<String, Barrier> cache;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void postConstruct() {
		cache = (Cache<String, Barrier>) GlobalCache.get().getCache(CACHE_NAME);
	}

	public Barrier getBarrier(String name) {
		Barrier barrier = cache.get(name);
		if (barrier != null)
			return barrier;
		return createAndStoreBarrier(name);
	}

	private Barrier createAndStoreBarrier(String name) {
		String lookup = "ejb:/" + Global.SIEBOG_MODULE + "//" + BarrierBean.class.getSimpleName()
				+ "!" + Barrier.class.getName() + "?stateful";
		Barrier barrier = ObjectFactory.lookup(lookup, Barrier.class);
		cache.put(name, barrier);
		return barrier;
	}
}
