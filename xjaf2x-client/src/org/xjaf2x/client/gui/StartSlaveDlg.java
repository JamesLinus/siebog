package org.xjaf2x.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class StartSlaveDlg extends JDialog implements DocumentListener
{
	private static final long serialVersionUID = 1L;
	private JTextField txtMyAddr;
	private JTextField txtMyName;
	private JTextField txtMasterAddr;
	private JButton btnStart;
	private boolean ok;

	public StartSlaveDlg(JFrame owner, String myAddr, String myName, String msAddr)
	{
		super(owner, "Start as Slave", true);
		setSize(400, 282);
		setLocationRelativeTo(owner);

		setTitle("XJAF 2.x admin");
		getContentPane().setLayout(null);

		JLabel lblMyAddr = new JLabel(Strings.MY_ADDR);
		lblMyAddr.setBounds(10, 11, 228, 28);
		getContentPane().add(lblMyAddr);

		txtMyAddr = new JTextField(myAddr);
		txtMyAddr.setBounds(10, 41, 228, 20);
		txtMyAddr.getDocument().addDocumentListener(this);
		getContentPane().add(txtMyAddr);

		JLabel lblMsAddr = new JLabel(Strings.MS_ADDR);
		lblMsAddr.setBounds(10, 70, 228, 14);
		getContentPane().add(lblMsAddr);

		txtMasterAddr = new JTextField(msAddr);
		txtMasterAddr.setBounds(10, 86, 228, 20);
		txtMasterAddr.getDocument().addDocumentListener(this);
		getContentPane().add(txtMasterAddr);

		JLabel lblMyName = new JLabel(Strings.MY_NAME);
		lblMyName.setBounds(10, 117, 46, 14);
		getContentPane().add(lblMyName);

		txtMyName = new JTextField(myName);
		txtMyName.setBounds(10, 133, 228, 20);
		txtMyName.getDocument().addDocumentListener(this);
		getContentPane().add(txtMyName);

		btnStart = new JButton("Start");
		btnStart.setBounds(10, 211, 89, 23);
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ok = true;
				setVisible(false);
			}
		});
		getContentPane().add(btnStart);

		updateControls();
	}

	private void updateControls()
	{
		btnStart.setEnabled((getMyAddr().length() > 0) && (getMyName().length() > 0)
				&& (getMasterAddr().length() > 0));
	}

	public String getMyAddr()
	{
		return txtMyAddr.getText().trim();
	}

	public String getMyName()
	{
		return txtMyName.getText().trim();
	}

	public String getMasterAddr()
	{
		return txtMasterAddr.getText().trim();
	}

	public boolean isOk()
	{
		return ok;
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
}
