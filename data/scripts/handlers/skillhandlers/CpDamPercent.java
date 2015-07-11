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
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.stats.Formulas;
import l2tserver.gameserver.templates.skills.L2SkillType;

/*
 * Just a quick draft to support Wrath skill. Missing angle based calculation etc.
 */

public class CpDamPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CPDAMPERCENT
	};
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.ISkillHandler#useSkill(l2tserver.gameserver.model.actor.L2Character, l2tserver.gameserver.model.L2Skill, l2tserver.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		double ssMul = L2ItemInstance.CHARGED_NONE;
		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				ssMul = weaponInst.getChargedSpiritShot();
				weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				ssMul = weaponInst.getChargedSoulShot();
				weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			if (skill.isMagic())
			{
				ssMul = activeSummon.getChargedSpiritShot();
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				ssMul = activeSummon.getChargedSoulShot();
				activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Npc)
		{
			if (skill.isMagic())
			{
				ssMul = ((L2Npc) activeChar)._soulshotcharged ? L2ItemInstance.CHARGED_SOULSHOT : L2ItemInstance.CHARGED_NONE;
				((L2Npc) activeChar)._soulshotcharged = false;
			}
			else
			{
				ssMul = ((L2Npc) activeChar)._spiritshotcharged ? L2ItemInstance.CHARGED_SPIRITSHOT : L2ItemInstance.CHARGED_NONE;
				((L2Npc) activeChar)._spiritshotcharged = false;
			}
		}
		
		for (L2Object obj: targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			
			L2Character target = (L2Character)obj;
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance)target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead() || target.isInvul(activeChar)
					|| (target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar))
			{
				continue;
			}
			
			byte shld = Formulas.calcShldUse(activeChar, target, skill);
			
			int damage = (int) (target.getCurrentCp() * (skill.getPower() / 100));
			
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			skill.getEffects(activeChar, target, new Env(shld, ssMul));
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.setCurrentCp(target.getCurrentCp() - damage);
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
