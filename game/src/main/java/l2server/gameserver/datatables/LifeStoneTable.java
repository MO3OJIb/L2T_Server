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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.EnchantEffect;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Augmentation;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ArmorTemplate;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pere
 */
public class LifeStoneTable {
	private static Logger log = LoggerFactory.getLogger(LifeStoneTable.class.getName());

	public static final class EnchantEffectSet {
		private final List<EnchantEffect> enchantEffects;
		private final float chance;

		public EnchantEffectSet(List<EnchantEffect> effects, float chance) {
			enchantEffects = effects;
			this.chance = chance;
		}

		public final EnchantEffect getRandomEnchantEffect() {
			return enchantEffects.get(Rnd.get(enchantEffects.size()));
		}

		public final float getChance() {
			return chance;
		}
	}

	public static final class EnchantEffectGroup {
		private final List<EnchantEffectSet> effects;

		public EnchantEffectGroup(List<EnchantEffectSet> effects) {
			this.effects = effects;
		}

		public final EnchantEffect getRandomEffect() {
			float random = Rnd.get(10000) / 100.0f;
			float current = 0.0f;
			for (EnchantEffectSet set : effects) {
				if (random < current + set.getChance()) {
					return set.getRandomEnchantEffect();
				}

				current += set.getChance();
			}

			return effects.get(0).getRandomEnchantEffect();
		}
	}

	public static final class LifeStone {
		// lifestone level to player level table
		private static final int[] LEVELS = {46, 49, 52, 55, 58, 61, 64, 67, 70, 76, 80, 82, 84, 85, 95, 99};

		private final int grade;
		private final int level;
		private final Map<String, EnchantEffectGroup[]> effects = new HashMap<>();

		public LifeStone(int grade, int level) {
			this.grade = grade;
			this.level = level;
		}

		public final int getLevel() {
			return level;
		}

		public final int getGrade() {
			return grade;
		}

		public final int getPlayerLevel() {
			return LEVELS[level];
		}

		public final void setEffectGroup(String type, int order, EnchantEffectGroup group) {
			EnchantEffectGroup[] augments = effects.get(type);
			if (augments == null) {
				augments = new EnchantEffectGroup[2];
				effects.put(type, augments);
			}

			augments[order] = group;
		}

		public final EnchantEffect getRandomEffect(String type, int order) {
			EnchantEffectGroup[] augments = effects.get(type);
			if (augments == null || augments[order] == null) {
				log.warn("Null augment: " + type + ", " + order);
				return null;
			}

			return augments[order].getRandomEffect();
		}
	}

	public static LifeStoneTable getInstance() {
		return SingletonHolder.instance;
	}

	public static final int GRADE_NONE = 0;
	public static final int GRADE_MID = 1;
	public static final int GRADE_HIGH = 2;
	public static final int GRADE_TOP = 3;
	public static final int GRADE_LEG = 4;
	public static final int GRADE_ACC = 5; // Accessory LS
	public static final int GRADE_ARIA = 6; // Aria's LS

	protected static final int GEMSTONE_D = 2130;
	protected static final int GEMSTONE_C = 2131;
	protected static final int GEMSTONE_B = 2132;
	protected static final int GEMSTONE_A = 2133;
	protected static final int GEMSTONE_R = 19440;

	private final Map<Integer, LifeStone> lifeStones = new HashMap<>();

	// =========================================================
	// Constructor
	private LifeStoneTable() {
	}
	
	@Load(dependencies = EnchantEffectTable.class)
	public final void load() {
		lifeStones.clear();

		// Load the skillmap
		// Note: the skillmap data is only used when generating new augmentations
		// the client expects a different id in order to display the skill in the
		// items description...
		try {
			File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/lifeStones.xml");

			if (!file.exists()) {
				file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "/lifeStones.xml");
			}

			if (!file.exists()) {
				if (Config.DEBUG) {
					log.info("The life stones file is missing.");
				}
				return;
			}

			XmlDocument doc = new XmlDocument(file);
			for (XmlNode stoneNode : doc.getChildren()) {
				if (!stoneNode.getName().equalsIgnoreCase("lifeStone")) {
					continue;
				}

				int id = stoneNode.getInt("id");
				int grade = stoneNode.getInt("grade");
				int level = stoneNode.getInt("level");
				LifeStone lifeStone = new LifeStone(grade, level);

				for (XmlNode groupNode : stoneNode.getChildren()) {
					if (!groupNode.getName().equalsIgnoreCase("augmentGroup")) {
						continue;
					}

					String[] weaponTypes = groupNode.getString("weaponType").split(",");
					int order = groupNode.getInt("order");

					List<EnchantEffectSet> sets = new ArrayList<>();
					for (XmlNode setNode : groupNode.getChildren()) {
						if (!setNode.getName().equalsIgnoreCase("augments")) {
							continue;
						}

						String[] ids = setNode.getString("ids").split(",");
						float chance = setNode.getFloat("chance");
						List<EnchantEffect> augments = new ArrayList<>();
						for (String idRange : ids) {
							if (idRange.contains("-")) {
								int start = Integer.parseInt(idRange.substring(0, idRange.indexOf("-")));
								int end = Integer.parseInt(idRange.substring(idRange.indexOf("-") + 1));
								for (int augmentId = start; augmentId <= end; augmentId++) {
									augments.add(EnchantEffectTable.getInstance().getEffect(augmentId));
								}
							} else {
								augments.add(EnchantEffectTable.getInstance().getEffect(Integer.parseInt(idRange)));
							}
						}

						sets.add(new EnchantEffectSet(augments, chance));
					}

					for (String weaponType : weaponTypes) {
						lifeStone.setEffectGroup(weaponType, order, new EnchantEffectGroup(sets));
					}
				}

				lifeStones.put(id, lifeStone);
			}

			log.info("LifeStoneTable: Loaded " + lifeStones.size() + " life stones.");
		} catch (Exception e) {
			log.error("Error loading life stone data", e);
		}
	}

	/**
	 * Generate a new random augmentation
	 *
	 * @return L2Augmentation
	 */
	public L2Augmentation generateRandomAugmentation(LifeStone lifeStone, Item targetItem) {
		switch (targetItem.getItem().getBodyPart()) {
			case ItemTemplate.SLOT_LR_FINGER:
				return generateRandomAugmentation(lifeStone, "ring");
			case ItemTemplate.SLOT_LR_EAR:
				return generateRandomAugmentation(lifeStone, "earring");
			case ItemTemplate.SLOT_NECK:
				return generateRandomAugmentation(lifeStone, "necklace");
			default:
				return generateRandomAugmentation(lifeStone, targetItem.getWeaponItem().isMagicWeapon() ? "mage" : "warrior");
		}
	}

	private L2Augmentation generateRandomAugmentation(LifeStone lifeStone, String weaponType) {
		EnchantEffect augment1 = lifeStone.getRandomEffect(weaponType, 0);
		EnchantEffect augment2 = lifeStone.getRandomEffect(weaponType, 1);
		if (augment1 == null) {
			return null;
		}

		return new L2Augmentation(augment1, augment2);
	}

	public final LifeStone getLifeStone(int itemId) {
		return lifeStones.get(itemId);
	}

	/*
	 * Checks player, source item, lifestone and gemstone validity for augmentation process
	 */
	public final boolean isValid(Player player, Item item, Item refinerItem, Item gemStones) {
		if (!isValid(player, item, refinerItem)) {
			return false;
		}

		// GemStones must belong to owner
		if (gemStones.getOwnerId() != player.getObjectId()) {
			return false;
		}
		// .. and located in inventory
		if (gemStones.getLocation() != Item.ItemLocation.INVENTORY) {
			return false;
		}

		final int grade = item.getItem().getItemGrade();
		final LifeStone ls = getLifeStone(refinerItem.getItemId());

		// Check for item id
		if (getGemStoneId(grade, ls.getGrade()) != gemStones.getItemId()) {
			return false;
		}
		// Count must be greater or equal of required number
		return getGemStoneCount(grade, ls.getGrade()) <= gemStones.getCount();
	}

	/*
	 * Checks player, source item and lifestone validity for augmentation process
	 */
	public final boolean isValid(Player player, Item item, Item refinerItem) {
		if (!isValid(player, item)) {
			return false;
		}

		// Item must belong to owner
		if (refinerItem.getOwnerId() != player.getObjectId()) {
			return false;
		}
		// Lifestone must be located in inventory
		if (refinerItem.getLocation() != Item.ItemLocation.INVENTORY) {
			return false;
		}

		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		if (ls == null) {
			return false;
		}

		if (item.getItem().isEpic() && ls.getGrade() != GRADE_ARIA) {
			return false;
		}

		// weapons can't be augmented with accessory ls
		if (item.getItem() instanceof WeaponTemplate && (ls.getGrade() == GRADE_ACC || ls.getGrade() == GRADE_ARIA)) {
			return false;
		}
		// and accessory can't be augmented with weapon ls
		if (item.getItem() instanceof ArmorTemplate && ls.getGrade() < GRADE_ACC) {
			return false;
		}
		// check for level of the lifestone
		return player.getLevel() >= ls.getPlayerLevel();
	}

	/*
	 * Check both player and source item conditions for augmentation process
	 */
	public static boolean isValid(Player player, Item item) {
		if (!isValid(player)) {
			return false;
		}

		// Item must belong to owner
		if (item.getOwnerId() != player.getObjectId()) {
			return false;
		}
		if (item.isAugmented()) {
			return false;
		}
		if (item.isHeroItem() && !item.getItem().isAugmentable()) {
			return false;
		}
		if (item.isShadowItem()) {
			return false;
		}
		if (item.isCommonItem()) {
			return false;
		}
		if (item.isEtcItem()) {
			return false;
		}
		if (item.isTimeLimitedItem()) {
			return false;
		}
		//if (item.isPvp())
		//	return false;
		if (item.getItem().getCrystalType() < ItemTemplate.CRYSTAL_C) {
			return false;
		}

		// Source item can be equipped or in inventory
		switch (item.getLocation()) {
			case INVENTORY:
			case EQUIPPED:
				break;
			default:
				return false;
		}

		if (item.getItem() instanceof WeaponTemplate) {
			switch (((WeaponTemplate) item.getItem()).getItemType()) {
				case NONE:
				case FISHINGROD:
					return false;
				default:
					break;
			}
		} else if (item.getItem() instanceof ArmorTemplate) {
			// only accessories can be augmented
			switch (item.getItem().getBodyPart()) {
				case ItemTemplate.SLOT_LR_FINGER:
				case ItemTemplate.SLOT_LR_EAR:
				case ItemTemplate.SLOT_NECK:
					break;
				default:
					return false;
			}
		} else {
			return false; // neither weapon nor armor ?
		}

		// blacklist check
		return item.getItem().isAugmentable();
	}

	/*
	 * Check if player's conditions valid for augmentation process
	 */
	public static boolean isValid(Player player) {
		if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION));
			return false;
		}
		if (player.getActiveTradeList() != null) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING));
			return false;
		}
		if (player.isDead()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD));
			return false;
		}
		if (player.isParalyzed()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED));
			return false;
		}
		if (player.isFishing()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING));
			return false;
		}
		if (player.isSitting()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN));
			return false;
		}
		if (player.isCursedWeaponEquipped()) {
			return false;
		}
		return !(player.isEnchanting() || player.isProcessingTransaction());
	}

	/*
	 * Returns GemStone itemId based on item grade
	 */
	public static int getGemStoneId(int itemGrade, int lifeStoneGrade) {
		if (lifeStoneGrade == GRADE_ARIA) {
			return GEMSTONE_R;
		}

		switch (itemGrade) {
			case ItemTemplate.CRYSTAL_C:
			case ItemTemplate.CRYSTAL_B:
				return GEMSTONE_D;
			case ItemTemplate.CRYSTAL_A:
			case ItemTemplate.CRYSTAL_S:
				return GEMSTONE_C;
			case ItemTemplate.CRYSTAL_S80:
			case ItemTemplate.CRYSTAL_S84:
				return GEMSTONE_B;
			case ItemTemplate.CRYSTAL_R:
			case ItemTemplate.CRYSTAL_R95:
			case ItemTemplate.CRYSTAL_R99:
				return GEMSTONE_A;
			default:
				return 0;
		}
	}

	/*
	 * Returns GemStone count based on item grade and lifestone grade
	 * (different for weapon and accessory augmentation)
	 */
	public static int getGemStoneCount(int itemGrade, int lifeStoneGrade) {
		switch (lifeStoneGrade) {
			case GRADE_ARIA:
				return 100;
			case GRADE_ACC:
				switch (itemGrade) {
					case ItemTemplate.CRYSTAL_C:
						return 200;
					case ItemTemplate.CRYSTAL_B:
						return 300;
					case ItemTemplate.CRYSTAL_A:
						return 200;
					case ItemTemplate.CRYSTAL_S:
						return 250;
					case ItemTemplate.CRYSTAL_S80:
						return 360;
					case ItemTemplate.CRYSTAL_S84:
						return 480;
					case ItemTemplate.CRYSTAL_R:
						return 26;
					case ItemTemplate.CRYSTAL_R95:
						return 90;
					case ItemTemplate.CRYSTAL_R99:
						return 236;
					default:
						return 0;
				}
			default:
				switch (itemGrade) {
					case ItemTemplate.CRYSTAL_C:
						return 20;
					case ItemTemplate.CRYSTAL_B:
						return 30;
					case ItemTemplate.CRYSTAL_A:
						return 20;
					case ItemTemplate.CRYSTAL_S:
						return 25;
					case ItemTemplate.CRYSTAL_S80:
						return 36;
					case ItemTemplate.CRYSTAL_S84:
						return 48;
					case ItemTemplate.CRYSTAL_R:
						return 13;
					case ItemTemplate.CRYSTAL_R95:
						return 45;
					case ItemTemplate.CRYSTAL_R99:
						return 118;
					default:
						return 0;
				}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final LifeStoneTable instance = new LifeStoneTable();
	}
}
