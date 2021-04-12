package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Room implements AutoCloseable {
	private static SocketServer server;
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private List<ServerThread> clients = new ArrayList<ServerThread>();

	public Room(String name) {
		this.name = name;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return (name);
	}

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);

		if (clients.indexOf(client) > -1) {
			log.log(Level.INFO, "Cloning a user is not possible");
		} else {
			clients.add(client);

			if (client.getClientName() != null) {
				client.sendClearList();
				sendConnectionStatus(client, true, "joined the room " + getName());
				updateClientList(client);
			}
		}
	}

	private void updateClientList(ServerThread client) {
		Iterator<ServerThread> iter = clients.iterator();

		while (iter.hasNext()) {
			ServerThread c = iter.next();

			if (c != client) {
				boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		clients.remove(client);

		if (clients.size() > 0) {
			sendConnectionStatus(client, false, "left the room " + getName());
		} else {
			cleanupEmptyRoom();
		}
	}

	private void cleanupEmptyRoom() {
		if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
			return;
		}

		try {
			log.log(Level.INFO, "Closing " + name);
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void joinRoom(String room, ServerThread client) {
		server.joinRoom(room, client);
	}

	protected void joinLobby(ServerThread client) {
		server.joinLobby(client);
	}

	private boolean processCommands(String message, ServerThread client) {
		boolean wasCommand = false;

		try {
			if (message.indexOf(COMMAND_TRIGGER) > -1) {
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];

				if (command != null) {
					command = command.toLowerCase();
				}

				String roomName;

				switch (command) {
				case CREATE_ROOM:
					roomName = comm2[1];

					if (server.createNewRoom(roomName)) {
						joinRoom(roomName, client);
					}

					wasCommand = true;
					break;

				case JOIN_ROOM:
					roomName = comm2[1];
					joinRoom(roomName, client);
					wasCommand = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (wasCommand);
	}

	protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		Iterator<ServerThread> iter = clients.iterator();

		while (iter.hasNext()) {
			ServerThread c = iter.next();
			boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + client.getId());
			}
		}
	}

	protected void sendMessage(ServerThread sender, String message) {
		log.log(Level.INFO, getName() + " sending message to " + clients.size() + " users");

		if (processCommands(message, sender)) {
			return;
		}

		Iterator<ServerThread> iter = clients.iterator();

		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.send(sender.getClientName(), message);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + client.getId());
			}
		}
	}

	@Override
	public void close() throws Exception {
		int clientCount = clients.size();

		if (clientCount > 0) {
			log.log(Level.INFO, "Sending " + clients.size() + " to lobby");
			Iterator<ServerThread> iter = clients.iterator();
			Room lobby = server.getLobby();

			while (iter.hasNext()) {
				ServerThread client = iter.next();
				lobby.addClient(client);
				iter.remove();
			}

			log.log(Level.INFO, "Sent " + clientCount + " to lobby");
		}

		server.cleanupRoom(this);
		name = null;

	}
}
