package client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
	public synchronized void onClientConnect(String clientName, String message) {
		// TODO Auto-generated method stub

		System.out.println("Connected on Game Panel: " + clientName);
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
				System.out.println("Reset myPlayer");
				myPlayer = p;
			}
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

		System.out.println("Message on Game Panel");
	}

	@Override
	public void onChangeRoom() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

		players = new ArrayList<Player>();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

		applyControls();
		localMovePlayers();
	}

	private void applyControls() {
		if (myPlayer != null) {
			int x = 0, y = 0;

			if (KeyStates.W) {
				y = -1;
			} else if (KeyStates.S) {
				y = 1;
			} else if (!KeyStates.W && !KeyStates.S) {
				y = 0;
			}

			if (KeyStates.A) {
				x = -1;
			} else if (KeyStates.D) {
				x = 1;
			} else if (!KeyStates.A && !KeyStates.D) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void draw(Graphics g) {
		// TODO Auto-generated method stub

		setBackground(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawPlayers(g);
		drawText(g);
	}

	private synchronized void drawPlayers(Graphics g) {
		// g.setColor(Color.RED);
		// g.fillRect(0, 0, 50, 50);
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
		// TODO Auto-generated method stub

		log.log(Level.INFO, "GamePanel quit");
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
		// TODO Auto-generated method stub
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
