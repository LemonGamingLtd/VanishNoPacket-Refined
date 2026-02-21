package org.kitteh.vanish.compat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.vanish.VanishPerms;
import org.kitteh.vanish.VanishPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class VanishCommandBukkit implements CommandExecutor, TabCompleter {

    private final VanishPlugin plugin;
    private final String reloadMessage = ChatColor.DARK_AQUA + "[Vanish] Users reloaded and some settings refreshed";

    public VanishCommandBukkit(@NonNull VanishPlugin plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        PluginCommand vanishCmd = plugin.getCommand("vanish");
        if (vanishCmd != null) {
            vanishCmd.setExecutor(this);
            vanishCmd.setTabCompleter(this);
        }

        PluginCommand softvanishCmd = plugin.getCommand("softvanish");
        if (softvanishCmd != null) {
            softvanishCmd.setExecutor(this);
            softvanishCmd.setTabCompleter(this);
        }

        PluginCommand adminvanishCmd = plugin.getCommand("adminvanish");
        if (adminvanishCmd != null) {
            adminvanishCmd.setExecutor(this);
            adminvanishCmd.setTabCompleter(this);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        String commandName = command.getName().toLowerCase();

        if (commandName.equals("softvanish")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                return true;
            }
            return handleSoftVanish(player);
        }

        if (commandName.equals("adminvanish")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                return true;
            }
            return handleAdminVanish(player);
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                return true;
            }
            if (!VanishPerms.canVanish(player)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to vanish");
                return true;
            }
            plugin.getManager().toggleVanish(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                if (!(sender instanceof Player) || VanishPerms.canReload(sender)) {
                    plugin.reload();
                    sender.sendMessage(reloadMessage);
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload");
                }
            }
            case "list" -> {
                if (!(sender instanceof Player) || VanishPerms.canList(sender)) {
                    StringBuilder list = new StringBuilder();
                    for (String player : plugin.getManager().getVanishedPlayers()) {
                        if (!list.isEmpty()) {
                            list.append(ChatColor.DARK_AQUA).append(", ");
                        }
                        list.append(ChatColor.AQUA).append(player);
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "Vanished: " + list);
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to list vanished players");
                }
            }
            case "on" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                if (!VanishPerms.canVanish(player)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to vanish");
                    return true;
                }
                if (plugin.getManager().isVanished(player)) {
                    sender.sendMessage(ChatColor.RED + "You are already invisible");
                    return true;
                }
                plugin.getManager().toggleVanish(player);
            }
            case "off" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                if (!VanishPerms.canVanish(player)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to vanish");
                    return true;
                }
                if (!plugin.getManager().isVanished(player)) {
                    sender.sendMessage(ChatColor.RED + "You are already visible");
                    return true;
                }
                plugin.getManager().toggleVanish(player);
            }
            case "fakequit" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                if (!VanishPerms.canFakeAnnounce(player)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission");
                    return true;
                }
                boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
                if (!plugin.getManager().isVanished(player)) {
                    plugin.getManager().toggleVanish(player);
                }
                plugin.getManager().getAnnounceManipulator().fakeQuit(player, force);
            }
            case "fakejoin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                if (!VanishPerms.canFakeAnnounce(player)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission");
                    return true;
                }
                boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");
                if (plugin.getManager().isVanished(player)) {
                    plugin.getManager().toggleVanish(player);
                }
                plugin.getManager().getAnnounceManipulator().fakeJoin(player, force);
            }
            case "soft" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                return handleSoftVanish(player);
            }
            case "admin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                return handleAdminVanish(player);
            }
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /vanish toggle <option>");
                    return true;
                }
                handleToggle(player, args[1]);
            }
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    private void handleToggle(Player player, String toggleName) {
        try {
            VanishPerms.TogglePermissions toggle = VanishPerms.TogglePermissions.valueOf(toggleName.toUpperCase());
            if (!toggle.getCanToggle().apply(player)) {
                player.sendMessage(ChatColor.DARK_RED + "You can't toggle that!");
                return;
            }
            Boolean status = toggle.getToggle().apply(player);
            player.sendMessage((status ? ChatColor.GREEN : ChatColor.DARK_RED) + "Successfully toggled: " + toggle.name() + " -> " + (status ? "ON" : "OFF"));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown toggle option: " + toggleName);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean handleSoftVanish(Player player) {
        if (!VanishPerms.canSoftVanish(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission");
            return true;
        }
        if (plugin.getManager().isVanished(player)) {
            boolean softModeEnabled = VanishPerms.toggleSoftMode(player);
            if (softModeEnabled) {
                player.sendMessage(ChatColor.DARK_AQUA + "Soft mode enabled");
            } else {
                player.sendMessage(ChatColor.DARK_AQUA + "Soft mode disabled");
            }
        } else {
            plugin.getManager().toggleVanish(player);
            VanishPerms.enableSoftMode(player);
            player.sendMessage(ChatColor.DARK_AQUA + "Soft mode enabled");
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleAdminVanish(Player player) {
        if (!VanishPerms.canAdminVanish(player)) {
            player.sendMessage(ChatColor.RED + "You don't have permission");
            return true;
        }
        boolean adminVanished = plugin.getManager().toggleAdminVanish(player);
        if (adminVanished) {
            player.sendMessage(ChatColor.DARK_RED + "Admin vanish enabled");
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "Admin vanish disabled");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("list");
            completions.add("on");
            completions.add("off");
            completions.add("fakequit");
            completions.add("fakejoin");
            completions.add("soft");
            completions.add("admin");
            completions.add("toggle");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("toggle")) {
                return Arrays.stream(VanishPerms.TogglePermissions.values())
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("fakequit") || args[0].equalsIgnoreCase("fakejoin")) {
                if ("force".startsWith(args[1].toLowerCase())) {
                    return List.of("force");
                }
            }
        }
        return List.of();
    }
}