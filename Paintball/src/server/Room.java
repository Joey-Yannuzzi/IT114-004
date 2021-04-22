package server;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.Player;
import core.BaseGamePanel;

public class Room extends BaseGamePanel implements AutoCloseable {
	private static SocketServer server;
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private List<ClientPlayer> clients = new ArrayList<ClientPlayer>();
	static Dimension gameAreaSize = new Dimension(400, 600);
	long frame = 0;

	public Room(String name, boolean delayStart) {
		super(delayStart);
		this.name = name;
		isServer = true;
	}

	public Room(String name) {
		this.name = name;
		isServer = true;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return (name);
	}

	private static Point getRandomStartPosition() {
		Point startPos = new Point();
		startPos.x = (int) (Math.random() * gameAreaSize.width);
		startPos.y = (int) (Math.random() * gameAreaSize.height);
		return (startPos);
	}

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);
		boolean exists = false;
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client == client) {
				exists = true;

				if (c.player == null) {
					log.log(Level.WARNING, "Client " + client.getClientName() + " player was null, creating");
					Player p = new Player();
					p.setName(client.getClientName());
					c.player = p;
					syncClient(c);
				}

				break;
			}
		}

		if (exists) {
			log.log(Level.INFO, "Cloning a user is not possible");
		} else {
			Player p = new Player();
			p.setName(client.getClientName());
			ClientPlayer cp = new ClientPlayer(client, p);
			clients.add(cp);
			syncClient(cp);
		}
	}

	private void syncClient(ClientPlayer cp) {
		if (cp.client.getClientName() != null) {
			cp.client.sendClearList();
			sendConnectionStatus(cp.client, true, "joined the room " + getName());
			Point startPos = Room.getRandomStartPosition();
			cp.player.setPosition(startPos);
			cp.client.sendPosition(cp.client.getClientName(), startPos);
			sendPositionSync(cp.client, startPos);
			updateClientList(cp.client);
			updatePlayers(cp.client);
		}
	}

	private synchronized void updatePlayers(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client != client) {
				boolean messageSent = client.sendDirection(c.client.getClientName(), c.player.getDirection());

				if (messageSent) {
					messageSent = client.sendPosition(c.client.getClientName(), c.player.getPosition());
				}
			}
		}
	}

	private synchronized void updateClientList(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client != client) {
				boolean messageSent = client.sendConnectionStatus(c.client.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client == client) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getClientName() + " from " + getName());
			}
		}

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
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			boolean messageSent = c.client.sendConnectionStatus(client.getClientName(), isConnect, message);

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

		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.send(sender.getClientName(), message);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + client.client.getId());
			}
		}
	}

	protected void sendDirectionSync(ServerThread sender, Point dir) {
		boolean changed = false;
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();

			if (client.client == sender) {
				changed = client.player.setDirection(dir.x, dir.y);
				break;
			}
		}

		if (changed) {
			iter = clients.iterator();

			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				boolean messageSent = client.client.sendDirection(sender.getClientName(), dir);

				if (!messageSent) {
					iter.remove();
					log.log(Level.INFO, "Removed client " + client.client.getId());
				}
			}
		}
	}

	protected void sendPositionSync(ServerThread sender, Point pos) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.sendPosition(sender.getClientName(), pos);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.client.getId());
			}
		}
	}

	@Override
	public void close() throws Exception {
		int clientCount = clients.size();

		if (clientCount > 0) {
			log.log(Level.INFO, "Sending " + clients.size() + " to lobby");
			Iterator<ClientPlayer> iter = clients.iterator();
			Room lobby = server.getLobby();

			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				lobby.addClient(client.client);
				iter.remove();
			}

			log.log(Level.INFO, "Sent " + clientCount + " to lobby");
		}

		server.cleanupRoom(this);
		name = null;
		isRunning = false;
	}

	@Override
	public void awake() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

		log.log(Level.INFO, getName() + " start called");
	}

	void checkPositionSync(ClientPlayer cp) {
		if (frame % 120 == 0) {
			if (cp.player.changedPosition()) {
				sendPositionSync(cp.client, cp.player.getPosition());
			}
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer p = iter.next();

			if (p != null) {
				p.player.move();
				checkPositionSync(p);
			}
		}
	}

	private void nextFrame() {
		if (Long.MAX_VALUE - 5 <= frame) {
			frame = Long.MIN_VALUE;
		}

		frame++;
	}

	@Override
	public void lateUpdate() {
		// TODO Auto-generated method stub

		nextFrame();
	}

	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub

		log.log(Level.WARNING, getName() + " quit() ");
	}

	@Override
	public void attachListeners() {
		// TODO Auto-generated method stub

	}
}
