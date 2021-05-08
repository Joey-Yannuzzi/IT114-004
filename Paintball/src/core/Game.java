package core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import server.ClientPlayer;

public class Game {

	private boolean isActive = false;
	private Team redTeam;
	private Team blueTeam;
	private List<Projectile> projectiles;

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

	private void createTimer() {
		boolean daemon = true;
		Timer time = new Timer(daemon);
	}

	public boolean addProjectile(Projectile projectile) {
		Iterator<Projectile> iter = projectiles.iterator();

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p == projectile) {
				return (false);
			}
		}

		projectiles.add(projectile);
		return (true);
	}

	public boolean removeProjectile(Projectile projectile) {
		Iterator<Projectile> iter = projectiles.iterator();

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p == projectile) {
				projectiles.remove(projectile);
				return (true);
			}
		}

		return (false);
	}

	public void shootProjectile(Projectile projectile) {
		Iterator<Projectile> iter = projectiles.iterator();
		boolean exists = false;

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p == projectile) {
				exists = true;
				break;
			}
		}

		if (!exists) {
			addProjectile(projectile);
		}

		projectile.move();
	}
}
