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
	private Room currentRoom;

	protected synchronized Room getCurrentRoom() {
		return (currentRoom);
	}

	protected synchronized void setCurrentRoom(Room room) {
		if (room != null) {
			currentRoom = room;
		} else {
			System.out.println("The room is null");
		}
	}

	public ServerThread(Socket myClient, Room room) throws IOException {
		this.client = myClient;
		this.currentRoom = room;
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
				currentRoom.sendMessage(this, fromClient);
			}
		} catch (Exception e) {
			System.out.println("User Disconnected");
		} finally {
			isRunning = false;
			cleanup();
		}
	}

	private void cleanup() {
		if (currentRoom != null) {
			System.out.println("Leaving room");
			currentRoom.removeClient(this);
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
