package net.libercraft.liberusers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class AdminChat implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String message = "";
		for (String s:args)
			message += s;
		
		for (Player p:LiberUsers.get().getServer().getOnlinePlayers())
			if (LiberUsers.getUD().getUserFromUUID(p.getUniqueId()).rank >= 2)
				p.sendMessage(ChatColor.GOLD + "[AriChat] " + ChatColor.AQUA + sender.getName() + ": " + message);
		
		return true;
	}
}
