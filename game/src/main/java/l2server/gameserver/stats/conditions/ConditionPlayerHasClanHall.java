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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;

import java.util.ArrayList;

/**
 * The Class ConditionPlayerHasClanHall.
 *
 * @author MrPoke
 */
public final class ConditionPlayerHasClanHall extends Condition {
	private final ArrayList<Integer> clanHall;
	
	/**
	 * Instantiates a new condition player has clan hall.
	 *
	 * @param clanHall the clan hall
	 */
	public ConditionPlayerHasClanHall(ArrayList<Integer> clanHall) {
		this.clanHall = clanHall;
	}
	
	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see Condition#testImpl(Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (!(env.player instanceof Player)) {
			return false;
		}
		
		L2Clan clan = ((Player) env.player).getClan();
		if (clan == null) {
			return clanHall.size() == 1 && clanHall.get(0) == 0;
		}
		
		if (env.player.isGM()) {
			return true;
		}
		
		// All Clan Hall
		if (clanHall.size() == 1 && clanHall.get(0) == -1) {
			return clan.getHasHideout() > 0;
		}
		
		return clanHall.contains(clan.getHasHideout());
	}
}
