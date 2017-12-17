import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame{
	
	/** a message for disconnect */
	private static final String QUIT= "QUIT";
	private static final String NUMNER_ERROR= "Oprand input error";
	/** OK message send from server */
	private static final String OK = "ok";
	private static final long serialVersionUID = 1L;
	/** port of the server */
	private static final int SERVER_PORT = 5000;
	private JTextArea logTextArea;
	/** server socket */
	private ServerSocket serverSocket;

	/**
	 * setup the GUI
	 */
	public Server() {
		setTitle("Calculate Server Multi");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(450, 400);
		setLocationRelativeTo(null);
		logTextArea = new JTextArea();
		getContentPane().add(logTextArea, BorderLayout.CENTER);
	}
	
	/**
	 * add a logging message
	 * @param Text message to be added
	 */
	private void addLog(String log){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				logTextArea.setText(log+"\n"+logTextArea.getText());
			}
		});
	}
	
	/**
	 * run the server thread
	 */
	public void runBack(){
		new Thread(new ServerRunnable()).start();
	}
	
	/**
	 * a runnable for accepting the connect requests
	 */
	private class ServerRunnable implements Runnable{

		@Override
		public void run() {
			try {
				// create the server socket
				serverSocket = new ServerSocket(SERVER_PORT);
				addLog("Server Running");
				while(true){
					//accept the clients' connection request
					Socket clientSocket=serverSocket.accept();
					addLog("/"+clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort()+" connected");
					new Thread(new CalculationRunnable(clientSocket)).start();
				}
			} catch (IOException e) {
				addLog(e.getMessage());
			}finally {
				if (serverSocket!=null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	/**
	 * a runnable for doing the calculation
	 */
	private class CalculationRunnable implements Runnable{
		
		private Socket connection;
		
		public CalculationRunnable(Socket connection) {
			this.connection=connection;
		}

		@Override
		public void run() {
			BufferedReader reader=null;
			PrintWriter writer=null;
			try {
				 reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				 writer = new PrintWriter(connection.getOutputStream());
				 writer.write(OK+"\n");
				 writer.write("/"+connection.getInetAddress().getHostAddress()+":"+connection.getPort()+"\n");
				 addLog("Sending OK Response");
				 writer.flush();
				 String line = null;
				 line = reader.readLine();
				 // read messages send from client until a quit message accepted 
				 while (line!=null&&!line.equals(QUIT)) {
					//parse the message
					String tokens[] = line.split(",");
					double op1,op2;
					try{
						op1 = Double.parseDouble(tokens[0]);
						op2 = Double.parseDouble(tokens[1]);
					}catch (NumberFormatException ne) {
						writer.write(NUMNER_ERROR+"\n");
						writer.flush();
						line = reader.readLine();
						continue;
					}
					// do the calculation
					double result=0;
					addLog("Calc problem: "+op1+" "+tokens[2]+" "+op2+" = ?");
					switch (tokens[2]) {
					case "+":
						result = op1+op2;
						break;
					case "-":
						result = op1-op2;
						break;
					case "*":
						result = op1*op2;
						break;
					case "/":
						result = op1/op2;
						break;
					default:
						break;
					}
					// send the result to client
					writer.write(tokens[0]+tokens[2]+tokens[1]+"= "+result+"\n");
					writer.flush();
					line = reader.readLine();
				 }
				 addLog("/"+connection.getInetAddress().getHostAddress()+":"+connection.getPort()+" disconnected");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (connection!=null) {
					try {
						connection.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server();
		server.setVisible(true);
		server.runBack();
	}

}
