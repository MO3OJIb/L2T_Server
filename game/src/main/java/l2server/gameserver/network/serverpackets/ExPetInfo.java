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

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.stats.VisualEffect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

/**
 * @author Pere
 */
public final class ExPetInfo extends L2GameServerPacket {
	private int objectId;
	private int val;
	private byte[] data1;
	private byte[] data2;
	private Set<Integer> abnormals;

	public ExPetInfo(PetInstance pet, Creature attacker, int val) {
		objectId = pet.getObjectId();
		val = 0;//pet.isShowSummonAnimation() ? 2 : val;

		ByteBuffer buffer = ByteBuffer.allocate(200).order(ByteOrder.LITTLE_ENDIAN);

		buffer.put((byte) (pet.isAutoAttackable(attacker) ? 1 : 0));
		buffer.put((byte) 0);
		buffer.put((byte) 0);
		buffer.put((byte) 0);
		buffer.put((byte) 0);

		for (char c : pet.getOwner().getName().toCharArray()) {
			buffer.putShort((short) c);
		}
		buffer.putShort((short) 0);

		int size = buffer.position();
		buffer.position(0);
		data1 = new byte[size];
		buffer.get(data1, 0, size);

		buffer = ByteBuffer.allocate(500).order(ByteOrder.LITTLE_ENDIAN);

		// Write data to the buffer
		buffer.putInt(pet.getNpcId() + 1000000);
		buffer.putInt(pet.getX());
		buffer.putInt(pet.getY());
		buffer.putInt(pet.getZ());
		buffer.putInt(pet.getHeading());
		buffer.putInt(pet.getPAtkSpd());
		buffer.putInt(pet.getMAtkSpd());
		buffer.putFloat(pet.getMovementSpeedMultiplier());
		buffer.putFloat(pet.getAttackSpeedMultiplier());

		/*buffer.putInt(summon.getWeapon());
		buffer.putInt(summon.getArmor());
		buffer.putInt(0);*/

		buffer.put((byte) 1);
		buffer.put((byte) (pet.isRunning() ? 1 : 0));
		buffer.put((byte) (pet.isInCombat() ? 1 : 0));
		buffer.put((byte) pet.getTeam());
		buffer.put((byte) 0);
		buffer.putShort((short) 0);
		buffer.putInt(0);

		buffer.putInt(0);

		buffer.putInt(0);
		buffer.put((byte) 1);

		buffer.putInt(pet.getMaxHp());

		buffer.putInt(pet.getMaxMp());

		buffer.putInt((int) Math.round(pet.getCurrentHp()));

		buffer.putInt((int) Math.round(pet.getCurrentMp()));

		if (pet.getName() != null) {
			for (char c : pet.getName().toCharArray()) {
				buffer.putShort((short) c);
			}
		}
		buffer.putShort((short) 0);

		buffer.putInt(-1);
		buffer.putInt(-1);
		buffer.put(pet.getPvpFlag());
		buffer.putInt(0);

		// Flag with bools
		// 0x00000001 unk
		// 0x00000002 dead
		// 0x00000004 unk
		// 0x00000008 show name
		// 0x00000010 unk
		// 0x00000020 unk
		// 0x00000040 unk
		byte flag = 0x04;
		if (pet.isAlikeDead()) {
			flag |= 0x02;
		}
		if (pet.getTemplate().ShowName) {
			flag |= 0x08;
		}
		buffer.put(flag);

		size = buffer.position();
		buffer.position(0);
		data2 = new byte[size];
		buffer.get(data2, 0, size);

		abnormals = pet.getAbnormalEffect();
		if (pet.getOwner().getAppearance().getInvisible()) {
			abnormals.add(VisualEffect.STEALTH.getId());
		}
	}

	@Override
	protected final void writeImpl() {
		writeD(objectId);
		writeC(val); // 0=teleported 1=default 2=summoned
		writeH(0x0025);
		writeC(0xfd);
		writeC(0xbf);
		writeC(0x5f);
		writeC(0xf3);
		writeC(0xec);

		writeC(data1.length);
		writeB(data1);

		writeH(data2.length);
		writeB(data2);

		writeH(abnormals.size());
		for (int abnormal : abnormals) {
			writeH(abnormal);
		}
	}
}
