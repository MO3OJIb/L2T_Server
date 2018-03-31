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

package vehicles;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.BoatManager;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.actor.instance.L2BoatInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.PlaySound;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author DS
 */
public class BoatInnadrilTour implements Runnable {
	private static final Logger log = Logger.getLogger(BoatInnadrilTour.class.getName());

	// Time: 1867s
	private static final VehiclePathPoint[] TOUR =
			{new VehiclePathPoint(105129, 226240, -3610, 150, 800), new VehiclePathPoint(90604, 238797, -3610, 150, 800),
					new VehiclePathPoint(74853, 237943, -3610, 150, 800), new VehiclePathPoint(68207, 235399, -3610, 150, 800),
					new VehiclePathPoint(63226, 230487, -3610, 150, 800), new VehiclePathPoint(61843, 224797, -3610, 150, 800),
					new VehiclePathPoint(61822, 203066, -3610, 150, 800), new VehiclePathPoint(59051, 197685, -3610, 150, 800),
					new VehiclePathPoint(54048, 195298, -3610, 150, 800), new VehiclePathPoint(41609, 195687, -3610, 150, 800),
					new VehiclePathPoint(35821, 200284, -3610, 150, 800), new VehiclePathPoint(35567, 205265, -3610, 150, 800),
					new VehiclePathPoint(35617, 222471, -3610, 150, 800), new VehiclePathPoint(37932, 226588, -3610, 150, 800),
					new VehiclePathPoint(42932, 229394, -3610, 150, 800), new VehiclePathPoint(74324, 245231, -3610, 150, 800),
					new VehiclePathPoint(81872, 250314, -3610, 150, 800), new VehiclePathPoint(101692, 249882, -3610, 150, 800),
					new VehiclePathPoint(107907, 256073, -3610, 150, 800), new VehiclePathPoint(112317, 257133, -3610, 150, 800),
					new VehiclePathPoint(126273, 255313, -3610, 150, 800), new VehiclePathPoint(128067, 250961, -3610, 150, 800),
					new VehiclePathPoint(128520, 238249, -3610, 150, 800), new VehiclePathPoint(126428, 235072, -3610, 150, 800),
					new VehiclePathPoint(121843, 234656, -3610, 150, 800), new VehiclePathPoint(120096, 234268, -3610, 150, 800),
					new VehiclePathPoint(118572, 233046, -3610, 150, 800), new VehiclePathPoint(117671, 228951, -3610, 150, 800),
					new VehiclePathPoint(115936, 226540, -3610, 150, 800), new VehiclePathPoint(113628, 226240, -3610, 150, 800),
					new VehiclePathPoint(111300, 226240, -3610, 150, 800), new VehiclePathPoint(111264, 226240, -3610, 150, 800)};

	private static final VehiclePathPoint DOCK = TOUR[TOUR.length - 1];

	private final L2BoatInstance boat;
	private int cycle = 0;

	private final CreatureSay ARRIVED_AT_INNADRIL;
	private final CreatureSay LEAVE_INNADRIL5;
	private final CreatureSay LEAVE_INNADRIL1;
	private final CreatureSay LEAVE_INNADRIL0;
	private final CreatureSay LEAVING_INNADRIL;

	private final CreatureSay ARRIVAL20;
	private final CreatureSay ARRIVAL15;
	private final CreatureSay ARRIVAL10;
	private final CreatureSay ARRIVAL5;
	private final CreatureSay ARRIVAL1;

	private final PlaySound INNADRIL_SOUND;

	public BoatInnadrilTour(L2BoatInstance boat) {
		this.boat = boat;

		ARRIVED_AT_INNADRIL = new CreatureSay(0, Say2.BOAT, 801, 998);
		LEAVE_INNADRIL5 = new CreatureSay(0, Say2.BOAT, 801, 999);
		LEAVE_INNADRIL1 = new CreatureSay(0, Say2.BOAT, 801, 1000);
		LEAVE_INNADRIL0 = new CreatureSay(0, Say2.BOAT, 801, 1001);
		LEAVING_INNADRIL = new CreatureSay(0, Say2.BOAT, 801, 1002);

		ARRIVAL20 = new CreatureSay(0, Say2.BOAT, 801, 1171);
		ARRIVAL15 = new CreatureSay(0, Say2.BOAT, 801, 1172);
		ARRIVAL10 = new CreatureSay(0, Say2.BOAT, 801, 1173);
		ARRIVAL5 = new CreatureSay(0, Say2.BOAT, 801, 1174);
		ARRIVAL1 = new CreatureSay(0, Say2.BOAT, 801, 1175);

		INNADRIL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", 1, boat.getObjectId(), DOCK.x, DOCK.y, DOCK.z);
	}

	@Override
	public void run() {
		try {
			switch (cycle) {
				case 0:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL5);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL1);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL0);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, LEAVING_INNADRIL, INNADRIL_SOUND);
					boat.payForRide(0, 1, 107092, 219098, -3952);
					boat.executePath(TOUR);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 650000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL20);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL15);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL10);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
				case 7:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL5);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 240000);
					break;
				case 8:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL1);
					break;
				case 9:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, ARRIVED_AT_INNADRIL, INNADRIL_SOUND);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 300000);
					break;
			}
			cycle++;
			if (cycle > 9) {
				cycle = 0;
			}
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
		}
	}

	public static void main(String[] args) {
		final L2BoatInstance boat = BoatManager.getInstance().getNewBoat(4, 111264, 226240, -3610, 32768);
		if (boat != null) {
			boat.registerEngine(new BoatInnadrilTour(boat));
			boat.runEngine(180000);
		}
	}
}