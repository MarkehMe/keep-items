package net.robinjam.bukkit.keepitems;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class.
 * 
 * @author robinjam
 */
public class KeepItems extends JavaPlugin implements Listener {
    
    private Map<Player, Death> deaths = new HashMap<Player, Death>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        // When the plugin is disabled, drop all managed items and clear the list
        for (Death death : deaths.values()) {
            death.drop();
        }
        
        deaths.clear();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(final EntityDeathEvent event) {
        // Skip if the entity that died is not a player
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // If the player already has a death on record, drop the items
        Death death = deaths.get(player);
        if (death != null)
            death.drop();
        
        // Register the death event
        deaths.put(player, new Death(player.getLocation(), event.getDrops(), calcExperience(player.getLevel())));
        
        // Don't drop any items or experience
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // If the player has a death on record, drop the items and experience at their respawn location
        Death death = deaths.remove(player);
        if (death != null)
            death.drop(event.getRespawnLocation());
    }
    
    /**
     * Calculates the total amount of experience required to reach the given level from level 0.
     * 
     * @param level The level for which to calculate the required experience
     * @return The amount of experience required
     */
    private int calcExperience(int level) {
        // Calculate the amount of experience required to reach this level from the previous one
        int xp = 7 + (int) Math.floor((level - 1) * 3.5);
        
        // Recursively repeat until we reach level 1
        return level > 1 ? xp + calcExperience(level - 1) : xp;
    }
    
}
