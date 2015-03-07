package siebog.interaction.bsp;

import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import siebog.core.Global;
import siebog.utils.ObjectFactory;

/**
 * A HA singleton factory, assures that there is only a single Barrier instance per cluster.
 * 
 * @author <a href="mitrovic.dejan@gmail.com">Dejan Mitrovic</a>
 */
public class BarrierService implements Service<Barrier> {
	private static final Logger LOG = LoggerFactory.getLogger(BarrierService.class);
	public static final ServiceName DEFAULT_SERVICE_NAME = ServiceName.JBOSS.append(
			Global.SIEBOG_MODULE, "bsp", "barrier");
	private AtomicBoolean started = new AtomicBoolean(false);
	private Barrier barrier;

	@Override
	public Barrier getValue() throws IllegalStateException, IllegalArgumentException {
		if (!started.get())
			throw new IllegalStateException("BSP Barrier service not running.");
		return barrier;
	}

	public Barrier getBarrier(String id) {
		return null;
	}

	@Override
	public void start(StartContext context) throws StartException {
		if (!started.compareAndSet(false, true)) {
			throw new StartException("BSP Barrier service already running.");
		}
		final String lookup = "ejb:/" + Global.SIEBOG_MODULE + "//"
				+ BarrierBean.class.getSimpleName() + "!" + Barrier.class.getName();
		barrier = ObjectFactory.lookup(lookup, Barrier.class);
		LOG.info("BSP Barrier started.");
	}

	@Override
	public void stop(StopContext context) {
		if (started.compareAndSet(true, false)) {
			barrier.remove();
			barrier = null;
			LOG.info("BSP Barrier stopped.");
		}
	}
}
