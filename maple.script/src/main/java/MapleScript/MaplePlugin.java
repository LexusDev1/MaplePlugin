package MapleScript;

import org.bukkit.plugin.java.JavaPlugin;

import MapleScript.commands.Ping;
import MapleScript.listeners.LoginEventListener;

public final class MaplePlugin extends JavaPlugin {
    
    private Database.Database database;
        

    @Override
    public void onLoad() {
        getLogger().info("Loaded");
    }

    @Override
    public void onEnable() {
        getLogger().info("[Maple] MapleScript Enabled!");
        
        database = Database.Database.getInstance();
        

        // Register commands
        getCommand("ping").setExecutor(new Ping());

        getServer().getPluginManager().registerEvents(new LoginEventListener(database), this);
        
    }

    @Override
    public void onDisable() {
        getLogger().info("[Maple] MapleScript Disabled!");
    }

}
