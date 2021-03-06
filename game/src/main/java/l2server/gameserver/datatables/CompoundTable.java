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
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pere
 */

public class CompoundTable {
	private static Logger log = LoggerFactory.getLogger(CompoundTable.class.getName());

	public class Combination {
		private final int item1;
		private final int item2;
		private final int result;
		private final int chance;

		public Combination(int item1, int item2, int result, int chance) {
			this.item1 = item1;
			this.item2 = item2;
			this.result = result;
			this.chance = chance;
		}

		public int getItem1() {
			return item1;
		}

		public int getItem2() {
			return item2;
		}

		public int getResult() {
			return result;
		}

		public int getChance() {
			return chance;
		}
	}

	private final Map<Integer, Combination> combinations = new HashMap<>();
	private final Set<Integer> combinable = new HashSet<>();

	private CompoundTable() {
	}

	@Reload("compound")
	@Load
	public void load() {
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/compound.xml");
		if (!file.exists()) {
			file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "/compound.xml");
		}

		XmlDocument doc = new XmlDocument(file);
		combinations.clear();

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("combination")) {
				int item1 = d.getInt("item1");
				int item2 = d.getInt("item2");
				int result = d.getInt("result");
				int chance = d.getInt("chance");
				combinations.put(getHash(item1, item2), new Combination(item2, item2, result, chance));
				combinable.add(item1);
				combinable.add(item2);
			}
		}

		log.info("CompoundTable: Loaded " + combinations.size() + " combinations.");
	}

	public Combination getCombination(int item1, int item2) {
		return combinations.get(getHash(item1, item2));
	}

	public boolean isCombinable(int itemId) {
		return combinable.contains(itemId);
	}

	private int getHash(int item1, int item2) {
		return Math.min(item1, item2) * 100000 + Math.max(item1, item2);
	}

	public static CompoundTable getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final CompoundTable instance = new CompoundTable();
	}
}
