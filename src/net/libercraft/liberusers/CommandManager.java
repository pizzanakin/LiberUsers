package net.libercraft.liberusers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.libercraft.liberusers.UserManager.User;
import net.md_5.bungee.api.ChatColor;

public class CommandManager implements CommandExecutor, Listener {
	private static CommandManager instance;
	public static List<CommandInterface> commands;
	
	public CommandManager() {
		instance = this;
		commands = new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		User user = LiberUsers.getUD().getUserFromUUID(player.getUniqueId());
		
		CommandInterface command = null;
		for (CommandInterface c:commands)
			if (c.getLabels().contains(label))
				command = c;
		String result = (command == null) ? null : command.attemptExecution(user, args, 1);
		
		if (result != null && result != "")
			player.sendMessage(command.getTag() + ChatColor.RESET + result);
		return true;
	}

	public static abstract interface CommandInterface {
		public abstract int getPermission();
		public abstract int getMode(); // 0 == public only, 1 == private only, 2 == both
		public abstract List<String> getLabels();
		
		public abstract String getTag();
		
		public abstract String execute(User user, String[] args, int mode);
		
		public default String attemptExecution(User user, String[] args, int mode) {
			
			// Check if executer has permission to execute
			if (user.rank < getPermission())
					return "You don't have permission to use this command";
			
			// Check if command is sent using the right mode
			if (mode == 0) 
				if (getMode() == 1)
					return null;
			else if (mode == 1) 
				if (getMode() == 0)
					return null;
			
			return execute(user, args, mode);
		}
		
		public default void register(JavaPlugin source) {
			commands.add(this);
			System.out.println("-------------- Registering this string as a command: " + this.toString().toLowerCase());
			source.getCommand(this.toString().toLowerCase()).setExecutor(instance);
		}
	}
	
	public static enum UserCommand implements CommandInterface {
		
		// Owner commands
		RANK(new String[] {"rank"}, 2, 1),
		
		// Moderator commands
		PROMOTE(new String[] {"promote"}, 2, 1),
		DEMOTE(new String[] {"demote"}, 2, 1),
		VOICE(new String[] {"voice"}, 2, 2),
		ADMINCHAT(new String[] {"adminchat", "a", "ac"}, 2, 1),
		STAFF(new String[] {"staff"}, 2, 1),
		
		// Trusted commands
		SEEN(new String[] {"seen"}, 1, 2),
		SETNICKNAME(new String[] {"setnickname"}, 1, 2),
		SETGREET(new String[] {"setgreet"}, 1, 2),
		
		// Default commands
		DONE(new String[] {"done"}, 0, 1),
		PVP(new String[] {"pvp"}, 0, 1),
		SETBIRTHDAY(new String[] {"setbday", "setbirthday"}, 0, 2),
		SETIRCNAME(new String[] {"setircname"}, 0, 1)
		;
		
		private String[] labels;
		private int permission;
		private int mode; // 0 == public only, 1 == private only, 2 == both
		
		private UserCommand(String[] labels, int permission, int mode) {
			this.labels = labels;
			this.permission = permission;
			this.mode = mode;
		}

		@Override
		public int getPermission() {
			return permission;
		}

		@Override
		public int getMode() {
			return mode;
		}
		
		@Override
		public List<String> getLabels() {
			return Arrays.asList(labels);
		}

		@Override
		public String execute(User user, String[] args, int mode) {
			switch (this) {
			case ADMINCHAT:
				return adminChat(user, args);
			case DEMOTE:
				return demote(user, args);
			case DONE:
				return done(user);
			case PROMOTE:
				return promote(user, args);
			case PVP:
				return pvp(user);
			case RANK:
				return rank(user, args);
			case SEEN:
				return seen(user, args);
			case SETBIRTHDAY:
				return setBirthday(user, args);
			case SETGREET:
				return setGreet(user, args);
			case SETIRCNAME:
				return setIRCName(user, args);
			case SETNICKNAME:
				return setNickname(user, args);
			case STAFF:
				return staff(user);
			case VOICE:
				return voice(args);
			default:
				break;
			}
			return null;
		}
		
		private String adminChat(User user, String[] args) {
			String message = "";
			for (String s:args)
				message += s;
			
			for (Player p:LiberUsers.get().getServer().getOnlinePlayers())
				if (LiberUsers.getUD().getUserFromUUID(p.getUniqueId()).rank >= 2)
					p.sendMessage(ChatColor.GOLD + "[AriChat] " + ChatColor.AQUA + user.getPlayer().getName() + ": " + message);
			return null;
		}
		
		private String demote(User u, String args[]) {
			if (args.length == 0) 
				return "Not enough arguments. Usage: /demote <username>";

			User user = LiberUsers.getUD().getUserFromNickname(args[0]);
			if (user.rank < (u.rank-1) && user.demote()) {
				LiberUsers.getUD().storeUser(user);
				if (user.rank == 0)
					return user.nickname + " is now Default!";
				if (user.rank == 1)
					return user.nickname + " is now Trusted!";
				if (user.rank == 2)
					return user.nickname + " is now Moderator!";
				if (user.rank == 3)
					return user.nickname + " is now Admin!";
				return "User demoted!";
			} else
				return "You can not demote this user any lower";
		}
		
		private String done(User u) {
			u.regdone = true;
			LiberUsers.getUD().storeUser(u);
			
			return "You will no longer be notified about your profile!";
		}
		
		private String promote(User u, String args[]) {
			if (args.length == 0) 
				return "Not enough arguments. Usage: /promote <username>";
			
			User user = LiberUsers.getUD().getUserFromNickname(args[0]);
			if (user.rank < (u.rank-1) && user.promote()) {
				LiberUsers.getUD().storeUser(user);
				if (user.rank == 0)
					return user.nickname + " is now Default!";
				if (user.rank == 1)
					return user.nickname + " is now Trusted!";
				if (user.rank == 2)
					return user.nickname + " is now Moderator!";
				if (user.rank == 3)
					return user.nickname + " is now Admin!";
				return "User promoted!";
			} else
				return "You can not promote this user any higher";
		}
		
		private String pvp(User u) {
			if (u.pvp) {
				u.pvp = false;
				LiberUsers.getUD().storeUser(u);
				return "PVP disabled!";
			} else {
				u.pvp = true;
				LiberUsers.getUD().storeUser(u);
				return "PVP enabled!";
			}
		}
		
		private String rank(User user, String[] args) {
			if (args == null || args.length < 1) 
				return "Not enough arguments. Usage: /rank <username>";
			
			User u = LiberUsers.getUD().getUserFromNickname(args[0]);
			if (user.rank < 4 || args.length == 1) {
				String rank = "";
				if (u.rank == 0)
					rank = "default";
				if (u.rank == 1)
					rank = "trusted";
				if (u.rank == 2)
					rank = "moderator";
				if (u.rank == 3)
					rank = "admin";
				if (u.rank == 4) 
					rank = "owner";
				if (u.rank > 4)
					rank = "wait a second this rank is higher than Pizza? wtf, hax0r!";
				if (u.rank == 101)
					rank = "uber-1337-mainframe-core defragulator";
				return u.nickname + "'s rank is " + rank;
			} else {
				int newRank = Integer.parseInt(args[1]);
				if (u == null) 
					return "Can't find " + args[0] + ", are you sure you spelled it correctly?";
				
				String rank = "";
				if (newRank == 0)
					rank = "default";
				if (newRank == 1)
					rank = "trusted";
				if (newRank == 2)
					rank = "moderator";
				if (newRank == 3)
					rank = "admin";
				if (newRank == 4) 
					rank = "owner";
				if (newRank > 4)
					rank = "wait a second this rank is higher than Pizza? wtf, hax0r!";
				if (newRank == 101)
					rank = "uber-1337-mainframe-core defragulator";
	
				u.rank = newRank;
				LiberUsers.getUD().storeUser(u);
				return "Succesfully set " + args[0] + "'s rank to " + rank + "!";
			}
		}
		
		private String seen(User u, String[] args) {
			if (args == null)
				return null;
			
			if (args.length > 2) 
				return "Too many arguments.";

			User user = LiberUsers.getUD().getUserFromNickname(args[0]);
			if (user == u)
				return "Still looking for yourself?";
			
			if (user == null)
				return "Can't find that user.";

			long seen = 0;
			boolean specific = (args.length == 2);
			if (specific) {
				String platform = args[1];
				
				if (!platform.equalsIgnoreCase("irc")&&
						!platform.equalsIgnoreCase("ig")&&
						!platform.equalsIgnoreCase("ingame"))
					return "Unknown platform. Use: irc or ig (ingame)";
				
				boolean irc = platform.equalsIgnoreCase("irc");
				if (irc) 
					seen = user.seenirc;
				else 
					seen = user.seenig;
			} else 
				seen = (user.seenirc > user.seenig) ? user.seenirc : user.seenig;
			
			if (seen == -1)
				return "This user has no record.";
			
			LocalDate today = LocalDate.now();
			LocalDate seendate = Instant.ofEpochMilli(seen).atZone(ZoneId.systemDefault()).toLocalDate();
			int months = Period.between(today, seendate).getMonths();
			int days = Period.between(today, seendate).getDays();
			
			String time;
			if (months == 0 && days == 0) {
				long now = System.currentTimeMillis();
				long elapsed = now - seen;
				elapsed = elapsed / 1000 / 60;
				int min = (int) (elapsed % 60);
				int hours = (int) Math.floor(elapsed / 60);
				if (("" + min).length() == 1)
					time = hours + ":0" + min;
				else
					time = hours + ":" + min;
			} else {
				String month;
				String day;
				if (months == 1) 
					month = " one month";
				else 
					month = " " + months + " months";
				if (days == 1)
					day = " one day";
				else 
					day = " " + days + " days";
				if (months > 0 && days > 0)
					time = month + " and" + day;
				else if (months > 0)
					time = month;
				else
					time = day;
			}
			return user.nickname + " was last seen " + time + " ago";
		}
		
		private String setBirthday(User u, String[] args) {
			if (args == null || args.length != 3) 
				return "Incorrect arguments! Use !setbirthday <day> <month> <year>.";
			int day = Integer.parseInt(args[0]);
			int month = Integer.parseInt(args[1]);
			int year = Integer.parseInt(args[2]);
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				Date dt = sdf.parse(day + "-" + month + "-" + year);
				u.birthday = dt.getTime();
				LiberUsers.getUD().storeUser(u);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			String dayName = "";
			int mod100 = day % 100;
			int mod10 = day % 10;
			if (mod10 == 1 && mod100 != 11) 
				dayName = day + "st";
			else if (mod10 == 2 && mod100 != 12) 
				dayName = day + "nd";
			else if (mod10 == 3 && mod100 != 13) 
				dayName = day + "rd";
			else 
				dayName = day + "th";
			
			String monthName = Month.of(month).toString().substring(0, 1);
			monthName += Month.of(month).toString().toLowerCase().substring(1);
			return "Birthday set to: " + dayName + " of " + monthName + ", " + year;
		}
		
		private String setGreet(User u, String[] args) {
			String greet = "";
			
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					greet += args[i];
					if (i + 1 != args.length)
						greet += " ";
				}
			}

			u.greet = greet;
			LiberUsers.getUD().storeUser(u);
			return "Greet succesfully updated!";
		}
		
		private String setIRCName (User u, String[] args) {
			if (args == null || args.length < 1 || args.length > 1)
				return "Incorrect arguments";
			
			String newName = args[0];
			
			if (u.ircName == newName) 
				return "This is already your name on IRC";
			
			User name = LiberUsers.getUD().getUserFromIRCName(newName);
			if (name != null)
				return "This name is already in use.";
			
			String key = "";
			
			for (int i = 0; i < 4; i++)
				key += String.valueOf((int) Math.ceil(Math.random() * 9));
			
			u.ircKey = key;
			u.ircName = newName;
			LiberUsers.getUD().storeUser(u);
			return "IRC name changed! Use this key to confirm your name when you are on irc: " + key;
		}
		
		private String setNickname (User u, String[] args) {
			if (args == null || args.length < 1 || args.length > 1)
				return "Incorrect arguments";
			
			String newName = args[0];
			
			if (u.nickname == newName) 
				return "This is already your nickname";
			
			User name = LiberUsers.getUD().getUserFromIRCName(newName);
			if (name != null)
				return "This nickname is already in use.";
			
			u.nickname = newName;
			LiberUsers.getUD().storeUser(u);
			return "Your nickname has changed! You are now known as " + newName;
		}
		
		private String staff(User user) {
			UUID uuid = user.mcUuid;
			
			if (RankManager.staff.contains(uuid)) {
				RankManager.trustedMode(user);
				return "Staff mode disabled!";
			} else {
				RankManager.staffMode(user);
				return "Staff mode disabled!";
			}
		}
		
		private String voice(String[] args) {
			// TODO implement !voice with new behaviour
			/*if (args == null || args.length != 1) {
				FlowerBot.getMM().sendMessage(channel.getName(), "Usage: !voice <username>");
				return;
			}
			String username = args[0];
			if (!FlowerBot.getCNM().isOnline(username)) {
				FlowerBot.getMM().sendMessage(channel.getName(), "Can't find " + username);
				return;
			}
			
			if (FlowerBot.getUM().isIdentified(username)) {
				FlowerBot.getCNM().write("cs", "access " + channel.getName() + " add " + username + " 3");
			}
			FlowerBot.getCNM().write("mode", channel.getName() + " v+ " + username);
			FlowerBot.getMM().sendMessage(channel.getName(), "Voiced " + username);*/
			return "Not implemented";
		}

		@Override
		public String getTag() {
			switch (this) {
			case STAFF:
				return "";
			default:
			}
			return ChatColor.AQUA + "[UserManager]";
		}
	}
}
