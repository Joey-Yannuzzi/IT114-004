package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {

	int port;
	public static boolean isRunning = false;
	private List<Room> rooms = new ArrayList<Room>();
	private Room lobby;
	private List<Room> isolatedPrelobbies = new ArrayList<Room>();
	private final static String PRELOBBY = "PreLobby";
	protected final static String LOBBY = "Lobby";
	private final static Logger log = Logger.getLogger(SocketServer.class.getName());

	private void start(int port) {
		this.port = port;
		log.log(Level.INFO, "Awating Users");

		try (ServerSocket serverSocket = new ServerSocket(port);) {
			isRunning = true;
			Room.setServer(this);
			lobby = new Room(LOBBY);
			rooms.add(lobby);

			while (SocketServer.isRunning) {
				try {
					Socket client = serverSocket.accept();
					log.log(Level.INFO, "Connecting User...");
					ServerThread thread = new ServerThread(client, lobby);
					thread.start();
					Room prelobby = new Room(PRELOBBY);
					prelobby.addClient(thread);
					isolatedPrelobbies.add(prelobby);
					log.log(Level.INFO, "User Added");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				cleanup();
				log.log(Level.INFO, "Server Closed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void cleanupRoom(Room r) {
		Iterator<Room> iter = isolatedPrelobbies.iterator();

		while (iter.hasNext()) {
			Room check = iter.next();

			if (check.equals(r)) {
				iter.remove();
				log.log(Level.INFO, "Removed " + check.getName() + " from prelobby");
				break;
			}
		}
	}

	private void cleanup() {
		Iterator<Room> rooms = this.rooms.iterator();

		while (rooms.hasNext()) {
			Room r = rooms.next();

			try {
				r.close();
			} catch (Exception e) {

			}
		}

		Iterator<Room> pl = isolatedPrelobbies.iterator();

		while (pl.hasNext()) {
			Room r = pl.next();

			try {
				r.close();
			} catch (Exception e) {

			}
		}

		try {
			lobby.close();
			log.log(Level.WARNING, "Lobby closed");
		} catch (Exception e) {

		}
	}

	protected Room getLobby() {
		return (lobby);
	}

	protected void joinLobby(ServerThread client) {
		Room prelobby = client.getCurrentRoom();

		if (joinRoom(LOBBY, client)) {
			if (prelobby != null) {
				prelobby.removeClient(client);
				log.log(Level.INFO, "Added " + client.getClientName() + " to the lobby");
			} else {
				log.log(Level.WARNING, "Prelobby was null for " + client.getClientName());
			}
		} else {
			log.log(Level.INFO, "Problem adding " + client.getClientName() + " to the lobby");
		}
	}

	private Room getRoom(String roomName) {
		Iterator<Room> iter = rooms.iterator();

		while (iter.hasNext()) {
			Room r = iter.next();

			if (r != null && r.getName() != null && r.getName().equalsIgnoreCase(roomName)) {
				return (r);
			}
		}

		log.log(Level.WARNING, "Error getting room " + roomName);
		return (null);
	}

	protected synchronized boolean joinRoom(String roomName, ServerThread client) {
		if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
			log.log(Level.WARNING, "Room is either null or " + PRELOBBY);
			return (false);
		}

		Room newRoom = getRoom(roomName);
		Room oldRoom = client.getCurrentRoom();

		if (newRoom != null) {
			if (oldRoom != null) {
				log.log(Level.INFO, client.getName() + " left " + oldRoom.getName());
				oldRoom.removeClient(client);
			} else {
				log.log(Level.WARNING, "old room is null for " + client.getClientName());
			}
			log.log(Level.INFO, client.getName() + " joined " + newRoom.getName());
			newRoom.addClient(client);
			return (true);
		}
		return (false);
	}

	protected synchronized boolean createNewRoom(String roomName) {
		if (roomName == null || roomName.equalsIgnoreCase(PRELOBBY)) {
			return (false);
		}

		if (getRoom(roomName) != null) {
			log.log(Level.INFO, "Room exists");
			return (false);
		} else {
			Room room = new Room(roomName);
			rooms.add(room);
			log.log(Level.INFO, "Created room: " + roomName);
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
				log.log(Level.INFO, "Enter numbers only");
				port = 0;
			}

			if (port != 0) {
				break;
			}
		}

		log.log(Level.INFO, "Starting...");
		SocketServer server = new SocketServer();
		log.log(Level.INFO, "Server Started");
		server.start(port);
		log.log(Level.INFO, "Server Stopped");
	}

}
