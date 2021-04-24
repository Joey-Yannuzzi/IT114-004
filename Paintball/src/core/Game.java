package core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import server.ClientPlayer;

public class Game {

	private boolean isActive = false;
	private Team redTeam;
	private Team blueTeam;

	public Game() {
		this.setActive(false);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getActive() {
		return (isActive);
	}

	public void createGame(List<ClientPlayer> clients) {
		this.setActive(true);
		this.createTeams(clients);
	}

	private void createTeams(List<ClientPlayer> clients) {
		Random rand = new Random();
		List<ClientPlayer> temp = clients;
		List<ClientPlayer> team1 = new ArrayList<ClientPlayer>();
		List<ClientPlayer> team2 = new ArrayList<ClientPlayer>();
		int size = clients.size() / 2;

		for (int i = 0; i < size; i++) {
			int r = rand.nextInt(temp.size());
			team1.add(temp.get(r));
			temp.remove(r);
		}

		redTeam = new Team(team1, Color.RED);
		size = clients.size() - size;

		for (int i = 0; i < size; i++) {
			int r = rand.nextInt(temp.size());
			team2.add(temp.get(r));
			temp.remove(r);
		}

		blueTeam = new Team(team2, Color.BLUE);
	}
}
