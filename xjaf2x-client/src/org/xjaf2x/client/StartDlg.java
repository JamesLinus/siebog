package org.xjaf2x.client;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class StartDlg extends JDialog
{
	private JTextField txtMaster;
	private JTextField txtSlave;
	private JButton btnConnect;
	private boolean ok;

	public StartDlg()
	{
		setResizable(false);
		setModal(true);
		setSize(204, 166);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		setTitle("Connect to XJAF 2.x");

		JLabel lblNetworkAddressOf = new JLabel("Network address of the master node:");
		lblNetworkAddressOf.setBounds(10, 11, 180, 14);
		getContentPane().add(lblNetworkAddressOf);

		txtMaster = new JTextField();
		txtMaster.setBounds(10, 26, 180, 20);
		getContentPane().add(txtMaster);
		txtMaster.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				updateControls();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				updateControls();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
			}
		});

		JLabel lblNetworkAddressOf_1 = new JLabel("Network address of a slave node:");
		lblNetworkAddressOf_1.setBounds(10, 57, 180, 14);
		getContentPane().add(lblNetworkAddressOf_1);

		txtSlave = new JTextField();
		txtSlave.setBounds(10, 72, 180, 20);
		getContentPane().add(txtSlave);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				Settings.instance().set("MasterNodeAddr", getMaster());
				Settings.instance().set("SlaveNodeAddr", getSlave());
				ok = true;
				setVisible(true);
			}
		});
		btnConnect.setBounds(10, 103, 180, 23);
		getContentPane().add(btnConnect);

		txtMaster.setText(Settings.instance().get("MasterNodeAddr", ""));
		txtSlave.setText(Settings.instance().get("SlaveNodeAddr", ""));
		updateControls();
	}

	private void updateControls()
	{
		btnConnect.setEnabled(getMaster().length() > 0);
	}

	public boolean isOk()
	{
		return ok;
	}

	public String getMaster()
	{
		return txtMaster.getText().trim();
	}

	public String getSlave()
	{
		return txtSlave.getText().trim();
	}
}
