/*
 * $Header: WayPointNode.java, 20/07/2005 19:49:29 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/07/2005 19:49:29 $
 * $Revision: 1 $
 * $Log: WayPointNode.java,v $
 * Revision 1  20/07/2005 19:49:29  luisantonioa
 * Added copyright notice
 *
 *
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

package l2server.gameserver.model.waypoint;

import l2server.Config;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.util.Point3D;

import java.util.*;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class WayPointNode extends WorldObject {
	private int id;
	private String title, type;
	private static final String NORMAL = "Node", SELECTED = "Selected", LINKED = "Linked";
	private static int lineId = 5560;
	private static final String LINE_TYPE = "item";
	private Map<WayPointNode, List<WayPointNode>> linkLists;

	public WayPointNode(int objectId) {
		super(objectId);
		linkLists = Collections.synchronizedMap(new WeakHashMap<WayPointNode, List<WayPointNode>>());
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.model.WorldObject#isAutoAttackable(l2server.gameserver.model.Creature)
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return false;
	}

	public static WayPointNode spawn(String type, int id, int x, int y, int z) {
		WayPointNode newNode = new WayPointNode(IdFactory.getInstance().getNextId());
		newNode.getPoly().setPolyInfo(type, id + "");
		newNode.spawnMe(x, y, z);
		return newNode;
	}

	public static WayPointNode spawn(boolean isItemId, int id, Player player) {
		return spawn(isItemId ? "item" : "npc", id, player.getX(), player.getY(), player.getZ());
	}

	public static WayPointNode spawn(boolean isItemId, int id, Point3D point) {
		return spawn(isItemId ? "item" : "npc", id, point.getX(), point.getY(), point.getZ());
	}

	public static WayPointNode spawn(Point3D point) {
		return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, point.getX(), point.getY(), point.getZ());
	}

	public static WayPointNode spawn(Player player) {
		return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, player.getX(), player.getY(), player.getZ());
	}

	@Override
	public void onAction(Player player, boolean interact) {
		if (player.getTarget() != this) {
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
	}

	public void setNormalInfo(String type, int id, String title) {
		this.type = type;
		changeID(id, title);
	}

	public void setNormalInfo(String type, int id) {
		this.type = type;
		changeID(id);
	}

	private void changeID(int id) {
		this.id = id;
		toggleVisible();
		toggleVisible();
	}

	private void changeID(int id, String title) {
		setName(title);
		setTitle(title);
		changeID(id);
	}

	public void setLinked() {
		changeID(Config.LINKED_NODE_ID, LINKED);
	}

	public void setNormal() {
		changeID(Config.NEW_NODE_ID, NORMAL);
	}

	public void setSelected() {
		changeID(Config.SELECTED_NODE_ID, SELECTED);
	}

	@Override
	public boolean isMarker() {
		return true;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static void drawLine(WayPointNode nodeA, WayPointNode nodeB) {
		int x1 = nodeA.getX(), y1 = nodeA.getY(), z1 = nodeA.getZ();
		int x2 = nodeB.getX(), y2 = nodeB.getY(), z2 = nodeB.getZ();
		int modX = x1 - x2 > 0 ? -1 : 1;
		int modY = y1 - y2 > 0 ? -1 : 1;
		int modZ = z1 - z2 > 0 ? -1 : 1;

		int diffX = Math.abs(x1 - x2);
		int diffY = Math.abs(y1 - y2);
		int diffZ = Math.abs(z1 - z2);

		int distance = (int) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);

		int steps = distance / 40;

		List<WayPointNode> lineNodes = new ArrayList<>();

		for (int i = 0; i < steps; i++) {
			x1 = x1 + modX * diffX / steps;
			y1 = y1 + modY * diffY / steps;
			z1 = z1 + modZ * diffZ / steps;

			lineNodes.add(WayPointNode.spawn(LINE_TYPE, lineId, x1, y1, z1));
		}

		nodeA.addLineInfo(nodeB, lineNodes);
		nodeB.addLineInfo(nodeA, lineNodes);
	}

	public void addLineInfo(WayPointNode node, List<WayPointNode> line) {
		linkLists.put(node, line);
	}

	public static void eraseLine(WayPointNode target, WayPointNode selectedNode) {
		List<WayPointNode> lineNodes = target.getLineInfo(selectedNode);
		if (lineNodes == null) {
			return;
		}
		for (WayPointNode node : lineNodes) {
			node.decayMe();
		}
		target.eraseLine(selectedNode);
		selectedNode.eraseLine(target);
	}

	public void eraseLine(WayPointNode target) {
		linkLists.remove(target);
	}

	private List<WayPointNode> getLineInfo(WayPointNode selectedNode) {
		return linkLists.get(selectedNode);
	}

	public static void setLineId(int line_id) {
		lineId = line_id;
	}

	public List<WayPointNode> getLineNodes() {
		List<WayPointNode> list = new ArrayList<>();

		for (List<WayPointNode> points : linkLists.values()) {
			list.addAll(points);
		}

		return list;
	}
}