package siebog.core.jasonee;

import jason.infra.InfrastructureFactory;
import jason.jeditplugin.MASLauncherInfraTier;
import jason.runtime.RuntimeServicesInfraTier;
import java.util.Arrays;
import javax.swing.JOptionPane;

public class SiebogInfrastructureFactory implements InfrastructureFactory {

	public SiebogInfrastructureFactory() {
		JOptionPane.showMessageDialog(null, "Hello!");
	}

	@Override
	public MASLauncherInfraTier createMASLauncher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RuntimeServicesInfraTier createRuntimeServices() {
		// TODO Auto-generated method stub
		return null;
	}

}
