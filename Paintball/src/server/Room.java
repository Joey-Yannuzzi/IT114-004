package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Room {
	private SocketServer server;
	private String name;
	private List<ServerThread> clients = new ArrayList<ServerThread>();

	public Room(String name, SocketServer server) {
		this.name = name;
		this.server = server;
	}

	public String getName() {
		return (name);
	}

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);

		if (clients.indexOf(client) > -1) {
			System.out.println("Cloning a user is not possible");
		} else {
			clients.add(client);
			sendMessage(client, " joined " + getName());
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		clients.remove(client);
		sendMessage(client, " left");
	}

	private boolean processCommands(String message, ServerThread client) {
		boolean wasCommand = false;

		try {
			if (message.indexOf("/") > -1) {
				String[] com = message.split("/");
				String part = com[1];
				String[] com2 = part.split(" ");
				String command = com2[0];
				String roomName;

				switch (command) {
				case "createroom":
					roomName = com2[1];

					if (server.createNewRoom(roomName)) {
						server.joinRoom(roomName, client);
						{

						}
					}

					wasCommand = true;
					break;
				case "joinroom":
					roomName = com2[1];
					server.joinRoom(roomName, client);
					wasCommand = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (wasCommand);
	}

	protected void sendMessage(ServerThread sender, String message) {
		System.out.println(getName() + " sending message to " + clients.size() + " users");

		if (processCommands(message, sender)) {
			return;
		}

		Iterator<ServerThread> iter = clients.iterator();
		message = String.format("User[%s]: %s", sender.getName(), message);

		while (iter.hasNext()) {
			ServerThread client = iter.next();
			boolean messageSent = client.send(message);

			if (!messageSent) {
				iter.remove();
				System.out.println("Removed " + client.getId());
			}
		}
	}
}
