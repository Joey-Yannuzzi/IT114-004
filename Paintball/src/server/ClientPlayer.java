package server;

import client.Player;

public class ClientPlayer {

	public ServerThread client;
	public Player player;

	public ClientPlayer(ServerThread client, Player player) {
		this.client = client;
		this.player = player;
	}
}
