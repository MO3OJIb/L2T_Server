package retail.TendencyNPCs;

import l2tserver.gameserver.datatables.ClanTable;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.instancemanager.CastleManager;
import l2tserver.gameserver.instancemanager.QuestManager;
import l2tserver.gameserver.model.L2Clan;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.entity.Castle;
import l2tserver.gameserver.model.quest.Quest;
import l2tserver.gameserver.model.quest.QuestState;
import l2tserver.gameserver.network.serverpackets.NpcHtmlMessage;
import l2tserver.gameserver.network.serverpackets.NpcSay;

/**
 * @author LasTravel
 * 
 * NOTE: Pere please don't kill me.
 */

public class TendencyNPCs  extends Quest
{
	private static final L2Skill blessingOfLight 	= SkillTable.getInstance().getInfo(19036, 1);
	
	public TendencyNPCs(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int id = 36600; id <= 36617; id ++)
		{
			addFirstTalkId(id);
			
			addStartNpc(id);
			
			addTalkId(id);
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			
			st = q.newQuestState(player);
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		
		Castle castle = npc.getCastle();	//Castle zone from the npc
		
		L2Clan clanOwner = ClanTable.getInstance().getClan(castle.getOwnerId());	//Owner of this castle
		
		L2Clan playerClan = player.getClan();	//Get the player clan
		
		Castle playerCastle = null;
		
		if (playerClan != null && CastleManager.getInstance().getCastleByOwner(playerClan) != null)
		{	
			playerCastle = CastleManager.getInstance().getCastleByOwner(playerClan);
		}
		
		int playerTendency = playerCastle != null ? playerCastle.getTendency() : 0;
				
		String htmlText = "";
		
		if (npc.getNpcId() >= 36609 && npc.getNpcId() <= 36617)
		{
			if (playerTendency == Castle.TENDENCY_LIGHT || playerTendency == Castle.TENDENCY_NONE)
			{	
				player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getNpcId(), 1300172));
				
				htmlText = "proclaimer.htm";
			}
			else
			{	
				htmlText = "proclaimer-no.htm";
			}	
		}
		else
		{
			if (playerTendency == Castle.TENDENCY_DARKNESS || playerTendency == Castle.TENDENCY_NONE)
			{	
				player.sendPacket(new NpcSay(npc.getObjectId(), 2, npc.getNpcId(), 1300171));
				
				htmlText = "revolutionary.htm";
			}
			else
			{	
				htmlText = "revolutionary-no.htm";
			}	
		}	
		
		String content = getHtm(player.getHtmlPrefix(), htmlText);
		
		html.setHtml(content);
		
		html.replace("%clanName%", clanOwner != null ? clanOwner.getName() : "Deffenders");
		
		html.replace("%clanLeader%", clanOwner != null ? clanOwner.getLeaderName() : "Tenkai");
		
		html.replace("%castleName%", castle.getName());
		
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
		player.sendPacket(html);
		
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("receiveBlessing"))
		{	
			blessingOfLight.getEffects(player, player);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new TendencyNPCs(-1, "TendencyNPCs", "retail");
	}
}
