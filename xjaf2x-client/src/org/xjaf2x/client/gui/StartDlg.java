package org.xjaf2x.client.gui;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.xjaf2x.client.Settings;
import org.xjaf2x.client.jboss.CLI;
import org.xjaf2x.server.ClusterManager;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class StartDlg extends JDialog implements ActionListener, DocumentListener
{
	private static final Logger logger = Logger.getLogger(StartDlg.class.getName());
	private JTextField txtMyAddr;
	private JTextField txtMasterAddr;
	private JPanel pnlMaster;
	private JButton btnStart;
	private JRadioButton rbSlave;
	private JRadioButton rbMaster;
	private JButton btnInit;
	private JPanel pnlCluster;
	private JLabel lblNetworkAddressOf;
	private JTextField txtSlaveAddr;
	protected boolean ok;
	private JLabel lblWaitStart;
	private JLabel lblWaitCluster;
	private boolean loading;

	public StartDlg(final JFrame owner, final CLI cli)
	{
		super(owner, "Start XJAF 2.x", true);
		setResizable(false);
		setBounds(100, 100, 375, 386);
		getContentPane().setLayout(null);

		loading = true;
		initGui();

		ButtonGroup grp = new ButtonGroup();
		txtSlaveAddr.setText(Settings.instance().get("StartSlaveAddr", ""));

		btnInit = new JButton("Initialize cluster");
		btnInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnInit.setEnabled(false);
				lblWaitCluster.setVisible(true);
				ok = initCluster(owner);
				updateControls();
			}
		});
		btnInit.setBounds(8, 62, 130, 23);
		pnlCluster.add(btnInit);

		lblWaitCluster = new JLabel(
				"Please wait for the cluster to be initialized, and then close this dialog.");
		lblWaitCluster.setVisible(false);
		lblWaitCluster.setBounds(8, 85, 331, 14);
		pnlCluster.add(lblWaitCluster);

		JPanel panel = new JPanel();
		panel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) Color.GRAY));
		panel.setBounds(0, 0, 369, 64);
		getContentPane().add(panel);
		panel.setLayout(null);
		JLabel lbl1 = new JLabel("Network address of this computer:");
		lbl1.setBounds(8, 8, 166, 14);
		panel.add(lbl1);

		txtMyAddr = new JTextField();
		// previous input
		txtMyAddr.setText(Settings.instance().get("StartMyAddr", ""));
		txtMyAddr.setBounds(8, 22, 166, 20);
		txtMyAddr.setColumns(10);
		txtMyAddr.getDocument().addDocumentListener(this);
		panel.add(txtMyAddr);

		JLabel lbl2 = new JLabel("E.g.: node, node.domain.lan, 192.168.0.1, etc.");
		lbl2.setBounds(8, 42, 264, 14);
		panel.add(lbl2);
		lbl2.setForeground(Color.GRAY);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new MatteBorder(0, 0, 1, 0, (Color) Color.GRAY));
		panel_1.setBounds(0, 66, 369, 182);
		getContentPane().add(panel_1);
		grp.add(rbMaster);
		panel_1.setLayout(null);

		rbMaster = new JRadioButton("Start as the Master node");
		rbMaster.setBounds(8, 8, 145, 23);
		panel_1.add(rbMaster);
		rbMaster.setSelected(true);
		rbMaster.addActionListener(this);
		grp.add(rbSlave);

		rbSlave = new JRadioButton("Start as a Slave node");
		rbSlave.setBounds(8, 65, 129, 23);
		rbSlave.addActionListener(this);
		panel_1.add(rbSlave);

		JLabel lbl3 = new JLabel("<html>Master node is used to control the entire cluster. "
				+ "Use this option also if this computer does not belong to a cluster.");
		lbl3.setBounds(29, 32, 278, 26);
		panel_1.add(lbl3);
		lbl3.setForeground(Color.GRAY);

		pnlMaster = new JPanel();
		pnlMaster.setBounds(29, 89, 193, 51);
		panel_1.add(pnlMaster);
		pnlMaster.setLayout(null);

		txtMasterAddr = new JTextField();
		txtMasterAddr.setColumns(10);
		txtMasterAddr.setBounds(0, 30, 180, 20);
		txtMasterAddr.getDocument().addDocumentListener(this);
		txtMasterAddr.setText(Settings.instance().get("StartMasterAddr", ""));
		pnlMaster.add(txtMasterAddr);

		JLabel lbl4 = new JLabel(
				"<html>Slaves cannot exist without the Master.<br />Please specify the Master address here:");
		lbl4.setBounds(0, 0, 254, 29);
		pnlMaster.add(lbl4);

		btnStart = new JButton("Start the server");
		btnStart.setBounds(8, 151, 109, 23);
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				btnStart.setEnabled(false);
				String str = rbMaster.isSelected() ? "." : ", and then close this dialog.";
				lblWaitStart.setText(lblWaitStart.getText() + str);
				lblWaitStart.setVisible(true);

				// save input
				Settings.instance().set("StartMyAddr", txtMyAddr.getText());
				Settings.instance().set("StartMasterAddr", txtMasterAddr.getText());
				Settings.instance().set("StartSlaveAddr", txtSlaveAddr.getText());
				Settings.instance().set("StartAsSlave", rbSlave.isSelected() ? "1" : "0");

				if (start(owner, cli))
				{
					if (rbMaster.isSelected())
						pnlCluster.setVisible(true);
					else
						ok = true;
				}
				updateControls();
			}
		});
		panel_1.add(btnStart);

		lblWaitStart = new JLabel("Please wait for the server to start");
		lblWaitStart.setBounds(127, 155, 180, 14);
		panel_1.add(lblWaitStart);
		lblWaitStart.setVisible(false);
		if ("1".equals(Settings.instance().get("StartAsSlave", "")))
			rbSlave.setSelected(true);

		loading = false;
		updateControls();
	}

	private boolean start(JFrame owner, CLI cli)
	{
		try
		{
			if (rbMaster.isSelected())
				cli.runMaster(txtMyAddr.getText());
			else
			{
				String sn = "xjaf2x-slave@" + txtMyAddr.getText();
				cli.runSlave(txtMyAddr.getText(), sn, txtMasterAddr.getText());
			}
			return true;
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Unable to start JBoss", ex);
			String msg = "The server has failed to start with the following error message:\n"
					+ ex.getClass().getName() + ": " + ex.getMessage() + "\n\n"
					+ "Please see the server log for more details.";
			JOptionPane.showMessageDialog(owner, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private boolean initCluster(JFrame owner)
	{
		List<String> addr = new ArrayList<>();
		addr.add(txtMyAddr.getText());
		final String[] slaves = txtSlaveAddr.getText().split(",");
		if ((slaves != null) && (slaves.length > 0))
			addr.addAll(Arrays.asList(slaves));

		try
		{
			ClusterManager.init(addr);
			return true;
		} catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Unable to initialize cluster", ex);
			String msg = "Cluster initialization has failed with the following error message:\n"
					+ ex.getClass().getName() + ": " + ex.getMessage() + "\n\n"
					+ "Please see the server log for more details.";
			JOptionPane.showMessageDialog(owner, msg, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void updateControls()
	{
		if (loading)
			return;
		btnStart.setEnabled((txtMyAddr.getText().length() > 0)
				&& (rbMaster.isSelected() || (txtMasterAddr.getText().length() > 0)));
		btnInit.setEnabled(rbMaster.isSelected());
	}

	private void initGui()
	{

		pnlCluster = new JPanel();
		pnlCluster.setBounds(0, 250, 369, 107);
		getContentPane().add(pnlCluster);
		pnlCluster.setLayout(null);

		lblNetworkAddressOf = new JLabel(
				"<html>Once you have started all Slave nodes, please specify<br />a comma-separated list of their addresses here:");
		lblNetworkAddressOf.setBounds(8, 8, 260, 25);
		pnlCluster.add(lblNetworkAddressOf);

		txtSlaveAddr = new JTextField();
		txtSlaveAddr.setBounds(8, 34, 260, 20);
		pnlCluster.add(txtSlaveAddr);
		txtSlaveAddr.setColumns(10);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateControls();
	}

	@Override
	public void insertUpdate(DocumentEvent e)
	{
		updateControls();
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{
		updateControls();
	}

	@Override
	public void changedUpdate(DocumentEvent e)
	{
	}

	public boolean isOk()
	{
		return ok;
	}
}
