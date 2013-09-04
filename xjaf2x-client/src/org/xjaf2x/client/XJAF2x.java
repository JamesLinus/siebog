package org.xjaf2x.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Point;
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
import org.xjaf2x.client.gui.AgentCtrls;
import org.xjaf2x.client.gui.StartDlg;
import org.xjaf2x.client.jboss.CLI;
import javax.swing.JTextField;

public class XJAF2x extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(XJAF2x.class.getName());
	private JTextField txtMas2j;
	private JButton btnRedeployServer;
	private JButton btnStart;
	private AgentCtrls agentCtrls;
	private CLI cli;

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

		cli = new CLI();

		JPanel pnlCtrls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		getContentPane().add(pnlCtrls, BorderLayout.NORTH);

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				StartDlg dlg = new StartDlg(XJAF2x.this, cli);
				Point pt = btnStart.getLocationOnScreen();
				dlg.setLocation(pt.x + 8, pt.y + btnStart.getHeight() + 8);
				dlg.setVisible(true);
				// --> on close
				if (dlg.isOk())
				{
					agentCtrls = new AgentCtrls(cli.getMasterAddr());
					getContentPane().add(agentCtrls);
					revalidate();
					repaint();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run()
						{
							btnRedeployServer.doClick();
						}
					});
				}
			}
		});
		pnlCtrls.add(btnStart);

		btnRedeployServer = new JButton("Redeploy server");
		btnRedeployServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					redeployServer();
					if (agentCtrls != null)
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run()
							{
								agentCtrls.reloadFamilies();
							}
						});
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		pnlCtrls.add(btnRedeployServer);

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

	

	private void redeployServer() throws Exception
	{
		cli.redeployServer();
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
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				try
				{
					new XJAF2x();
				} catch (Exception ex)
				{
					logger.log(Level.SEVERE, "Error while initializing XJAF 2.x", ex);
				}
			}
		});
	}
}
