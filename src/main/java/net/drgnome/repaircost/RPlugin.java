// Bukkit Plugin "RepairCost" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.repaircost;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;

public class RPlugin extends JavaPlugin implements Runnable, Listener
{
    public static final String _version = "#VERSION#";
    private static final String[] _commands = {"help", "reload", "update", "version", "set", "info"};
    private static final String[] _messages = {"You tool was blessed by the spirit of the eternal dragon.", "Balanced the force in your tool, the jedi masters have.",
    "The forge of the aether has granted a new strength to your tool.", "Your tool has been renewed through the power of the old religion.", "\"Jarvis, fix that for me.\""};
    private boolean _update = false;
    
    public void onEnable()
    {
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        if(Config.bool("check-update"))
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 72000L);
        }
    }
    
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);
    }
    
    public void reloadConfig()
    {
        super.reloadConfig();
        Config.reload(this);
        saveConfig();
        Shape.reload(this);
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        command(sender, args);
        return true;
    }
    
    public void command(CommandSender sender, String[] args)
    {
        if(args.length == 0)
        {
            args = new String[]{"info"};
        }
        int cmd = Util.switchString(args[0], _commands);
        if((cmd > 3) && !(sender instanceof Player))
        {
            Util.sendMessage(sender, "You can't execute that command from the console.", ChatColor.RED);
            return;
        }
        switch(cmd)
        {
            case 0:
                Util.sendMessage(sender, "---------- RepairCost Help ----------", ChatColor.AQUA);
                Util.sendMessage(sender, "/rcost help - This help", ChatColor.AQUA);
                Util.sendMessage(sender, "/rcost version - Show the current version", ChatColor.AQUA);
                Util.sendMessage(sender, "/rcost [info] - Show the repair cost level of your item", ChatColor.AQUA);
                if(sender.hasPermission("rcost.cheat"))
                {
                    Util.sendMessage(sender, "/rcost set [level] - Set the repair cost level", ChatColor.AQUA);
                }
                if(sender.hasPermission("rcost.admin"))
                {
                    Util.sendMessage(sender, "/rcost reload - Reload the plugin", ChatColor.AQUA);
                }
                if(sender.hasPermission("rcost.update"))
                {
                    Util.sendMessage(sender, "/rcost update - Check for updates", ChatColor.AQUA);
                }
                break;
            case 1:
                if(!sender.hasPermission("rcost.admin"))
                {
                    Util.sendMessage(sender, "You're not allowed to do that.", ChatColor.RED);
                    return;
                }
                reloadConfig();
                Util.sendMessage(sender, "Reloaded RepairCost.", ChatColor.GREEN);
                break;
            case 2:
                if(!sender.hasPermission("rcost.update"))
                {
                    Util.sendMessage(sender, "You're not allowed to do that.", ChatColor.RED);
                    return;
                }
                if(checkUpdate())
                {
                    Util.sendMessage(sender, "There is an update available for RepairCost.", ChatColor.YELLOW);
                }
                else
                {
                    Util.sendMessage(sender, "RepairCost is up to date.", ChatColor.GREEN);
                }
                break;
            case 3:
                Util.sendMessage(sender, "RepairCost version " + _version, ChatColor.GREEN);
                break;
            case 4:
                if(!sender.hasPermission("rcost.cheat"))
                {
                    Util.sendMessage(sender, "You're not allowed to do that.", ChatColor.RED);
                    return;
                }
                // No break here
            case 5:
                Player player = (Player)sender;
                ItemStack item = player.getItemInHand();
                if(item == null)
                {
                    Util.sendMessage(sender, "There's nothing in your hand.", ChatColor.RED);
                    return;
                }
                ItemMeta itemMeta = item.getItemMeta();
                if((itemMeta == null) || !(itemMeta instanceof Repairable))
                {
                    Util.sendMessage(sender, "This item isn't repairable.", ChatColor.RED);
                    return;
                }
                Repairable meta = (Repairable)itemMeta;
                if(cmd == 5)
                {
                    Util.sendMessage(sender, "Repair cost level: " + (meta.getRepairCost()) + ".", ChatColor.GREEN);
                    return;
                }
                try
                {
                    int level = (args.length > 1) ? Util.max(Integer.parseInt(args[1]), 0) : 0;
                    meta.setRepairCost(level);
                    item.setItemMeta(itemMeta);
                    player.setItemInHand(item);
                    Util.sendMessage(sender, "Repair cost level set to " + level + ".", ChatColor.GREEN);
                }
                catch(NumberFormatException e)
                {
                    Util.sendMessage(sender, "Buddy, a level has to be a NUMBER.", ChatColor.YELLOW);
                }
                break;
            default:
                Util.sendMessage(sender, "Unknown argument.", ChatColor.RED);
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleLogin(PlayerLoginEvent event)
    {
        if(_update && event.getPlayer().hasPermission("rcost.update"))
        {
            Util.sendMessage(event.getPlayer(), "There is an update available for RepairCost.", ChatColor.YELLOW);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleClickEvent(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if(!event.hasBlock() || (event.getAction() != Action.RIGHT_CLICK_BLOCK) || !player.hasPermission("rcost.use"))
        {
            return;
        }
        ItemStack item = player.getItemInHand();
        if(item == null)
        {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if((itemMeta == null) || !(itemMeta instanceof Repairable))
        {
            return;
        }
        Repairable meta = (Repairable)itemMeta;
        if(meta.getRepairCost() == 0)
        {
            return;
        }
        Block block = event.getClickedBlock();
        if(!Shape.apply(player.getWorld(), block.getX(), block.getY(), block.getZ()))
        {
            return;
        }
        meta.setRepairCost(0);
        item.setItemMeta(itemMeta);
        player.setItemInHand(item);
        Util.sendMessage(player, randomMessage(), ChatColor.GREEN);
        event.setCancelled(true);
    }
    
    private static String randomMessage()
    {
        return _messages[new Random().nextInt(_messages.length)];
    }
    
    public void run()
    {
        if(checkUpdate())
        {
            getServer().getScheduler().cancelTasks(this);
        }
    }
    
    public boolean checkUpdate()
    {
        _update = Util.hasUpdate("rcost", _version);
        return _update;
    }
}