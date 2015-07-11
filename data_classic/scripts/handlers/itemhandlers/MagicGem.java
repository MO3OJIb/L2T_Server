/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Migi
 */

package handlers.itemhandlers;

import l2tserver.Config;
import l2tserver.gameserver.handler.IItemHandler;
import l2tserver.gameserver.instancemanager.InstanceManager;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.taskmanager.AttackStanceTaskManager;

public class MagicGem implements IItemHandler
{
	/**
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.actor.instance.L2Playable, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance magicGem, boolean forcedUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance)playable;
		
		if (!player.getFloodProtectors().getMagicGem().tryPerformAction("Magic Gem"))
			return;
		
		if (Config.isServer(Config.TENKAI))
		{
			if (player.getInstanceId() == 0 && !player.isInsideZone(L2Character.ZONE_PVP)
					&& (!player.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND)
							|| player.isInsideZone(L2Character.ZONE_TOWN))
					&& player.getEvent() == null && !player.isInOlympiadMode()
					&& !AttackStanceTaskManager.getInstance().getAttackStanceTask(player)
					&& InstanceManager.getInstance().getInstance(player.getObjectId()) == null
					&& player.getPvpFlag() == 0)
			{
				player.spawnServitors();
				player.sendMessage("You use a Magic Gem.");
			}
			else
				player.sendMessage("You cannot use a Magic Gem right now.");
		}
	}
}
