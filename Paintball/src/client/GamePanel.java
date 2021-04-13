package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import core.BaseGamePanel;

public class GamePanel extends BaseGamePanel implements Event {

	private static final long serialVersionUID = -1121202275148798015L;
	List<Player> players;
	Player myPlayer;
	String playerUsername;
	private final static Logger log = Logger.getLogger(GamePanel.class.getName());

	public void setPlayerName(String name) {
		playerUsername = name;

		if (myPlayer != null) {
			myPlayer.setName(playerUsername);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		setBackground(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.YELLOW);
		g.drawLine(30, 40, 100, 200);
		g.drawOval(150, 180, 10, 10);
		g.drawRect(200, 210, 20, 30);
		g.setColor(Color.RED);
		g.fillOval(300, 310, 30, 50);
		g.fillRect(test.x, test.y, 60, 50);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
		g.drawString("Testing custom drawing ...", 10, 20);
	}

	@Override
	public void attachListeners() {
		InputMap im = this.getRootPane().getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");
		ActionMap am = this.getRootPane().getActionMap();
		am.put("up_pressed", new MoveAction(KeyEvent.VK_W, true));
		am.put("up_released", new MoveAction(KeyEvent.VK_W, false));
		am.put("down_pressed", new MoveAction(KeyEvent.VK_S, true));
		am.put("down_released", new MoveAction(KeyEvent.VK_S, false));
		am.put("left_pressed", new MoveAction(KeyEvent.VK_A, true));
		am.put("left_released", new MoveAction(KeyEvent.VK_A, false));
		am.put("right_pressed", new MoveAction(KeyEvent.VK_D, true));
		am.put("right_released", new MoveAction(KeyEvent.VK_D, false));
	}

	@Override
	public void onSyncDirection(String clientName, Point direction) {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println("Syncing direction: " + clientName);
				p.setDirection(direction.x, direction.y);
				System.out.println("From: " + direction);
				System.out.println("To: " + p.getDirection());
				break;
			}
		}
	}

	@Override
	public void onSyncPosition(String clientName, Point position) {
		System.out.println("Got position for " + clientName);
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println(clientName + " set " + position);
				p.setPosition(position);
				break;
			}
		}
	}

	@Override
	public synchronized void onClientConnect(String clientName, String message) {
		System.out.println("Connected to game: " + clientName);
		boolean exists = false;
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				exists = true;
				break;
			}
		}

		if (!exists) {
			Player p = new Player();
			p.setName(clientName);
			players.add(p);

			if (clientName.equals(playerUsername)) {
				System.out.println("Reset player");
				myPlayer = p;
			}
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {
		System.out.println("Disconnected on Game Panel: " + clientName);
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && !p.getName().equals(playerUsername) && p.getName().equalsIgnoreCase(clientName)) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void onMessageReceive(String clientName, String message) {
		System.out.println("Message on Game Panel");
	}

	@Override
	public void onChangeRoom() {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();
			iter.remove();
		}

		myPlayer = null;
		System.out.println("Cleared players");
	}

	@Override
	public void awake() {
		players = new ArrayList<Player>();
	}

	@Override
	public void start() {

	}

	@Override
	public void update() {
		applyControls();
		localMovePlayers();
	}

	private void applyControls() {
		if (myPlayer != null) {
			int x = 0, y = 0;

			if (KeyStates.W) {
				y = -1;
			}

			if (KeyStates.S) {
				y = 1;
			}

			if (!KeyStates.W && !KeyStates.S) {
				y = 0;
			}

			if (KeyStates.A) {
				x = -1;
			} else if (KeyStates.D) {
				x = 1;
			}

			if (!KeyStates.A && !KeyStates.D) {
				x = 0;
			}

			boolean changed = myPlayer.setDirection(x, y);

			if (changed) {
				System.out.println("Direction changed");
				SocketClient.INSTANCE.syncDirection(new Point(x, y));
			}
		}
	}

	private void localMovePlayers() {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null) {
				p.move();
			}
		}
	}

	@Override
	public void lateUpdate() {

	}

	@Override
	public synchronized void draw(Graphics g) {
		setBackground(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawPlayers(g);
		drawText(g);
	}

	private synchronized void drawPlayers(Graphics g) {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();
			if (p != null) {
				p.draw(g);
			}
		}
	}

	private void drawText(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));

		if (myPlayer != null) {
			g.drawString("Debug MyPlayer: " + myPlayer.toString(), 10, 20);
		}
	}

	@Override
	public void quit() {
		log.log(Level.INFO, "Quiting game");
	}
}

class MoveAction extends AbstractAction {
	private static final long serialVersionUID = 5137817329873449021L;
	int key;
	boolean pressed = false;

	MoveAction(int k, boolean pressed) {
		key = k;
		this.pressed = pressed;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (key) {
		case KeyEvent.VK_W:
			KeyStates.W = pressed;
			break;

		case KeyEvent.VK_S:
			KeyStates.S = pressed;
			break;

		case KeyEvent.VK_A:
			KeyStates.A = pressed;
			break;

		case KeyEvent.VK_D:
			KeyStates.D = pressed;
			break;
		}
	}
}

class KeyStates {
	public static boolean W = false;
	public static boolean S = false;
	public static boolean A = false;
	public static boolean D = false;
}
