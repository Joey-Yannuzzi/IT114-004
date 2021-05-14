package client;

import java.awt.Point;

import core.Team;

public interface Event {
	void onClientConnect(String clientName, String message);

	void onClientDisconnect(String clientName, String message);

	void onMessageReceive(String clientName, String message);

	void onChangeRoom();

	void onSyncDirection(String clientName, Point direction);

	void onSyncPosition(String clientName, Point position);

	void onSpawnProjectile(String name, Point direction);

	void onGameStart();

	void onGameEnd();

	// void onSendTeammates(Game game);

	void onSetCountdown(String message, int duration);

	void onSendTeams(Team redTeam, Team blueTeam);

	void onDeathReport(String name);

	void onGlobalDeath(String name, String message);

	void onHitReport(String name);
}