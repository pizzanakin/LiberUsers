package net.libercraft.liberusers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import net.libercraft.liberusers.UserManager.User;

public class RankManager implements Listener {
	
	public static HashMap<UUID,PermissionAttachment> perms;
	public static List<UUID> staff;
	
	public RankManager() {
		perms = new HashMap<>();
		staff = new ArrayList<>();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		PermissionAttachment attachment = e.getPlayer().addAttachment(LiberUsers.get());
		perms.put(e.getPlayer().getUniqueId(), attachment);
		
		User u = LiberUsers.getUD().getUserFromUUID(e.getPlayer().getUniqueId());
		u.assignPermissions();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		// TODO also do this on close();
		PermissionAttachment attachment = perms.get(e.getPlayer().getUniqueId());
		e.getPlayer().removeAttachment(attachment);
		perms.remove(e.getPlayer().getUniqueId());
	}
	
	public static void staffMode(User u) {
		Rank rank = Rank.valueOf(u.rank);
		
		if (rank == Rank.OWNER) {
			LiberUsers.get().getServer().getPlayer(u.mcUuid).setOp(true);
		} else 
			for (String p:rank.getPermissions())
				perms.get(u.mcUuid).setPermission(p, true);

		if (!staff.contains(u.mcUuid))
			staff.add(u.mcUuid);
	}
	
	public static void trustedMode(User u) {
		Rank rank = Rank.valueOf(u.rank);

		if (rank == Rank.OWNER) {
			LiberUsers.get().getServer().getPlayer(u.mcUuid).setOp(false);
		} else 
			for (String p:rank.getPermissions())
				perms.get(u.mcUuid).unsetPermission(p);
		
		if (staff.contains(u.mcUuid))
			staff.remove(u.mcUuid);
	}
	
	public static enum Rank {
		DEFAULT(0),
		TRUSTED(1),
		MODERATOR(2),
		ADMIN(3),
		OWNER(4);
		
		private int i;
		
		private Rank(int i) {
			this.i = i;
		}
		
		public List<String> getPermissions() {
			List<String> permissions = new ArrayList<>();

			// TODO make sure stars work
			
			// Default permissions
			permissions.add("RightOn.spawn.tp");
			permissions.add("ZMessages.msg");
			permissions.add("ZMessages.reply");
			permissions.add("minecraft.command.kill");
			if (this == DEFAULT)
				return permissions;
			
			// Trusted permissions
			permissions.add("use.lava");
			permissions.add("use.flint");
			permissions.add("use.tnt");
			if (this == TRUSTED)
				return permissions;
			
			// Moderator permissions
			permissions.add("OpenInv.crossworld");
			permissions.add("OpenInv.editender");
			permissions.add("OpenInv.editinv");
			permissions.add("OpenInv.openender");
			permissions.add("OpenInv.openenderall");
			permissions.add("OpenInv.openinv");
			permissions.add("OpenInv.openself");
			permissions.add("coreprotect.help");
			permissions.add("coreprotect.inspect");
			permissions.add("coreprotect.lookup");
			permissions.add("coreprotect.restore");
			permissions.add("coreprotect.rollback");
			permissions.add("minecraft.command.ban");
			permissions.add("minecraft.command.ban-ip");
			permissions.add("minecraft.command.banlist");
			permissions.add("minecraft.command.kick");
			permissions.add("minecraft.command.pardon");
			permissions.add("minecraft.command.pardon-ip");
			permissions.add("minecraft.command.whitelist");
			permissions.add("vanish.standard");
			permissions.add("worldguard.region.info.*");
			if (this == MODERATOR)
				return permissions;

			// Admin permissions
			permissions.add("OpenInv.anychest");
			permissions.add("OpenInv.search");
			permissions.add("OpenInv.searchenchant");
			permissions.add("OpenInv.silent");
			permissions.add("ZMessages.socialspy");
			permissions.add("bukkit.command.plugins");
			permissions.add("bukkit.command.version");
			permissions.add("minecraft.command.toggledownfall");
			permissions.add("minecraft.command.weather");
			permissions.add("worldedit.selection.*");
			permissions.add("worldedit.wand");
			permissions.add("worldedit.region.addmember.*");
			permissions.add("worldedit.region.addowner.*");
			permissions.add("worldedit.region.define.*");
			permissions.add("worldedit.region.list.*");
			permissions.add("worldedit.region.redefine.*");
			permissions.add("worldedit.region.removemember.*");
			permissions.add("worldedit.region.removeowner.*");
			permissions.add("worldedit.region.select.*");
			permissions.add("worldedit.region.wand.*");
			return permissions;
		}
		
		public static Rank valueOf(int i) {
			for (Rank r:Rank.values())
				if (r.i == i)
					return r;
			return null;
		}
	}
}
