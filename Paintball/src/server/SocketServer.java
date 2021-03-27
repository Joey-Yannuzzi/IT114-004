package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SocketServer {

	int port;
	public static boolean isRunning = false;
	private List<Room> rooms = new ArrayList<Room>();
	private Room lobby;

	private void start(int port) {
		this.port = port;
		System.out.println("Awating Users");

		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;
			lobby = new Room("Lobby", this);
			rooms.add(lobby);

			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					System.out.println("Connecting User...");
					ServerThread thread = new ServerThread(client, lobby);
					thread.start();
					lobby.addClient(thread);
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

	protected Room getLobby() {
		return (lobby);
	}

	private Room getRoom(String roomName) {
		for (int i = 0, l = rooms.size(); i < l; i++) {
			if (rooms.get(i).getName().equalsIgnoreCase(roomName)) {
				return (rooms.get(i));
			}
		}
		return (null);
	}

	protected synchronized boolean joinRoom(String roomName, ServerThread client) {
		Room newRoom = getRoom(roomName);
		Room oldRoom = client.getCurrentRoom();

		if (newRoom != null) {
			if (oldRoom != null) {
				System.out.println(client.getName() + " left " + oldRoom.getName());
				oldRoom.removeClient(client);
			}
			System.out.println(client.getName() + " joined " + newRoom.getName());
			oldRoom.removeClient(client);
			return (true);
		}
		return (false);
	}

	protected synchronized boolean createNewRoom(String roomName) {
		if (getRoom(roomName) != null) {
			System.out.println("Room exists");
			return (false);
		} else {
			Room room = new Room(roomName, this);
			rooms.add(room);
			System.out.println("Created room: " + roomName);
			return (true);
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
