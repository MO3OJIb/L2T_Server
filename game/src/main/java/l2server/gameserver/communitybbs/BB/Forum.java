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

package l2server.gameserver.communitybbs.BB;

import l2server.DatabasePool;
import l2server.gameserver.communitybbs.Manager.ForumsBBSManager;
import l2server.gameserver.communitybbs.Manager.TopicBBSManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Forum {
	private static Logger log = LoggerFactory.getLogger(Forum.class.getName());

	//type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	//perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;

	private List<Forum> children;
	private Map<Integer, Topic> topic;
	private int forumId;
	private String forumName;
	//private int ForumParent;
	private int forumType;
	private int forumPost;
	private int forumPerm;
	private Forum fParent;
	private int ownerID;
	private boolean loaded = false;

	/**
	 * Creates new instance of Forum. When you create new forum, use
	 * {@link l2server.gameserver.communitybbs.Manager.ForumsBBSManager#
	 * addForum(l2server.gameserver.communitybbs.BB.Forum)} to add forum
	 * to the forums manager.
	 */
	public Forum(int Forumid, Forum FParent) {
		forumId = Forumid;
		fParent = FParent;
		children = new ArrayList<>();
		topic = new HashMap<>();

		/*load();
		getChildren();	*/
	}

	public Forum(String name, Forum parent, int type, int perm, int OwnerID) {
		forumName = name;
		forumId = ForumsBBSManager.getInstance().getANewID();
		//ForumParent = parent.getID();
		forumType = type;
		forumPost = 0;
		forumPerm = perm;
		fParent = parent;
		ownerID = OwnerID;
		children = new ArrayList<>();
		topic = new HashMap<>();
		parent.children.add(this);
		ForumsBBSManager.getInstance().addForum(this);
		loaded = true;
	}

	/**
	 *
	 */
	private void load() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();

			if (result.next()) {
				forumName = result.getString("forum_name");
				//ForumParent = result.getInt("forum_parent");
				forumPost = result.getInt("forum_post");
				forumType = result.getInt("forum_type");
				forumPerm = result.getInt("forum_perm");
				ownerID = result.getInt("forum_owner_id");
			}
			result.close();
			statement.close();
		} catch (Exception e) {
			log.warn("Data error on Forum " + forumId + " : " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				Topic t = new Topic(Topic.ConstructorType.RESTORE,
						result.getInt("topic_id"),
						result.getInt("topic_forum_id"),
						result.getString("topic_name"),
						result.getLong("topic_date"),
						result.getString("topic_ownername"),
						result.getInt("topic_ownerid"),
						result.getInt("topic_type"),
						result.getInt("topic_reply"));
				topic.put(t.getID(), t);
				if (t.getID() > TopicBBSManager.getInstance().getMaxID(this)) {
					TopicBBSManager.getInstance().setMaxID(t.getID(), this);
				}
			}
			result.close();
			statement.close();
		} catch (Exception e) {
			log.warn("Data error on Forum " + forumId + " : " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	/**
	 *
	 */
	private void getChildren() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
			statement.setInt(1, forumId);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				Forum f = new Forum(result.getInt("forum_id"), this);
				children.add(f);
				ForumsBBSManager.getInstance().addForum(f);
			}
			result.close();
			statement.close();
		} catch (Exception e) {
			log.warn("Data error on Forum (children): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	public int getTopicSize() {
		vload();
		return topic.size();
	}

	public Topic getTopic(int j) {
		vload();
		return topic.get(j);
	}

	public void addTopic(Topic t) {
		vload();
		topic.put(t.getID(), t);
	}

	public int getID() {
		return forumId;
	}

	public String getName() {
		vload();
		return forumName;
	}

	public int getType() {
		vload();
		return forumType;
	}

	public Forum getChildByName(String name) {
		vload();
		for (Forum f : children) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

	public void rmTopicByID(int id) {
		topic.remove(id);
	}

	/**
	 *
	 */
	public void insertIntoDb() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) VALUES (?,?,?,?,?,?,?)");
			statement.setInt(1, forumId);
			statement.setString(2, forumName);
			statement.setInt(3, fParent.getID());
			statement.setInt(4, forumPost);
			statement.setInt(5, forumType);
			statement.setInt(6, forumPerm);
			statement.setInt(7, ownerID);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Error while saving new Forum to db " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}

	/**
	 *
	 */
	public void vload() {
		if (!loaded) {
			load();
			getChildren();
			loaded = true;
		}
	}
}
