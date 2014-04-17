package org.xjaf2x.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextField;
import xjaf2x.server.config.ServerConfig;
import xjaf2x.server.config.ServerConfig.Mode;

public class XJAF2x extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(XJAF2x.class.getName());
	private JTextField txtMas2j;

	public XJAF2x() throws Exception
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("XJAF 2.x");
		Rectangle bounds = Settings.instance().get("GUIBounds", null);
		if (bounds != null)
			setBounds(bounds);
		else
		{
			setSize(800, 600);
			setLocationRelativeTo(null);
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				Settings s = Settings.instance();
				s.set("GUIBounds", getBounds());
				s.set("mas2j", txtMas2j.getText());
				s.save();
			}
		});

		if (ServerConfig.getMode() == Mode.MASTER)
			ServerConfig.initCluster();
		getContentPane().add(new AgentCtrls());

		JPanel pnlCtrls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		getContentPane().add(pnlCtrls, BorderLayout.NORTH);

		txtMas2j = new JTextField();
		txtMas2j.setText(Settings.instance().get("mas2j", ""));
		txtMas2j.setColumns(10);
		pnlCtrls.add(txtMas2j);

		JButton btnJason = new JButton("Jason");
		btnJason.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new Thread() {
					@Override
					public void run()
					{
						try
						{
							String mas2j = txtMas2j.getText();
							jason.infra.centralised.RunCentralisedMAS.main(new String[] { mas2j });
						} catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}.start();
			}
		});
		pnlCtrls.add(btnJason);

		JButton btnMake = new JButton("Make");
		btnMake.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					makeMas2j(txtMas2j.getText());
				} catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		});
		pnlCtrls.add(btnMake);

		updateControls();
		setVisible(true);
	}

	private void updateControls()
	{
		/*
		 * btnStartAsMaster.setEnabled(!asMaster);
		 * btnStart.setEnabled(!running); btnStop.setEnabled(running);
		 */
		// TODO : implement updateControls
	}

	private void makeMas2j(String mas2j) throws IOException
	{
		String val = JOptionPane.showInputDialog(this, "Number of agents?");
		if (val == null)
			return;
		final int N = Integer.parseInt(val);

		try (PrintWriter out = new PrintWriter(mas2j))
		{
			out.println("MAS xjaf2x_jason {");
			out.println("infrastructure: xjaf2x");
			out.println("agents:");

			for (int i = 0; i < N; i++)
			{
				out.println("agent"
						+ i
						+ " sample_agent"
						+ " [ family = \"org.xjaf2x.server.agents.jason.FactJason\", numAgents = \""
						+ N + "\" ]" + " agentArchClass org.xjaf2x.client.jason.Xjaf2xAgArch;");
			}

			out.println("aslSourcePath:");

			String asl = "/" + mas2j.replace('\\', '/');
			int i = asl.lastIndexOf('/');
			asl = asl.substring(0, i + 1);

			out.println("\"" + asl + "\";");
			out.println("}");
		}
	}
	
	
	
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex)
		{
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable ex)
			{
				logger.log(Level.WARNING, "Uncaught exception", ex);
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				Settings.instance().save();
			}
		});
		
		try
		{
			new XJAF2x();
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Error while initializing XJAF 2.x", ex);
			System.exit(-1);
		}
	}
}
