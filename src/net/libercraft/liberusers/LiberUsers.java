package net.libercraft.liberusers;

import org.bukkit.ChatColor;

import net.libercraft.libercore.interfaces.Module;

public class LiberUsers extends Module {
	private static LiberUsers instance;
	
	private UserDatabase ud;
	private UserManager um;
	private RankManager rm;

	@Override
	public ChatColor colour() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onActivate() {
		instance = this;
		
		ud = new UserDatabase();
		um = new UserManager();
		rm = new RankManager();
		
		getServer().getPluginManager().registerEvents(um, this);
		getServer().getPluginManager().registerEvents(rm, this);
		
		getCommand("staff").setExecutor(rm);
		getCommand("adminchat").setExecutor(new AdminChat());
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
		
	}
	
	public static LiberUsers get() {
		return instance;
	}
	
	public static UserDatabase getUD() {
		return instance.ud;
	}
	
	public static UserManager getUM() {
		return instance.um;
	}
	
	public static RankManager getRM() {
		return instance.rm;
	}

}
