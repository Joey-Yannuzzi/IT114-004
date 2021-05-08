package core;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import server.ClientPlayer;

public class Team {

	private Color teamColor;
	private List<ClientPlayer> teammates;
	private final int MAXTEAMSIZE = 3;
	private int teamSize;

	public Team(List<ClientPlayer> teammates, Color teamColor) {
		if (teammates.size() > MAXTEAMSIZE) {
			System.out.println("Team too big");
			return;
		}

		this.teammates = teammates;
		this.teamSize = this.teammates.size();
		this.teamColor = teamColor;
	}

	public Color getTeamColor() {
		return teamColor;
	}

	public List<ClientPlayer> getTeammates() {
		return teammates;
	}

	public boolean removeTeammate(ClientPlayer teammate) {
		boolean isRemoved = false;
		Iterator<ClientPlayer> iter = teammates.iterator();

		while (iter.hasNext()) {
			ClientPlayer t = iter.next();

			if (t == teammate) {
				teammates.remove(teammate);
				updateTeamSize();
				isRemoved = true;
				break;
			}
		}

		return (isRemoved);
	}

	public boolean addTeammate(ClientPlayer teammate) {
		boolean isAdded = false;

		if ((this.getTeamSize() + 1) > MAXTEAMSIZE) {
			return (isAdded);
		}

		Iterator<ClientPlayer> iter = teammates.iterator();

		while (iter.hasNext()) {
			ClientPlayer t = iter.next();

			if (t == teammate) {
				return (isAdded);
			}
		}

		teammates.add(teammate);
		updateTeamSize();
		isAdded = true;
		return (isAdded);
	}

	public int getTeamSize() {
		return teamSize;
	}

	private void updateTeamSize() {
		this.teamSize = this.teammates.size();
	}
}
