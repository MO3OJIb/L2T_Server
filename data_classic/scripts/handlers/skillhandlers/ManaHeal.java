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
package handlers.skillhandlers;

import l2tserver.gameserver.handler.ISkillHandler;
import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.StatusUpdate;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.stats.Stats;
import l2tserver.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class ManaHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MANAHEAL,
		L2SkillType.MANARECHARGE
	};
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.ISkillHandler#useSkill(l2tserver.gameserver.model.actor.L2Character, l2tserver.gameserver.model.L2Skill, l2tserver.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isInvul() || (target != actChar && target.getFaceoffTarget() != null && target.getFaceoffTarget() != actChar))
				continue;
			
			double mp = skill.getPower();
			mp = (skill.getSkillType() == L2SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
			
			//from CT2 u will receive exact MP, u can't go over it, if u have full MP and u get MP buff, u will receive 0MP restored message
			if ((target.getCurrentMp() + mp) >= target.getMaxMp())
				mp = target.getMaxMp() - target.getCurrentMp();

			mp = Math.min(target.calcStat(Stats.GAIN_MP_LIMIT, target.getMaxMp(), null, null), target.getCurrentMp() + mp) - target.getCurrentMp();
			
			target.setCurrentMp(mp + target.getCurrentMp());
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);
			
			SystemMessage sm;
			if (actChar instanceof L2PcInstance && actChar != target)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
				sm.addString(actChar.getName());
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			
			if (skill.hasEffects())
			{
				//target.stopSkillEffects(skill.getId());
				skill.getEffects(actChar, target);
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(skill);
				target.sendPacket(sm);
			}
		}
		
		if (skill.hasSelfEffects())
		{
			L2Abnormal effect = actChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			skill.getEffectsSelf(actChar);
		}
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
