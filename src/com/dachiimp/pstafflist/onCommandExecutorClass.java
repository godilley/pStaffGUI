package com.dachiimp.pstafflist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by DaChiimp on 5/16/2016.
 */
public class onCommandExecutorClass implements CommandExecutor {

    static ArrayList<String> blockedNames = new ArrayList<>();

    public static void setupBlockedNames() {
        if (blockedNames.size() > 0) {
            blockedNames.clear();
        }
        blockedNames.add("Spacers");
        blockedNames.add("SpacerPositions");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("staff")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    Methods.gui(player, false);
                } else if (args.length >= 3 && (args[0].equalsIgnoreCase("changerank") || args[0].equalsIgnoreCase("rank"))) {
                    if (player.hasPermission("staff.modify")) {
                        if (Methods.checkStaff(args[1])) {
                            StringBuilder st = new StringBuilder();
                            for (int m = 2; m < args.length; m++) {
                                st.append(args[m] + " ");
                            }

                            String newrank = st.toString();
                            Methods.staffRank.put(args[1], newrank);
                            player.sendMessage(ChatColor.GRAY + "Changed " + ChatColor.RED + args[1] + ChatColor.GRAY + "'s rank to " + ChatColor.RED + newrank);
                        } else {
                            player.sendMessage(ChatColor.RED + "There is no such staff member added. Please note it is case sensitive.");
                        }

                    }
                } else if (args.length >= 4) {
                    // staff add PLAYER 0 {RANK}
                    if (player.hasPermission("staff.modify")) {
                        if (args[0].equalsIgnoreCase("add")) {
                            if (args[1].length() >= 3) {
                                StringBuilder st = new StringBuilder();
                                for (int m = 3; m < args.length; m++) {
                                    st.append(args[m] + " ");
                                }

                                String rank = st.toString();
                                if (!Methods.checkStaff(args[1])) {
                                    try {
                                        int position = Integer.parseInt(args[2]);
                                        position = position - 1;
                                        if (Methods.getPositionList().contains(position + "")) {
                                            player.sendMessage(ChatColor.RED + "Someone already has position " + position + ".");
                                        } else {
                                            if (position <= 53 && position >= 0) {
                                                Methods.addStaffMember(args[1], position, rank);
                                                player.sendMessage(ChatColor.GRAY + "Added " + ChatColor.RED + args[1] + ChatColor.GRAY + " to staff with the rank of " + ChatColor.RED + rank + ChatColor.GRAY + "with the position of " + ChatColor.RED + args[2] + ChatColor.GRAY + ".");
                                            } else {
                                                player.sendMessage(ChatColor.RED + "Please enter a slot number between 1-54");
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(ChatColor.RED + args[2] + " is not a number.");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "That staff member already exists.");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Please enter a valid Minecraft username");
                            }
                        } else {
                            Methods.unknownCommand(player);
                        }
                    } else {
                        Methods.noCommandPerm(player);
                    }
                } else if (args.length == 2) {
                    if (player.hasPermission("staff.modify")) {
                        if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                            if (Methods.checkStaff(args[1])) {
                                Methods.removeStaffMember(args[1]);
                                player.sendMessage(ChatColor.GRAY + "Removed staff member " + ChatColor.RED + args[1] + ChatColor.GRAY + " from the staff list.");
                            } else {
                                player.sendMessage(ChatColor.RED + "There is no such staff member added. Please note it is case sensitive.");
                            }

                        } else if (args[0].equalsIgnoreCase("addSpacer")) {
                            if (args[1].length() < 3) {
                                player.sendMessage(ChatColor.RED + "Please enter a valid spacer name that is longer than 3 characters");
                            } else {
                                String spacerName = args[1];
                                if (blockedNames.contains(spacerName)) {
                                    player.sendMessage(ChatColor.RED + "You cannot create a spacer with the name " + spacerName + " -_-");
                                } else {
                                    if (SpacerMethods.getSpacerNames().contains(spacerName)) {
                                        player.sendMessage(ChatColor.RED + "There's already a spacer called " + ChatColor.RESET + spacerName);
                                    } else {
                                        if (player.getItemInHand() != null) {
                                            if (!player.getItemInHand().getType().equals(Material.AIR)) {
                                                if (SpacerMethods.getSpacerNames().size() < 36) {
                                                    if (SpacerMethods.getSpacers().containsValue(player.getItemInHand())) {
                                                        player.sendMessage(ChatColor.RED + "There's already a spacer for that item!");
                                                    } else {
                                                        ItemStack spacer = player.getItemInHand();
                                                        SpacerMethods.addSpacer(spacerName, spacer);
                                                        player.sendMessage(ChatColor.GREEN + "Added a spacer called " + spacerName + " as the item in your hand");
                                                    }
                                                } else {
                                                    player.sendMessage(ChatColor.RED + "You can only have 36 spacers!");
                                                }
                                            } else {
                                                player.sendMessage(ChatColor.RED + "Please put an item in your hand!");
                                            }
                                        } else {
                                            player.sendMessage(ChatColor.RED + "Please put an item in your hand!");
                                        }
                                    }
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("removeSpacer")) {
                            if (args[1].length() < 3) {
                                player.sendMessage(ChatColor.RED + "Please enter a valid spacer name");
                            } else {
                                String spacerName = args[1];
                                if (SpacerMethods.getSpacerNames().contains(spacerName)) {
                                    SpacerMethods.removeSpacer(spacerName);
                                    player.sendMessage(ChatColor.GREEN + "Removed the spacer called " + spacerName);
                                } else {
                                    player.sendMessage(ChatColor.RED + "There is not a spacer called " + spacerName);
                                }
                            }
                        } else {
                            Methods.unknownCommand(player);
                        }
                    } else {
                        Methods.noCommandPerm(player);
                    }
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("modify")) {
                        if (player.hasPermission("staff.modify")) {
                            Methods.gui(player, true);
                        } else {
                            Methods.noCommandPerm(player);
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (player.hasPermission("staff.modify")) {
                            StaffList._plugin.reloadConfig();
                            Methods.saveAllFiles();
                            player.sendMessage("Saved Files...");
                            final Player p = player;
                            Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
                                @Override
                                public void run() {
                                    Methods.loadAllFiles();
                                    p.sendMessage("Loaded" +
                                            " Files...");
                                }
                            }, 10L);
                            if (Methods.getCfgValue("InventoryTitle").length() > 32) {
                                player.sendMessage(ChatColor.DARK_RED + "InventoryTitle is longer than 32 chars. Reverting to default.");
                            }
                        } else {
                            Methods.noCommandPerm(player);
                        }
                    } else if (args[0].equalsIgnoreCase("debug")) {
                        if (player.hasPermission("staff.debug")) {
                            if (!StaffList.debug) {
                                StaffList.debug = true;
                                player.sendMessage("Set debug mode to " + ChatColor.RED + StaffList.debug);
                            } else {
                                StaffList.debug = false;
                                player.sendMessage("Set debug mode to " + ChatColor.RED + StaffList.debug);
                            }
                        } else {
                            Methods.noCommandPerm(player);
                        }
                    } else if (args[0].equalsIgnoreCase("help")) {
                        Methods.displayHelp(player);
                    } else if (args[0].equalsIgnoreCase("info")) {
                        Methods.displayInfo(player);
                    } else if (args[0].equalsIgnoreCase("fake")) {
                        if (player.hasPermission("staff.fake")) {
                            if (Methods.ignored.contains(player.getName())) {
                                Methods.ignored.remove(player.getName());
                                player.sendMessage(ChatColor.RED + "No longer faking as offline");
                            } else {
                                Methods.ignored.add(player.getName());
                                player.sendMessage(ChatColor.GREEN + "Now faking as offline");
                            }
                        } else {
                            Methods.noCommandPerm(player);
                        }
                    } else {
                        Methods.unknownCommand(player);
                    }
                } else {
                    Methods.unknownCommand(player);
                }
            } else {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("debug")) {
                        if (sender instanceof ConsoleCommandSender) {
                            if (!StaffList.debug) {
                                StaffList.debug = true;
                                Debug.p("Set debug mode to " + ChatColor.RED + StaffList.debug);
                            } else {
                                StaffList.debug = false;
                                Debug.p("Set debug mode to " + ChatColor.RED + StaffList.debug);
                            }
                        } else {
                            sender.sendMessage("You must be a player to run this command!");
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (sender instanceof ConsoleCommandSender) {
                            StaffList._plugin.reloadConfig();
                            Methods.saveAllFiles();
                            Debug.p("Saved Files...");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
                                @Override
                                public void run() {
                                    Methods.loadAllFiles();
                                    Debug.p("Loaded" +
                                            " Files...");
                                }
                            }, 10L);
                            if (Methods.getCfgValue("InventoryTitle").length() > 32) {
                                sender.sendMessage(ChatColor.DARK_RED + "InventoryTitle is longer than 32 chars. Reverting to default.");
                            }
                        } else {
                            sender.sendMessage("You must be a player to run this command!");
                        }
                    } else {
                        Methods.unknownCommand(sender);
                    }
                } else {
                    Methods.unknownCommand(sender);
                }
            }
        }
        return false;
    }

}
