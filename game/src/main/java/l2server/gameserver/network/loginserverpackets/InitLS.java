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

package l2server.gameserver.network.loginserverpackets;

import l2server.network.BaseRecievePacket;

public class InitLS extends BaseRecievePacket {
	private int rev;
	private byte[] key;

	public int getRevision() {
		return rev;
	}

	public byte[] getRSAKey() {
		return key;
	}

	public InitLS(byte[] decrypt) {
		super(decrypt);
		rev = readD();
		int size = readD();
		key = readB(size);
	}
}