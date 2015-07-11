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
package handlers.itemhandlers;

import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Item skills not allowed on olympiad
 */
public class ItemSkills extends ItemSkillsTemplate
{
	/**
	 * 
	 * @see l2tserver.gameserver.handler.IItemHandler#useItem(l2tserver.gameserver.model.actor.L2Playable, l2tserver.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (activeChar != null && activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (activeChar.getEvent() != null && !activeChar.getEvent().onScrollUse(activeChar.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		super.useItem(playable, item, forceUse);
	}
}
