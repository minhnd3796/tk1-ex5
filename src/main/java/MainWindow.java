import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class MainWindow extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> historyListModel;
	private JList<String> historyList;

	private JLabel snapshot1Label;
	private JButton snapshot1Button;

	private JLabel snapshot2Label;
	private JButton snapshot2Button;

	private JLabel snapshot3Label;
	private JButton snapshot3Button;

	public static void main(String[] args) {
		final int H12PORT = 9612;
		final int H13PORT = 9613;
		final int H21PORT = 9621;
		final int H23PORT = 9623;
		final int H31PORT = 9631;
		final int H32PORT = 9632;

		MainWindow mainWindow = new MainWindow();
		mainWindow.setVisible(true);
		try {
			// create 6 communication channels
			// each hangar has 2 channels
			ServerSocket channel12 = new ServerSocket(H12PORT);
			ServerSocket channel13 = new ServerSocket(H13PORT);
			ServerSocket channel21 = new ServerSocket(H21PORT);
			ServerSocket channel23 = new ServerSocket(H23PORT);
			ServerSocket channel31 = new ServerSocket(H31PORT);
			ServerSocket channel32 = new ServerSocket(H32PORT);

			// create 3 hangars
			// each hangar can send to 2 other hangars
			// so each hangar can use 2 communication channel
			// to send planes to 2 other hangars
			Hangar hangar1 = new Hangar("H1", H12PORT, H13PORT);
			Hangar hangar2 = new Hangar("H2", H21PORT, H23PORT);
			Hangar hangar3 = new Hangar("H3", H31PORT, H32PORT);

			// there are 6 threads for 6 communication channels
			// each thread get the messgage out of the communication stream
			// and put it into the corresponding message queue
			// each queue of each hangar should be received messages from 2 other channels (hangars)
			HangarReceiver channel12Thread = new HangarReceiver(channel12, hangar2.getReceivedMessageQueue());
			HangarReceiver channel13Thread = new HangarReceiver(channel13, hangar3.getReceivedMessageQueue());
			HangarReceiver channel21Thread = new HangarReceiver(channel21, hangar1.getReceivedMessageQueue());
			HangarReceiver channel23Thread = new HangarReceiver(channel23, hangar3.getReceivedMessageQueue());
			HangarReceiver channel31Thread = new HangarReceiver(channel31, hangar1.getReceivedMessageQueue());
			HangarReceiver channel32Thread = new HangarReceiver(channel32, hangar2.getReceivedMessageQueue());

			// create 3 threads, used to dispatch the received message from queue
			// with simulated communication delay
			MessageQueueDispatcher messageQueueDispatcher1 = new MessageQueueDispatcher(hangar1);
			MessageQueueDispatcher messageQueueDispatcher2 = new MessageQueueDispatcher(hangar2);
			MessageQueueDispatcher messageQueueDispatcher3 = new MessageQueueDispatcher(hangar3);

			// start all hangar thread to constantly send planes to each other
			hangar1.start();
			hangar2.start();
			hangar3.start();

			// start 6 communication threads to get messages out of the each object stream
			// and put them into the received message queues
			channel12Thread.start();
			channel13Thread.start();
			channel21Thread.start();
			channel23Thread.start();
			channel31Thread.start();
			channel32Thread.start();

			// start 3 message queue dispatcher for each hangar
			messageQueueDispatcher1.start();
			messageQueueDispatcher2.start();
			messageQueueDispatcher3.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MainWindow() {
		setSize(400, 300);
		setTitle("TK1-EX5");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// history list
		historyListModel = new DefaultListModel<String>();
		historyList = new JList<String>(historyListModel);
		historyList.setAutoscrolls(true);

		JScrollPane historyScroll = new JScrollPane(historyList);
		add(historyScroll, BorderLayout.CENTER);

		// slide panel
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new GridLayout(3, 1));

		// snapshot 1
		snapshot1Label = new JLabel("Hangar 1 (#0)");
		snapshot1Button = new JButton("Snapshot");
		snapshot1Button.addActionListener(x -> snapshot(1));

		JPanel snapshot1 = new JPanel();
		snapshot1.setLayout(new GridLayout(2, 1));
		snapshot1.add(snapshot1Label);
		snapshot1.add(snapshot1Button);
		sidePanel.add(snapshot1);

		// snapshot 2
		snapshot2Label = new JLabel("Hangar 2 (#0)");
		snapshot2Button = new JButton("Snapshot");
		snapshot2Button.addActionListener(x -> snapshot(1));

		JPanel snapshot2 = new JPanel();
		snapshot2.setLayout(new GridLayout(2, 1));
		snapshot2.add(snapshot2Label);
		snapshot2.add(snapshot2Button);
		sidePanel.add(snapshot2);

		// snapshot 3
		snapshot3Label = new JLabel("Hangar 3 (#0)");
		snapshot3Button = new JButton("Snapshot");
		snapshot3Button.addActionListener(x -> snapshot(1));

		JPanel snapshot3 = new JPanel();
		snapshot3.setLayout(new GridLayout(2, 1));
		snapshot3.add(snapshot3Label);
		snapshot3.add(snapshot3Button);
		sidePanel.add(snapshot3);

		add(sidePanel, BorderLayout.EAST);
	}

	private void snapshot(int snapshot) {
		// TODO
		historyListModel.addElement("Snapshot ...");
	}

}
