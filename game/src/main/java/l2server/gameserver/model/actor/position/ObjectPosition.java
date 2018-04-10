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

package l2server.gameserver.model.actor.position;

import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.util.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectPosition {
	private static Logger log = LoggerFactory.getLogger(ObjectPosition.class.getName());


	// =========================================================
	// Data Field
	private WorldObject activeObject;
	private int heading = 0;
	private Point3D worldPosition;
	private WorldRegion worldRegion; // Object localization : Used for items/chars that are seen in the world
	
	// =========================================================
	// Constructor
	public ObjectPosition(WorldObject activeObject) {
		this.activeObject = activeObject;
		setWorldRegion(World.getInstance().getRegion(getWorldPosition()));
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Set the x,y,z position of the WorldObject and if necessary modify its worldRegion.<BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> worldRegion != null</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Update position during and after movement, or after teleport </li><BR>
	 */
	public final void setXYZ(int x, int y, int z) {
		assert getWorldRegion() != null;
		
		setWorldPosition(x, y, z);
		
		try {
			if (World.getInstance().getRegion(getWorldPosition()) != getWorldRegion()) {
				updateWorldRegion();
			}
		} catch (Exception e) {
			log.warn("Object at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
			badCoords();
		}
	}
	
	/**
	 * Called on setXYZ exception.<BR><BR>
	 * <B><U> Overwritten in </U> :</B><BR><BR>
	 * <li> CharPosition</li>
	 * <li> PcPosition</li><BR>
	 */
	protected void badCoords() {
	
	}
	
	/**
	 * Set the x,y,z position of the WorldObject and make it invisible.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A WorldObject is invisble if <B>hidden</B>=true or <B>worldregion</B>==null <BR><BR>
	 * <p>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> worldregion==null <I>(WorldObject is invisible)</I></li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create a Door</li>
	 * <li> Restore Player</li><BR>
	 */
	public final void setXYZInvisible(int x, int y, int z) {
		if (x > World.MAP_MAX_X) {
			x = World.MAP_MAX_X - 5000;
		}
		if (x < World.MAP_MIN_X) {
			x = World.MAP_MIN_X + 5000;
		}
		if (y > World.MAP_MAX_Y) {
			y = World.MAP_MAX_Y - 5000;
		}
		if (y < World.MAP_MIN_Y) {
			y = World.MAP_MIN_Y + 5000;
		}
		
		setWorldPosition(x, y, z);
		getActiveObject().setIsVisible(false);
	}
	
	/**
	 * checks if current object changed its region, if so, update referencies
	 */
	public void updateWorldRegion() {
		if (!getActiveObject().isVisible()) {
			return;
		}
		
		WorldRegion newRegion = World.getInstance().getRegion(getWorldPosition());
		if (newRegion != getWorldRegion()) {
			getWorldRegion().removeVisibleObject(getActiveObject());
			
			setWorldRegion(newRegion);
			
			// Add the L2Oject spawn to visibleObjects and if necessary to allplayers of its WorldRegion
			getWorldRegion().addVisibleObject(getActiveObject());
		}
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	public WorldObject getActiveObject() {
		return activeObject;
	}
	
	public final int getHeading() {
		return heading;
	}
	
	public final void setHeading(int value) {
		heading = value;
	}
	
	/**
	 * Return the x position of the WorldObject.
	 */
	public final int getX() {
		return getWorldPosition().getX();
	}
	
	public final void setX(int value) {
		getWorldPosition().setX(value);
	}
	
	/**
	 * Return the y position of the WorldObject.
	 */
	public final int getY() {
		return getWorldPosition().getY();
	}
	
	public final void setY(int value) {
		getWorldPosition().setY(value);
	}
	
	/**
	 * Return the z position of the WorldObject.
	 */
	public final int getZ() {
		return getWorldPosition().getZ();
	}
	
	public final void setZ(int value) {
		getWorldPosition().setZ(value);
	}
	
	public final Point3D getWorldPosition() {
		if (worldPosition == null) {
			worldPosition = new Point3D(0, 0, 0);
		}
		return worldPosition;
	}
	
	public final void setWorldPosition(int x, int y, int z) {
		getWorldPosition().setXYZ(x, y, z);
	}
	
	public final void setWorldPosition(Point3D newPosition) {
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}
	
	public final WorldRegion getWorldRegion() {
		return worldRegion;
	}
	
	public void setWorldRegion(WorldRegion value) {
		if (worldRegion != null && getActiveObject() instanceof Creature) // confirm revalidation of old region's zones
		{
			if (value != null) {
				worldRegion.revalidateZones((Creature) getActiveObject()); // at world region change
			} else {
				worldRegion.removeFromZones((Creature) getActiveObject()); // at world region change
			}
		}
		
		worldRegion = value;
	}
}
