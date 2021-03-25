package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

class SocketServer {

	int port;
	public static boolean isRunning = false;
	private List<ServerThread> clients = new ArrayList<ServerThread>();

	private void start(int port) {
		this.port = port;
		System.out.println("Awating Users");

		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;

			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Connecting User...");
					ServerThread thread = new ServerThread(client, this);
					thread.start();
					clients.add(thread);
					System.out.println("User Added");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				System.out.println("Server Closed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected synchronized void disconnect(ServerThread client) {
		long id = client.getId();
		clients.remove(client);
		broadcast("disconnected", id);
	}

	public synchronized void broadcast(String message, long id) {
		message = String.format("User[%d]: %s", id, message);

		Iterator<ServerThread> it = clients.iterator();

		while (it.hasNext()) {
			ServerThread client = it.next();
			boolean wasSuccessful = client.send(message);

			if (!wasSuccessful) {
				System.out.println("Removing user from list");
				it.remove();
				broadcast("disconnected", id);
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port;

		while (true) {
			System.out.print("Enter Port: ");
			try (Scanner doabarrelroll = new Scanner(System.in)) {
				port = Integer.parseInt(doabarrelroll.next());
			} catch (NumberFormatException e) {
				System.out.println("Enter numbers only");
				port = 0;
			}

			if (port != 0) {
				break;
			}
		}

		System.out.println("Starting...");
		SocketServer server = new SocketServer();
		System.out.println("Server Started");
		server.start(port);
		System.out.println("Server Stopped");
	}

}
