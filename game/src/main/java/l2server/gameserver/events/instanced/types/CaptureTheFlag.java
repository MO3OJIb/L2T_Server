package l2server.gameserver.events.instanced.types;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.EventFlagInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Pere
 */
public class CaptureTheFlag extends EventInstance {
	private static Logger log = LoggerFactory.getLogger(CaptureTheFlag.class.getName());

	private boolean flagsSpawned = false;

	public CaptureTheFlag(int id, EventConfig config) {
		super(id, config);
	}

	@Override
	public boolean startFight() {
		if (!super.startFight()) {
			return false;
		}

		if (!flagsSpawned) {
			spawnFlags();
		}

		return true;
	}

	@Override
	public void calculateRewards() {
		EventTeam team;
		if (config.getLocation().getTeamCount() != 4) {
			if (teams[0].getPoints() == teams[1].getPoints()) {
				// Check if one of the teams have no more players left
				if (teams[0].getParticipatedPlayerCount() == 0 || teams[1].getParticipatedPlayerCount() == 0) {
					// set state to rewarding
					setState(EventState.REWARDING);
					// return here, the Fight can't be completed
					Announcements.getInstance().announceToAll("The event has ended. No team won due to inactivity!");
					return;
				}

				// Both teams have equals points
				if (Config.INSTANCED_EVENT_REWARD_TEAM_TIE) {
					rewardTeams(-1);
				}

				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}

			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);

			// Get team which has more points
			team = teams[teams[0].getPoints() > teams[1].getPoints() ? 0 : 1];

			if (team == teams[0]) {
				rewardTeams(0);
			} else {
				rewardTeams(1);
			}
		} else {
			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);
			if (teams[0].getPoints() > teams[1].getPoints() && teams[0].getPoints() > teams[2].getPoints() &&
					teams[0].getPoints() > teams[3].getPoints()) {
				rewardTeams(0);
				team = teams[0];
			} else if (teams[1].getPoints() > teams[0].getPoints() && teams[1].getPoints() > teams[2].getPoints() &&
					teams[1].getPoints() > teams[3].getPoints()) {
				rewardTeams(1);
				team = teams[1];
			} else if (teams[2].getPoints() > teams[0].getPoints() && teams[2].getPoints() > teams[1].getPoints() &&
					teams[2].getPoints() > teams[3].getPoints()) {
				rewardTeams(2);
				team = teams[2];
			} else if (teams[3].getPoints() > teams[0].getPoints() && teams[3].getPoints() > teams[1].getPoints() &&
					teams[3].getPoints() > teams[2].getPoints()) {
				rewardTeams(3);
				team = teams[3];
			} else {
				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}
		}

		Announcements.getInstance().announceToAll("The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " points.");
	}

	@Override
	public void stopFight() {
		super.stopFight();
		unspawnFlags();
	}

	@Override
	public String getRunningInfo(Player player) {
		String html = "";
		for (EventTeam team : teams) {
			if (team.getParticipatedPlayerCount() > 0) {
				html += "Team " + team.getName() + " points: " + team.getPoints() + "<br>";
			}
		}
		if (html.length() > 4) {
			html = html.substring(0, html.length() - 4);
		}
		return html;
	}

	public void onFlagTouched(Player player, EventTeam team) {
		EventTeam playerTeam = getParticipantTeam(player.getObjectId());
		if (playerTeam == null) {
			return;
		}

		if (team == playerTeam && player.getCtfFlag() != null) {
			spawnFlag(player.getCtfFlag());
			player.setCtfFlag(null);
			player.addEventPoints(1);
			setImportant(player, false);
			playerTeam.increasePoints();
			sendToAllParticipants("The " + playerTeam.getName() + " team has captured a flag!");

			player.addEventPoints(20);
		} else if (team != playerTeam && player.getCtfFlag() == null) {
			unspawnFlag(team);
			player.setCtfFlag(team);
			setImportant(player, true);
			sendToAllParticipants(
					playerTeam.getName() + " team's member " + player.getName() + " has caught the " + team.getName() + " team's flag!");

			CreatureSay cs = new CreatureSay(player.getObjectId(), Say2.TELL, player.getName(), "I have caught " + team.getName() + " team's flag!");
			playerTeam.getParticipatedPlayers().values().stream().filter(character -> character != null).forEach(character -> {
				character.sendPacket(cs);
			});

			player.addEventPoints(10);
		}
	}

	@Override
	public void onKill(Creature killerCharacter, Player killedPlayer) {
		spawnFlags();
		if (killedPlayer == null || !isState(EventState.STARTED)) {
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayer.getObjectId());
		if (killedTeamId == -1) {
			return;
		}

		int killValue = 1;
		if (killedPlayer.getCtfFlag() != null) {
			spawnFlag(killedPlayer.getCtfFlag());
			killedPlayer.setCtfFlag(null);
			if (killerCharacter != null && getParticipantTeam(killerCharacter.getObjectId()) != null) {
				sendToAllParticipants(killedPlayer.getName() + ", the " + getParticipantTeam(killerCharacter.getObjectId()).getName() +
						" team's flag possessor, has lost the flag.");
			}

			killValue = 4;
		}

		Player killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null) {
			return;
		}

		killerPlayer.addEventPoints(3 * killValue);
		List<Player> assistants = PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
		for (Player assistant : assistants) {
			assistant.addEventPoints(killValue);
		}

		new EventTeleporter(killedPlayer, teams[killedTeamId].getCoords(), false, false);
	}

	private void spawnFlags() {
		spawnFlag(teams[0]);
		spawnFlag(teams[1]);
		if (config.getLocation().getTeamCount() == 4) {
			spawnFlag(teams[2]);
			spawnFlag(teams[3]);
		}
		flagsSpawned = true;
	}

	private void unspawnFlags() {
		for (EventTeam team : teams) {
			unspawnFlag(team);
		}
		flagsSpawned = false;
	}

	public void spawnFlag(EventTeam team) {
		NpcTemplate tmpl = NpcTable.getInstance().getTemplate(team.getFlagId());

		try {
			int x = 0;
			int y = 0;
			for (int i = 0; i < config.getLocation().getTeamCount(); i++) {
				x += teams[i].getCoords().getX();
				y += teams[i].getCoords().getY();
			}
			x /= config.getLocation().getTeamCount();
			y /= config.getLocation().getTeamCount();

			L2Spawn flagSpawn = new L2Spawn(tmpl);

			team.setFlagSpawn(flagSpawn);

			int heading = (int) Math.round(Math.atan2(y - team.getCoords().getY(), x - team.getCoords().getX()) / Math.PI * 32768);
			if (heading < 0) {
				heading = 65535 + heading;
			}

			flagSpawn.setX(team.getCoords().getX());
			flagSpawn.setY(team.getCoords().getY());
			flagSpawn.setZ(team.getCoords().getZ());
			flagSpawn.setHeading(heading);
			flagSpawn.setInstanceId(getInstanceId());

			SpawnTable.getInstance().addNewSpawn(flagSpawn, false);

			flagSpawn.stopRespawn();
			flagSpawn.doSpawn();
			EventFlagInstance flag = (EventFlagInstance) flagSpawn.getNpc();
			flag.setEvent(this);
			flag.setTeam(team);
			flag.setTitle(team.getName());
			flag.updateAbnormalEffect();
		} catch (Exception e) {
			log.warn("CTF Engine[spawnFlag(" + team.getName() + ")]: exception:");
			e.printStackTrace();
		}
	}

	private void unspawnFlag(EventTeam team) {
		if (team.getFlagSpawn() != null) {
			((EventFlagInstance) team.getFlagSpawn().getNpc()).shouldBeDeleted();
			team.getFlagSpawn().getNpc().deleteMe();
			team.getFlagSpawn().stopRespawn();
			SpawnTable.getInstance().deleteSpawn(team.getFlagSpawn(), false);
		}
	}
}
