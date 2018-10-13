package net.libercraft.liberusers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.libercraft.libercore.Database;
import net.libercraft.liberusers.UserManager.User;

public class UserDatabase extends Database {
	
	public UserDatabase() {
		super();
	}

	@Override
	public List<String> getTables() {
		List<String> tables = new ArrayList<>();
		tables.add("users");
		return tables;
	}

	@Override
	public List<String> getColumns(String table) {
		List<String> columns = new ArrayList<>();
		switch (table) {
		case "users":
			columns.add("uid INTEGER PRIMARY KEY AUTOINCREMENT");
			columns.add("nickname TEXT NOT NULL UNIQUE");
			columns.add("mcuuid TEXT");
			columns.add("mcname TEXT");
			columns.add("ircname TEXT");
			columns.add("irckey TEXT");
			columns.add("rank INTEGER NOT NULL");
			columns.add("birthday INTEGER");
			columns.add("deaths INTEGER");
			columns.add("greet TEXT");
			columns.add("regdone INTEGER NOT NULL DEFAULT '0'");
			columns.add("seenig INTEGER");
			columns.add("seenirc INTEGER");
			columns.add("pvp INTEGER NOT NULL DEFAULT '0'");
			columns.add("warningpoints INTEGER");
			columns.add("warningdate INTEGER");
			columns.add("warninglog TEXT");
			break;
		default:
			return null;
		}
		return columns;
	}
	
	public User getUserFromMCName(String mcName) {
		Object result = getFromCondition(this, "users", "uid", new Condition("mcname", "=!^", mcName));
		return (result != null) ? getUserFromUid(Integer.parseInt((String)result)) : null;
	}
	
	public User getUserFromNickname(String nickname) {
		Object result = getFromCondition(this, "users", "uid", new Condition("nickname", "=!^", nickname));
		if (result == null)
			result = getFromCondition(this, "users", "uid", new Condition("mcname", "=!^", nickname));
		if (result == null)
			result = getFromCondition(this, "users", "uid", new Condition("ircname", "=!^", nickname));
		return (result != null) ? getUserFromUid(Integer.parseInt((String)result)) : null;
	}
	
	public User getUserFromUUID(UUID uuid) {
		Object result = getFromKey(this, "users", "mcuuid", uuid.toString(), "uid");
		return (result != null) ? getUserFromUid(Integer.parseInt((String)result)) : null;
	}
	
	public User getUserFromIRCName(String ircname) {
		Object result = getFromCondition(this, "users", "uid", new Condition("ircname", "=!^", ircname));
		return (result != null) ? getUserFromUid(Integer.parseInt((String)result)) : null;
	}
	
	public User getUserFromUid(int uid) {
		String nickname = (String) getFromKey(this, "users", "uid", uid, "nickname");
		String mcUuid = (String) getFromKey(this, "users", "uid", uid, "mcuuid");
		String mcName = (String) getFromKey(this, "users", "uid", uid, "mcname");
		String ircName = (String) getFromKey(this, "users", "uid", uid, "ircname");
		String ircKey = (String) getFromKey(this, "users", "uid", uid, "irckey");
		
		Object rRank = getFromKey(this, "users", "uid", uid, "rank");
		int rank = (rRank == null) ? 0 : Integer.parseInt((String)rRank);
		
		Object rBirthday = getFromKey(this, "users", "uid", uid, "birthday");
		long birthday = (rBirthday == null) ? -1 : Long.parseLong((String)rBirthday);
		
		Object rDeaths = getFromKey(this, "users", "uid", uid, "deaths");
		int deaths = (rDeaths == null) ? 0 : Integer.parseInt((String)rDeaths);
		
		String greet = (String) getFromKey(this, "users", "uid", uid, "greet");
		
		Object rRegdone = getFromKey(this, "users", "uid", uid, "regdone");
		boolean regdone = ((String)rRegdone).equals("1");
		
		Object rSeenig = getFromKey(this, "users", "uid", uid, "seenig");
		long seenig = (rSeenig == null) ? -1 : Long.parseLong((String)rSeenig);
		
		Object rSeenirc = getFromKey(this, "users", "uid", uid, "seenirc");
		long seenirc = (rSeenirc == null) ? -1 : Long.parseLong((String)rSeenirc);
		
		Object rPvp = getFromKey(this, "users", "uid", uid, "pvp");
		boolean pvp = ((String)rPvp).equals("1");
		
		Object rWarningpoints = getFromKey(this, "users", "uid", uid, "warningpoints");
		int warningpoints = (rWarningpoints == null) ? 0 : Integer.parseInt((String)rWarningpoints);
		
		Object rWarningdate = getFromKey(this, "users", "uid", uid, "warningdate");
		long warningdate = (rWarningpoints == null) ? -1 : Long.parseLong((String)rWarningdate);
		
		Object rWarninglog = getFromKey(this, "users", "uid", uid, "warningpoints");
		String warninglog = (rWarningpoints == null) ? null : (String)rWarninglog;
		
		User user = new User(uid, nickname);
		user.mcUuid = (mcUuid != null) ? UUID.fromString(mcUuid) : null;
		user.mcName = mcName;
		user.ircName = ircName;
		user.ircKey = ircKey;
		user.rank = rank;
		user.birthday = birthday;
		user.deaths = deaths;
		user.greet = greet;
		user.regdone = regdone;
		user.seenig = seenig;
		user.seenirc = seenirc;
		user.pvp = pvp;
		user.warningpoints = warningpoints;
		user.warningdate = warningdate;
		user.warninglog = warninglog;
		
		return user;
	}
	
	public User newUser(String nickname) {
		insert(this, "users", Arrays.asList(new String[] {"nickname", "rank"}), Arrays.asList(new Object[] {nickname, 0}));
		return getUserFromNickname(nickname);
	}
	
	public void storeUser(User user) {
		update(this, "users", "nickname", user.nickname, "uid", user.uid);
		update(this, "users", "mcuuid", user.mcUuid.toString(), "uid", user.uid);
		update(this, "users", "mcname", user.mcName, "uid", user.uid);
		update(this, "users", "ircname", user.ircName, "uid", user.uid);
		update(this, "users", "irckey", user.ircKey, "uid", user.uid);
		update(this, "users", "rank", user.rank, "uid", user.uid);
		update(this, "users", "birthday", user.birthday, "uid", user.uid);
		update(this, "users", "deaths", user.deaths, "uid", user.uid);
		update(this, "users", "greet", user.greet, "uid", user.uid);
		update(this, "users", "regdone", user.regdone, "uid", user.uid);
		update(this, "users", "seenig", user.seenig, "uid", user.uid);
		update(this, "users", "seenirc", user.seenirc, "uid", user.uid);
		update(this, "users", "pvp", user.pvp, "uid", user.uid);
		update(this, "users", "warningpoints", user.warningpoints, "uid", user.uid);
		update(this, "users", "warningdate", user.warningdate, "uid", user.uid);
		update(this, "users", "warninglog", user.warninglog, "uid", user.uid);
	}
}
