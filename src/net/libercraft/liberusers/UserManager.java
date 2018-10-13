package net.libercraft.liberusers;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.lpizzanakinl.pizzairc.FlowerBot;

import net.libercraft.libercore.LiberCore;
import net.libercraft.libercore.managers.MessageManager;
import net.libercraft.liberusers.RankManager.Rank;
import net.md_5.bungee.api.ChatColor;

public class UserManager implements Listener {
	
	@EventHandler
	public void onPlayerDamageEvent(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player))
			return;
		
		Player damager = (Player) e.getDamager();
		Player target = (Player) e.getEntity();
		
		User uD = LiberUsers.getUD().getUserFromUUID(damager.getUniqueId());
		User tD = LiberUsers.getUD().getUserFromUUID(target.getUniqueId());
		
		if (!uD.pvp) {
			e.setCancelled(true);
			damager.sendMessage(ChatColor.RED + "You have PVP disabled!");
			return;
		}
		
		if (!tD.pvp) {
			e.setCancelled(true);
			damager.sendMessage(ChatColor.RED + target.getName() + " has PVP disabled!");
			return;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		UUID uuid = e.getPlayer().getUniqueId();
		String mcName = e.getPlayer().getName();
		User u = LiberUsers.getUD().getUserFromUUID(uuid);
		
		// Create new user if user is new
		if (u == null) {
			u = LiberUsers.getUD().newUser(mcName);
			u.mcUuid = uuid;
			u.greet = "Hello "+u.nickname+"!";
			LiberUsers.getUD().storeUser(u);
		}
		
		// Update mcname in case user has changed names
		u.mcName = mcName;
		
		// Remove warning points if a month has passed
		long today = System.currentTimeMillis();
		long thiday = 86400 * 1000 * 30;
		if ((u.warningdate + thiday) < today) {
			if (u.warningpoints > 0)
				u.warningpoints -= 2;
			
			if (u.warningpoints <= 0) {
				MessageManager.sendMessage(LiberUsers.get(), e.getPlayer(), "Your warning points have reduced to 0!");
				u.warningpoints = 0;
				u.warningdate = -1;
			} else 
				u.warningdate = today;
		}
		
		// Update user changes in database
		LiberUsers.getUD().storeUser(u);
		
		final User user = u;
		new BukkitRunnable() {
			@Override
			public void run() {
				// Send greet
				user.sendGreet();
				
				// Send additional registration info messages to user
				if (user.regdone() || user.regdone)
					return;
				FlowerBot.getMM().sendPrivateMessage(mcName, "Your user profile still requires some information to be complete:", false);
				if (user.birthday == -1) 
					FlowerBot.getMM().sendPrivateMessage(mcName, " - Set your birthday using '/setbirthday <day> <month> <year>' in numbers", false);
				if (user.ircName == null) 
					FlowerBot.getMM().sendPrivateMessage(mcName, " - Set your IRC name using '/setircname name'", false);
				FlowerBot.getMM().sendPrivateMessage(mcName, "If you don't want to be notified of this anymore, use '/done'", false);
			}
		}.runTaskLater(LiberUsers.get(), 1);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		User u = LiberUsers.getUD().getUserFromUUID(e.getPlayer().getUniqueId());
		
		if (u != null) {
			u.seenig = System.currentTimeMillis();
			LiberUsers.getUD().storeUser(u);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		User u = LiberUsers.getUD().getUserFromUUID(e.getEntity().getUniqueId());
		if (u == null)
			return;
		
		u.deaths = u.deaths + 1;
		LiberUsers.getUD().storeUser(u);
	}
	
	public static class User {
		public int uid;
		public String nickname;
		public UUID mcUuid;
		public String mcName;
		public String ircName;
		public String ircKey;
		public int rank;
		public long birthday;
		public int deaths;
		public String greet;
		public boolean regdone;
		public long seenig;
		public long seenirc;
		public boolean pvp;
		public int warningpoints;
		public long warningdate;
		public String warninglog;
		
		public User(int uid, String nickname) {
			this.uid = uid;
			this.nickname = nickname;
			rank = 0;
			deaths = 0;
		}
		
		public void assignPermissions() {
			// Remove all current permissions
			for (String p:Rank.ADMIN.getPermissions())
				LiberUsers.getRM().perms.get(mcUuid).unsetPermission(p);
			
			// Assign staff permissions if user is in staff mode
			if (LiberUsers.getRM().staff.contains(this.mcUuid)) {
				LiberUsers.getRM().staffMode(this);
				return;
			}
			
			// Assign normal or trusted permissions
			if (rank == 0)
				for (String p:Rank.DEFAULT.getPermissions()) 
					LiberUsers.getRM().perms.get(this.mcUuid).setPermission(p, true);
			else
				for (String p:Rank.TRUSTED.getPermissions()) 
					LiberUsers.getRM().perms.get(this.mcUuid).setPermission(p, true);
		}
		
		public Player getPlayer() {
			return LiberUsers.get().getServer().getPlayer(mcUuid);
		}
		
		public void sendGreet() {
			if (LiberCore.isActive("PizzaIRC")) 
				FlowerBot.getMM().sendFlowerBotMessage(greet);
		}
		
		public boolean promote() {
			if (rank <= 2) {
				rank++;
				if (getPlayer() != null && getPlayer().isOnline())
					assignPermissions();
				return true;
			}
			return false;
		}
		
		public boolean demote() {
			if (rank > 0) {
				rank--;
				if (getPlayer() != null && getPlayer().isOnline())
					assignPermissions();
				return true;
			}
			return false;
		}
		
		public boolean regdone() {
			if (birthday == -1)
				return false;
			if (ircName == null)
				return false;
			return true;
		}
	}
}
