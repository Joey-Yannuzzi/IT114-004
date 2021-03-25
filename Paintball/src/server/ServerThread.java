package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerThread extends Thread {
	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private SocketServer server;

	public ServerThread(Socket myClient, SocketServer server) throws IOException {
		this.client = myClient;
		this.server = server;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
	}

	public boolean send(String message) {
		try {
			out.writeObject(message);
			return (true);
		} catch (IOException e) {
			System.out.println("Error sending a message");
			cleanup();
			return (false);
		}
	}

	@Override
	public void run() {
		try {
			isRunning = true;
			String fromClient;

			while (isRunning && !client.isClosed() && (fromClient = (String) in.readObject()) != null) {
				System.out.println("User: " + fromClient);
				server.broadcast(fromClient, this.getId());
			}
		} catch (Exception e) {
			System.out.println("User Disconnected");
		} finally {
			isRunning = false;
			cleanup();
		}
	}

	private void cleanup() {
		if (server != null) {
			server.disconnect(this);
		}

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				System.out.println("Already Closed");
			}
		}

		if (out != null) {
			try {
				in.close();
			} catch (IOException e) {
				System.out.println("Already Closed");
			}
		}

		if (client != null && !client.isClosed()) {
			try {
				client.shutdownInput();
			} catch (IOException e) {
				System.out.println("Socket is already closed");
			}

			try {
				client.shutdownOutput();
			} catch (IOException e) {
				System.out.println("Socket is already closed");
			}

			try {
				client.close();
			} catch (IOException e) {
				System.out.println("Already Closed");
			}
		}
	}
}
