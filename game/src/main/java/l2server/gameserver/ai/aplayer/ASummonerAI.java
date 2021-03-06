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

package l2server.gameserver.ai.aplayer;

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.templates.skills.SkillTargetType;

/**
 * @author Pere
 */
public class ASummonerAI extends APlayerAI {
	public ASummonerAI(Creature creature) {
		super(creature);
	}
	
	@Override
	protected int[] getRandomGear() {
		return new int[]{30259, 19709, 19710, 19711, 19712, 19713, 18099, 19464, 19463, 19458, 17623, 35570, 34860, 19462, 19454, 35920, 30315};
	}
	
	@Override
	protected boolean interactWith(Creature target) {
		if (super.interactWith(target)) {
			return true;
		}
		
		if (player.getCurrentMp() > player.getMaxMp() * 0.7 || player.getCurrentHp() < player.getMaxHp() * 0.5 ||
				player.getTarget() instanceof Playable) {
			for (Skill skill : player.getAllSkills()) {
				if (!skill.isOffensive() || skill.getTargetType() != SkillTargetType.TARGET_ONE) {
					continue;
				}
				
				if (player.useMagic(skill, true, false)) {
					break;
				}
			}
		}
		
		return true;
	}
	
	@Override
	protected void think() {
		super.think();
	}
}
