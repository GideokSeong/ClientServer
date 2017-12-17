import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JComboBox;
import javax.sound.sampled.Line;
import javax.swing.JButton;

public class Client extends JFrame {

	/** IP address of server */
	public static final String SERVER_ADDRESS = "192.168.1.2";
	/** port of the server */
	private static final int SERVER_PORT = 5000;
	/** time out interval */
	private static final int TIMEOUT = 10000;
	/** a message for disconnect */
	private static final String QUIT = "QUIT";
	/**  */
	private static final String NUMNER_ERROR = "Oprand input error";
	/** OK message send from server */
	private static final String OK = "ok";
	/** all the operators */
	private static final String[] oprands = new String[] { " ", "+", "-", "*", "/" };
	private static final long serialVersionUID = 1L;
	private JTextField op1TextField;
	private JTextField op2TextField;
	private JComboBox<String> oprandComboBox;
	private JButton calulateButton;
	private JButton connectButton;
	private JTextArea logTextArea;
	private boolean connected = false;
	/** a connect socket */
	private Socket socket = null;
	private PrintWriter writer;
	private BufferedReader reader;

	private String addressLine;

	public Client() {
		setTitle("NOT Connected");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(450, 400);
		setLocationRelativeTo(null);
		initComponents();
		addEventHandler();
		addLog("Client Running\n");
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				if (socket!=null) {
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

		});
	}

	/**
	 * change the status of the Client View
	 */
	private void changeStatus() {
		if (connected) {
			connectButton.setText("Disconnect");
			calulateButton.setEnabled(true);
		} else {
			connectButton.setText("Connect Server");
			calulateButton.setEnabled(false);
			setTitle("DISCONNECTED: " + addressLine);
		}
	}

	/**
	 * add a logging message
	 * 
	 * @param Text
	 *            message to be added
	 */
	private void addLog(String Text) {
		logTextArea.append(Text);
	}

	/**
	 * initialize the GUI of the Client
	 */
	private void initComponents() {
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setHgap(10);

		logTextArea = new JTextArea();
		getContentPane().add(logTextArea, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 1, 0, 0));

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEADING);
		panel.add(panel_2);

		op1TextField = new JTextField();
		panel_2.add(op1TextField);
		op1TextField.setColumns(10);

		op2TextField = new JTextField();
		panel_2.add(op2TextField);
		op2TextField.setColumns(10);

		oprandComboBox = new JComboBox<String>(oprands);
		panel_2.add(oprandComboBox);

		calulateButton = new JButton("Compute");
		calulateButton.setEnabled(false);
		panel_2.add(calulateButton);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setHgap(20);
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel.add(panel_1);

		connectButton = new JButton("Connect to Server");
		panel_1.add(connectButton);
	}

	/**
	 * add clicked event handlers for all the buttons
	 */
	private void addEventHandler() {

		calulateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder builder = new StringBuilder();
				builder.append(op1TextField.getText() + ",");
				builder.append(op2TextField.getText() + ",");
				builder.append(oprandComboBox.getSelectedItem() + "\n");
				// send a string to be calculated to server
				writer.write(builder.toString());
				// flush the output stream
				writer.flush();
				try {
					// read the calculation result from the server
					String line = reader.readLine();
					// if the input operands has some error
					if (line.equals(NUMNER_ERROR)) {
						JOptionPane.showMessageDialog(Client.this, NUMNER_ERROR);
					} else {
						addLog(line + "\n");
					}
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(Client.this, "Something going wrong!");
				}
			}
		});

		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (connected) { // already connected to server, disconnect
					connected = false;
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					try {
						// create a socket
						socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
						addLog("Created Socket\n");
						socket.setSoTimeout(TIMEOUT);
						// create the input stream and output stream
						reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						writer = new PrintWriter(socket.getOutputStream());
						addLog("Created input stream\n");
						String line = reader.readLine();
						if (line.equals(OK)) {
							addressLine = reader.readLine();
							setTitle("CONNECTED: " + addressLine);
							addLog("The Text from the server is: OK\n");
							addLog("Connect Successful\n");
						}
					} catch (Exception e2) { // create socket failed
						JOptionPane.showMessageDialog(Client.this, "Connect error!");
						return;
					}
					connected = true;
				}
				changeStatus();
			}
		});
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Client().setVisible(true);
	}

}
