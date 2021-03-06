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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.actor.instance.Player;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestShortCutDel extends L2GameClientPacket {

	private int slot;
	private int page;

	@Override
	protected void readImpl() {
		int id = readD();
		slot = id % 12;
		page = id / 12;
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (page > 10 || page < 0) {
			return;
		}

		activeChar.deleteShortCut(slot, page);
		// client needs no confirmation. this packet is just to inform the server
	}

	@Override
	protected boolean triggersOnActionRequest() {
		return false;
	}
}
