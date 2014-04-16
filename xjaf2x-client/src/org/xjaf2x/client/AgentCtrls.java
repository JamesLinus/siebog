package org.xjaf2x.client;

import javax.swing.JPanel;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import org.xjaf2x.server.Global;
import org.xjaf2x.server.agentmanager.agent.AID;
import org.xjaf2x.server.messagemanager.fipaacl.ACLMessage;
import org.xjaf2x.server.messagemanager.fipaacl.Performative;
import javax.swing.DefaultListModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class AgentCtrls extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AgentCtrls.class.getName());
	private JTextField txtContent;
	private JComboBox<Performative> cbxPerf;
	private JTextField txtNewName;
	private DefaultListModel<String> mdlFamilies;
	private JList<String> lstFamilies;
	private JList<AID> lstRunning;
	private DefaultListModel<AID> mdlRunning;
	private JSpinner spnNumMsgs;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AgentCtrls()
	{
		setLayout(null);

		JPanel pnlPostMsg = new JPanel();
		pnlPostMsg.setBorder(new TitledBorder(null, " Send a message ", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlPostMsg.setBounds(10, 275, 395, 294);
		add(pnlPostMsg);
		pnlPostMsg.setLayout(null);

		JLabel lblPerformative = new JLabel("Performative:");
		lblPerformative.setBounds(16, 188, 66, 14);
		pnlPostMsg.add(lblPerformative);

		cbxPerf = new JComboBox(Performative.values());
		cbxPerf.setMaximumRowCount(18);
		cbxPerf.setBounds(16, 204, 160, 20);
		pnlPostMsg.add(cbxPerf);

		JLabel lblContent = new JLabel("Content:");
		lblContent.setBounds(186, 188, 46, 14);
		pnlPostMsg.add(lblContent);

		txtContent = new JTextField();
		txtContent.setBounds(186, 204, 160, 20);
		pnlPostMsg.add(txtContent);

		JLabel lblRecipient = new JLabel("Running agents:");
		lblRecipient.setBounds(16, 28, 89, 14);
		pnlPostMsg.add(lblRecipient);

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				final Performative p = (Performative) cbxPerf.getSelectedItem();
				final AID aid = (AID) lstRunning.getSelectedValue();
				final String content = txtContent.getText();
				final int n = (Integer) spnNumMsgs.getValue();
				for (int i = 0; i < n; i++)
				{
					new Thread() {
						@Override
						public void run()
						{
							// construct the message
							ACLMessage msg = new ACLMessage(p);
							msg.addReceiver(aid);
							msg.setContent(content);
							// go!
							try
							{
								Global.getMessageManager().post(msg);
							} catch (Exception ex)
							{
								logger.log(Level.WARNING, "Error while sending a message", ex);
							}
						}
					}.start();
					Thread.yield();
				}
			}
		});
		btnSend.setBounds(16, 260, 89, 23);
		pnlPostMsg.add(btnSend);
		
		JButton test = new JButton("Proba");
		test.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		test.setBounds(120, 260, 89, 23);
		pnlPostMsg.add(test);

		JScrollPane spRunning = new JScrollPane();
		spRunning.setBounds(16, 44, 362, 133);
		pnlPostMsg.add(spRunning);

		mdlRunning = new DefaultListModel<>();
		lstRunning = new JList(mdlRunning);
		spRunning.setViewportView(lstRunning);

		final JButton btnReloadRunning = new JButton("Reload");
		btnReloadRunning.setBounds(302, 20, 76, 23);
		btnReloadRunning.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					mdlRunning.clear();
					List<AID> aids = Global.getAgentManager().getRunning();
					for (AID aid : aids)
						mdlRunning.addElement(aid);
				} catch (Exception ex)
				{
					logger.log(Level.WARNING, "Error while reloading running agents", ex);
				}
			}
		});
		pnlPostMsg.add(btnReloadRunning);

		JLabel lblSend = new JLabel("Send");
		lblSend.setBounds(16, 235, 46, 14);
		pnlPostMsg.add(lblSend);

		spnNumMsgs = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		spnNumMsgs.setBounds(45, 232, 60, 20);
		pnlPostMsg.add(spnNumMsgs);

		JLabel lblMessagesInParallel = new JLabel("messages in parallel");
		lblMessagesInParallel.setBounds(115, 235, 95, 14);
		pnlPostMsg.add(lblMessagesInParallel);

		JPanel pnlRun = new JPanel();
		pnlRun.setBorder(new TitledBorder(null, " Run new agent ", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlRun.setBounds(10, 11, 395, 253);
		add(pnlRun);
		pnlRun.setLayout(null);

		JLabel lblAgentName = new JLabel("Agent runtime name:");
		lblAgentName.setBounds(16, 182, 122, 14);
		pnlRun.add(lblAgentName);

		txtNewName = new JTextField();
		txtNewName.setBounds(123, 179, 160, 20);
		pnlRun.add(txtNewName);
		txtNewName.setColumns(10);

		JLabel lblAgentFamily = new JLabel("Available families:");
		lblAgentFamily.setBounds(16, 28, 89, 14);
		pnlRun.add(lblAgentFamily);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 44, 362, 133);
		pnlRun.add(scrollPane);

		mdlFamilies = new DefaultListModel<>();
		lstFamilies = new JList(mdlFamilies);
		scrollPane.setViewportView(lstFamilies);

		final JButton btnReloadFamilies = new JButton("Reload");
		btnReloadFamilies.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				mdlFamilies.clear();
				try
				{
					List<String> families = Global.getAgentManager().getFamilies();
					for (String str : families)
						mdlFamilies.addElement(str);
				} catch (Exception ex)
				{
					logger.log(Level.WARNING, "Error while reloading agent families", ex);
				}
			}
		});
		btnReloadFamilies.setBounds(302, 20, 76, 23);
		pnlRun.add(btnReloadFamilies);

		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String runtimeName = txtNewName.getText();
					String family = (String) lstFamilies.getSelectedValue();
					if (family == null)
						return;
					// TODO : need an option to pass initialization arguments instead of null here
					Global.getAgentManager().startAgent(family, runtimeName, null);
				} catch (Exception ex)
				{
					logger.log(Level.WARNING, "Error while starting an agent", ex);
				} finally
				{
					EventQueue.invokeLater(new Runnable() {
						public void run()
						{
							btnReloadRunning.doClick();
						}
					});
				}
			}
		});
		btnRun.setBounds(289, 178, 89, 23);
		pnlRun.add(btnRun);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				btnReloadFamilies.doClick();
			}
		});
	}
}
