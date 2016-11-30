package oasismc.oasisfreeze;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class OasisFreeze extends JavaPlugin implements Listener {

	private HashMap<UUID, UUID> frozenPlayers = new HashMap<UUID, UUID>();

	@Override
	public void onEnable() {
		getLogger().info("OasisFreeze is booting...");
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		getLogger().info("OasisFreeze is disabling...");
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("freeze")) {

			Player sender;

			if (commandSender instanceof Player) {
				sender = (Player) commandSender;
			} else {
				commandSender.sendMessage(ChatColor.DARK_RED + "Only players can use this command!");
				return true;
			}

			// Make sure that the player specified exactly one argument (the name of the player to freeze)

			if (args.length != 1) {
				// When onCommand() returns false, the help message associated with that command is displayed.
				return false;
			}

			Player target = sender.getServer().getPlayer(args[0]);
			// Make sure the player is online.
			if (target == null) {
				sender.sendMessage(ChatColor.DARK_RED + args[0] + " is not currently online so cannot be frozen!");
				return true;
			}

			UUID targetUUID = target.getUniqueId();

			if (frozenPlayers.containsKey(targetUUID)) {
				frozenPlayers.remove(targetUUID);
				target.sendMessage(ChatColor.GREEN + "You have been unfrozen!");
				sender.sendMessage(ChatColor.AQUA + "You have unfrozen " + target.getDisplayName());
			} else {
				frozenPlayers.put(targetUUID, sender.getUniqueId());
				target.sendMessage(ChatColor.AQUA + "You have been frozen by a staff member!");
				sender.sendMessage(ChatColor.GREEN + "You have frozen " + target.getDisplayName());
			}
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
		Player target = e.getPlayer();

		if (frozenPlayers.containsKey(target.getUniqueId())) {
			e.setCancelled(true);
			target.sendMessage(ChatColor.RED + "You cannot use commands while frozen!");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent e) {
		Player target = e.getPlayer();

		if (frozenPlayers.containsKey(target.getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent e) {
		Player target = e.getPlayer();

		if (frozenPlayers.containsKey(target.getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLogout(PlayerQuitEvent e) {
		if (frozenPlayers.containsValue(e.getPlayer().getUniqueId())) {
			for (UUID playerUUID : frozenPlayers.keySet()) {
				if (frozenPlayers.get(playerUUID).equals(e.getPlayer().getUniqueId())) {
					frozenPlayers.remove(playerUUID);
					if (getServer().getPlayer(playerUUID) != null) {
						getServer().getPlayer(playerUUID).sendMessage(ChatColor.GREEN + "The staff member who froze you has gone offline, so you have been unfrozen!");
					}
				}
			}
		}
	}
}
