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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.L2ItemInstance;

/**
 * 16
 * d6 6d c0 4b		player id who dropped it
 * ee cc 11 43 		object id
 * 39 00 00 00 		item id
 * 8f 14 00 00 		x
 * b7 f1 00 00 		y
 * 60 f2 ff ff 		z
 * 01 00 00 00 		show item-count 1=yes
 * 7a 00 00 00	  count										 .
 * <p>
 * format  dddddddd	rev 377
 * ddddddddd   rev 417
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DropItem extends L2GameServerPacket {
	private L2ItemInstance item;
	private int charObjId;

	/**
	 * Constructor of the DropItem server packet
	 *
	 * @param item        : L2ItemInstance designating the item
	 * @param playerObjId : int designating the player ID who dropped the item
	 */
	public DropItem(L2ItemInstance item, int playerObjId) {
		this.item = item;
		charObjId = playerObjId;
	}

	@Override
	protected final void writeImpl() {
		writeD(charObjId);
		writeD(item.getObjectId());
		writeD(item.getItemId());

		writeD(item.getX());
		writeD(item.getY());
		writeD(item.getZ());
		// only show item count if it is a stackable item
		writeC(item.isStackable() ? 0x01 : 0x00);
		writeQ(item.getCount());

		writeC(0); // unknown
	}
}