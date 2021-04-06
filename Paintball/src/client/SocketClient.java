package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

class SocketClient {

	private Socket server;
	private Thread inputThread;
	private Thread fromServerThread;

	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("User Connected");
		} catch (UnknownHostException e) {
			System.out.println("Invalid IP Address");
		} catch (IOException e) {
			System.out.println("Invalid Port");
		}
	}

	private void listenForKeyboard(Scanner doabarrelroll, ObjectOutputStream out) {
		inputThread = new Thread() {
			@Override
			public void run() {
				try {
					while (!server.isClosed()) {
						System.out.println("Awaitng Messages");
						String line = doabarrelroll.nextLine();

						if (!"quit".equalsIgnoreCase(line) && line != null) {
							out.writeObject(line);
						} else {
							System.out.println("Stopping Thread");
							out.writeObject("Goodbye");
							break;
						}

						try {
							sleep(50);
						} catch (Exception e) {
							System.out.println("Thread has insomnia");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					close();
					System.out.println("No longer listening");
				}
			}
		};
		inputThread.start();
	}

	private void listenForServerMessage(ObjectInputStream in) {
		fromServerThread = new Thread() {
			@Override
			public void run() {
				try {
					String fromServer;

					while (!server.isClosed() && (fromServer = (String) in.readObject()) != null) {
						System.out.println(fromServer);
					}
				} catch (Exception e) {
					if (!server.isClosed()) {
						System.out.println("Server Closed");
					} else {
						System.out.println("Connection Closed");
					}
				} finally {
					close();
					System.out.println("No longer listening");
				}
			}
		};
		fromServerThread.start();
	}

	public void start() {
		if (server == null) {
			return;
		}

		System.out.println("Enter Input");

		try (Scanner doabarrelroll = new Scanner(System.in);
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());) {
			listenForKeyboard(doabarrelroll, out);
			listenForServerMessage(in);

			while (!server.isClosed()) {
				Thread.sleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void close() {
		if (server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Socket Closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SocketClient client = new SocketClient();
		Scanner doabarrelroll = new Scanner(System.in);
		String ip;
		int p;

		System.out.print("Enter Address: ");
		ip = doabarrelroll.next();

		while (true) {
			System.out.print("Enter Port: ");
			try {
				p = Integer.parseInt(doabarrelroll.next());
			} catch (NumberFormatException e) {
				System.out.println("Enter numbers only");
				p = 0;
			}

			if (p != 0) {
				break;
			}
		}
		doabarrelroll.close();
		client.connect(ip, p);
		client.start();
	}

}
