package com.dachiimp.pstafflist;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.shortninja.staffplus.StaffPlus;
import net.shortninja.staffplus.player.attribute.mode.handler.VanishHandler;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.staticaccess.VanishNoPacket;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by DaChiimp on 15/06/2016.
 */
public class Methods implements Listener {

    static ArrayList<String> StaffStringList = new ArrayList<>();
    static ArrayList<String> PositionStringList = new ArrayList<>();
    static Integer invSize = 666;
    static HashMap<String, Integer> playerPositions = new HashMap<>();
    static HashMap<String, ArrayList<String>> bungeeServers = new HashMap<>();
    static HashMap<String, String> lastLogin = new HashMap<>();
    static HashMap<String, String> staffRank = new HashMap<>();
    static HashMap<String, String> guiTemplate = new HashMap<>();
    static ArrayList<String> ignored = new ArrayList<>();

    static String dateFormat = null;

    public static void saveAllFiles() {

        saveLastLogins();

        SpacerMethods.saveSpacers();
        SpacerMethods.saveSpacerPositions();
        UUIDSupport.saveUUIDs();

        saveRanks();
    }

    public static void reloadAllFiles() {

        saveLastLogins();
        SpacerMethods.saveSpacers();
        UUIDSupport.saveUUIDs();
        SpacerMethods.saveSpacerPositions();
        saveRanks();
        loadStaffList();
        loadPositionList();
        loadPlayerPositions();
        loadLastLogins();
        loadRanks();
        StaffList.setupBooleans();
        updateGuiTemplates();
        setupBungeeServers();
        loadInvSize();

    }

    public static void loadAllFiles() {
        loadStaffList();
        loadPositionList();
        loadPlayerPositions();
        loadLastLogins();
        loadRanks();
        StaffList.setupBooleans();
        loadInvSize();
        updateGuiTemplates();
        setupBungeeServers();
        SpacerMethods.loadSpacers();
        SpacerMethods.loadSpacerPositions();
    }

    public static void checkPStaffList() {
        if (Bukkit.getServer().getPluginManager().getPlugin("StaffList") == null) {
            /*Debug.p("NOTE: There is a new version for this plugin - A premium version with more features");
            Debug.p("To get this plugin visit bit.ly/pStaffList (Case sensitive)");*/
        } else {
            Debug.p("Found StaffList - disabling this plugin. Please remove StaffList to allow pStaffList to work.");
            Bukkit.getServer().getPluginManager().disablePlugin(StaffList._plugin);
        }
    }

    public static void playerListScheduler() {
        if (StaffList.bungee) {

            if (StaffList.debug) {
                Debug.p("Retrieving players online");
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
                @Override
                public void run() {
                    //code

                        bungeeServersUpdatePlayers();

                        playerListScheduler();

                }
            }, StaffList.updateInterval * 20);
        }
    }

    public static void bungeeServersUpdatePlayers() {

        if (!StaffList.bungee) return;

        if (StaffList.debug) {
            Debug.p("bungeeServersUpdatePlayers");
        }

        List<String> servers = new ArrayList<>();

        for (String s : bungeeServers.keySet()) {
            servers.add(s);
        }

        bungeeServers.clear();

        for (String s : servers) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerList");
            out.writeUTF(s);
            if (StaffList.debug) {
                Debug.p("sentPluginMessage; for " + s + " from bungeeServerList: " + bungeeServers.keySet());
            }

            if (Bukkit.getServer().getOnlinePlayers().size() > 0) {

                Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

                player.sendPluginMessage(StaffList._plugin, "BungeeCord", out.toByteArray());
            }
        }
    }

    static boolean arrayContainsIgnoreCase(List<String> array, String compare) {
        for (String s : array) {
            if (s.equalsIgnoreCase(compare)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void gui(final Player player, boolean modify) {
        List<String> list = getStaffList();

        int size = getInvSize();

        if (size == 666) {
            player.sendMessage(ChatColor.RED + "There are no staff setup.");
        } else {


            String invTitle = getInvTitle();

            if (modify) {
                invTitle = ChatColor.DARK_RED + "Editing Staff";
                size = 54;
                SpacerMethods.setupInvWithSpacers(player);
                player.sendMessage(ChatColor.GREEN + "Adding spacers to inventory");
            }


            final Inventory sg = Bukkit.createInventory(null, size, invTitle);


            final Inventory staffgui = SpacerMethods.addSpacersToGUIInv(sg);

            boolean error = false;
            List<String> errors = new ArrayList<>();

            if (list.size() > 0) {
                for (final String p : list) {

                    final List<String> online = new ArrayList<>();

                    final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                    final SkullMeta sm = (SkullMeta) head.getItemMeta();
                    final List<String> lores = new ArrayList<>();
                    sm.setOwner(p);


                    String cfg_dname = getCfgValue("StaffHeadTitle");
                    cfg_dname = replaceAll(cfg_dname, p);


                    String line1 = getGUITemplate("line1");
                    line1 = replaceAll(line1, p);
                    String line2 = getGUITemplate("line2");
                    line2 = replaceAll(line2, p);
                    String line3 = getGUITemplate("line3");
                    line3 = replaceAll(line3, p);
                    String line4 = getGUITemplate("line4");
                    line4 = replaceAll(line4, p);
                    String line5 = getGUITemplate("line5");
                    line5 = replaceAll(line5, p);
                    String lastline_ONLINE = getGUITemplate("lastline_ONLINE");
                    lastline_ONLINE = replaceAll(lastline_ONLINE, p);
                    String lastline_OFFLINE = getGUITemplate("lastline_OFFLINE");
                    lastline_OFFLINE = replaceAll(lastline_OFFLINE, p);


                    // change to per line checks and line5
                    sm.setDisplayName(cfg_dname);
                    if (StaffList.s1) {
                        lores.add(line1);
                    }
                    if (StaffList.s2) {
                        lores.add(line2);
                    }
                    if (StaffList.s3) {
                        lores.add(line3);
                    }
                    if (StaffList.s4) {
                        lores.add(line4);
                    }
                    if (StaffList.s5) {
                        lores.add(line5);
                    }

                    if (StaffList.sio) {
                        if (Bukkit.getServer().getPlayer(p) != null && !StaffList.bungee) {
                            if (isIgnored(p)) {
                                lores.add(lastline_OFFLINE);
                            } else if (isEssentialsVanished(player)) {
                                lores.add(lastline_OFFLINE);
                            } else {
                                if (isNoPacketVanished(p)) {
                                    if (player.hasPermission("vanish.see") || player.hasPermission("vanish.standard") || player.hasPermission("vanish.*") || player.isOp()) {
                                        lores.add(lastline_ONLINE);
                                        online.add(p);
                                    } else {
                                        lores.add(lastline_OFFLINE);
                                    }
                                } else {
                                    lores.add(lastline_ONLINE);
                                    online.add(p);
                                }
                            }
                        } else {

                            if (StaffList.bungee) {
                                String bungeeOnline = getGUITemplate("lastline_ONLINE_BUNGEE");
                                boolean loreAdded = false;

                                for (String listserver : bungeeServers.keySet()) {
                                    ArrayList<String> players = bungeeServers.get(listserver);
                                    if (StaffList.debug) {
                                        Debug.p("listserver = " + listserver);
                                        Debug.p("players ArrayList = " + players);
                                        Debug.p("Player = " + p);
                                    }
                                    if (players != null && !loreAdded) {
                                        if (arrayContainsIgnoreCase(players, p)/*players.contains(p) fuck you capitals*/) {
                                            if (isIgnored(p)) {
                                                lores.add(lastline_OFFLINE);
                                                loreAdded = true;
                                            } else {
                                                bungeeOnline = bungeeOnline.replaceAll("%server%", listserver);
                                                bungeeOnline = replaceAll(bungeeOnline, p);
                                                lores.add(bungeeOnline);
                                                online.add(p);
                                                loreAdded = true;
                                            }
                                        } else {
                                        }
                                    }

                                }
                                if (!loreAdded) {
                                    lores.add(lastline_OFFLINE);
                                }

                            } else {
                                lores.add(lastline_OFFLINE);
                            }
                        }
                    }
                    if (StaffList.showOnlyIfOnline) {

                        if (online.contains(sm.getOwner())) {
                            sm.setLore(lores);
                            head.setItemMeta(sm);
                            Integer pos = getPosition(sm.getOwner());
                            if (pos == null || pos == 666) {
                                error = true;
                                errors.add(".onlyIfOnline pos 666/null for " + sm.getOwner());
                            } else {
                                if (StaffList.addHeadsInNextFreeSlot) {
                                    staffgui.addItem(head);
                                } else {
                                    staffgui.setItem(pos, head);
                                }
                                if (StaffList.debug) {
                                    Debug.p("Set slot " + getPosition(sm.getOwner()) + " to " + sm.getOwner());
                                }
                            }
                        }
                    } else {

                        sm.setLore(lores);
                        head.setItemMeta(sm);
                        Integer pos = getPosition(sm.getOwner());
                        if (pos == null || pos == 666) {
                            error = true;
                            errors.add(".onlyIfOnline[else] pos 666/null for " + sm.getOwner());
                        } else {
                            staffgui.setItem(pos, head);
                            if (StaffList.debug) {
                                Debug.p("Set slot " + getPosition(sm.getOwner()) + " to " + sm.getOwner());
                            }
                        }

                    }


                }

                if(error) {
                    player.sendMessage(ChatColor.RED + "Error loading staff, please retry or contact an administrator. Errors have been printed to console");
                    System.out.println("[pStaffList] Errors Loading Staff: " + StringUtils.join(", ",errors));
                    //return; may fix?
                }

                if (modify) {
                    player.sendMessage(ChatColor.RED + "You are modifying staff");
                }


                // LE Open Inv
                player.openInventory(staffgui);


            } else {
                player.sendMessage(ChatColor.RED + "There are no staff setup.");
            }
        }

    }

    public static String getInvTitle() {
        String name = getCfgValue("InventoryTitle");
        if (name.length() > 32) {
            Debug.p("Inventory Title is longer than 32 Chars Reverting To Default");
            name = ChatColor.DARK_RED + "Staff Members";
        }
        name = name.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
        return name;
    }

    public static String getOnlineTime(String player) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        Date d1;
        String ll = lastLogin.get(player);
        if (ll != null) {
            try {
                d1 = format.parse(ll);
            } catch (ParseException e) {
                return "Unknown";
            }

            long diff = date.getTime() - d1.getTime();

            // TODO: Use TimeUnit.MLISECOJNDS.toDays() etc

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            diff -= TimeUnit.MILLISECONDS.convert(days,TimeUnit.DAYS);
            long diffHours = TimeUnit.MILLISECONDS.toHours(diff);
            diff -= TimeUnit.MILLISECONDS.convert(diffHours,TimeUnit.HOURS);
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            diff -= TimeUnit.MILLISECONDS.convert(diffMinutes,TimeUnit.MINUTES);
            long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            /*long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000);
            long days = ((diff / (1000*60*60*24)) % 7);
            long weeks = (diff / (1000*60*60*24*7));*/
            String time = "";
            if(Methods.dateFormat == null) {

                if (days != 0) {
                    if (days == 1) {
                        time = days + " day ";
                    } else {
                        time = days + " days ";
                    }
                }

                if (diffHours != 0) {
                    if (diffHours == 1) {
                        time = diffHours + " hour ";
                    } else {
                        time = diffHours + " hours ";
                    }
                }

                if (diffMinutes != 0) {
                    if (diffMinutes == 1) {
                        time = time + diffMinutes + " minute ";
                    } else {
                        time = time + diffMinutes + " minutes ";
                    }
                }
                if (diffSeconds == 1) {
                    time = time + diffSeconds + " second";
                } else {
                    time = time + diffSeconds + " seconds";
                }
            } else {
                time = Methods.dateFormat;
                time = time.replaceAll("%days%","" + days);
                time = time.replaceAll("%hours%","" + diffHours);
                time = time.replaceAll("%minutes%","" + diffMinutes);
                time = time.replaceAll("%seconds%","" + diffSeconds);
            }
            return time;
        } else {
            return "Unknown";
        }


    }

    public static void displayHelp(Player player) {
        if (player.hasPermission("staff.modify")) {
            player.sendMessage(ChatColor.DARK_GRAY + "===========================");
            player.sendMessage(ChatColor.RED + "-------  Commands:  -------");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "/staff - " + ChatColor.RESET + "" + ChatColor.GRAY + "Display online staff");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "/staff help - " + ChatColor.RESET + "" + ChatColor.GRAY + "Display this help -_- (Depending on your permission will depend how far into detail this is)");
            delayedMessage(player, 3, ChatColor.RED + "----- Admin Commands: -----");
            delayedMessage(player, 4, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff add <player> <position> <rank> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Add a staff member to the GUI");
            delayedMessage(player, 5, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff [remove/delete] <player> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Remove the specified staff member from the GUI");
            delayedMessage(player, 6, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff addSpacer <name> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Add the item in your hand as a spacer");
            delayedMessage(player, 7, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff removeSpacer <name> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Remove the specified spacer");
            if (player.hasPermission("staff.reload")) {
                delayedMessage(player, 8, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff reload - " + ChatColor.RESET + "" + ChatColor.GRAY + "Force save of all files and reload the config");
            }
            delayedMessage(player, 9, ChatColor.GRAY + "" + ChatColor.BOLD + "/staff modify - " + ChatColor.RESET + "" + ChatColor.GRAY + "Open the GUI and allow for editing of the positions");

            delayedMessage(player, 10, ChatColor.RED + "------  Admin Usage:  ------");
            delayedMessage(player, 12, ChatColor.GRAY + "" + ChatColor.BOLD + "<player> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Player's name");
            delayedMessage(player, 14, ChatColor.GRAY + "" + ChatColor.BOLD + "<position> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Position in the GUI (1-54)");
            delayedMessage(player, 16, ChatColor.GRAY + "" + ChatColor.BOLD + "<rank> - " + ChatColor.RESET + "" + ChatColor.GRAY + "Player's rank (Can be multiple words)");
            delayedMessage(player, 18, ChatColor.GRAY + "" + ChatColor.BOLD + "addSpacer - " + ChatColor.RESET + "" + ChatColor.GRAY + "Place an item in your hand edited to your own liking and it will add that item as a spacer");
            delayedMessage(player, 20, ChatColor.GRAY + "" + ChatColor.BOLD + "Using Spacers - " + ChatColor.RESET + "" + ChatColor.GRAY + "Once you've added a spacer, you will need to use /staff modify. It will add the spacers to your inventory and allow you to place them inside the GUI where you wish.");
            delayedMessage(player, 22, ChatColor.DARK_GRAY + "===========================");
        } else {
            player.sendMessage(ChatColor.DARK_GRAY + "===========================");
            player.sendMessage(ChatColor.RED + "-------  Commands:  -------");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "/staff - " + ChatColor.RESET + "" + ChatColor.GRAY + "Display online staff");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "/staff help - " + ChatColor.RESET + "" + ChatColor.GRAY + "Display this help -_-");
            player.sendMessage(ChatColor.DARK_GRAY + "===========================");
        }
    }

    public static void displayInfo(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "===========================");
        player.sendMessage(ChatColor.RED + "-------  StaffList:  -------");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Version: " + ChatColor.RESET + "" + ChatColor.GRAY + StaffList._plugin.getDescription().getVersion());
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Author: " + ChatColor.RESET + "" + ChatColor.GRAY + "DaChiimp / George");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Plugin Link: " + ChatColor.RESET + "" + ChatColor.GRAY + "www.dachiimp.com/r/stafflist/");
        player.sendMessage(ChatColor.GRAY + "To display help type /staff help");
        player.sendMessage(ChatColor.DARK_GRAY + "===========================");
    }

    public static void delayedMessage(Player player, long Delay, String msg) {
        final Player p = player;
        final String s = msg;
        final long l = Delay;
        Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
            @Override
            public void run() {
                p.sendMessage(s);
            }
        }, l);
    }

    public static boolean isIgnored(String player) {
        return ignored.contains(player);
    }

    public static void loadStaffList() {
        if (StaffStringList.size() > 0) {
            StaffStringList.clear();
        }

        File file = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);

        List list = yc.getStringList("Staff");

        Iterator l = list.iterator();

        while (l.hasNext()) {
            String s = (String) l.next();
            StaffStringList.add(s);
        }

        loadInvSize();
    }

    public static List<String> getStaffList() {

        return StaffStringList;

    }

    public static void loadPositionList() {
        if (PositionStringList.size() > 0) {
            PositionStringList.clear();
        }

        File file = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);

        List list = yc.getStringList("Size");

        Iterator l = list.iterator();

        while (l.hasNext()) {
            String s = (String) l.next();
            PositionStringList.add(s);
        }

    }

    public static List getPositionList() {

        return PositionStringList;

    }

    public static void addStaffMember(String player, Integer position, String rank) {
        List<String> list = getStaffList();
        List<String> plist = getPositionList();
        if (StaffList.debug) {
            Debug.p("plist = " + plist);
        }
        plist.add(position + "");
        if (StaffList.debug) {
            Debug.p("added " + position + " to plist to get " + plist);
        }
        list.add(player);
        File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        File lfile = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
        YamlConfiguration lyc = YamlConfiguration.loadConfiguration(lfile);
        lyc.set("Staff", list);
        yc.set(player + ".Rank", rank);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        yc.set(player + ".LastLogin", "Unknown");
        yc.set(player + ".Position", position);
        lyc.set("Size", plist);

        SaveMethods.saveFile(file, yc);

        SaveMethods.saveFile(lfile, lyc);

        // add them to current to stop having to reload files

        PositionStringList.add(position + "");
        StaffStringList.add(player);
        staffRank.put(player, rank);

        loadInvSize();


    }

    public static boolean checkStaff(String player) {
        List<String> list = getStaffList();
        if (list.contains(player)) {
            return true;
        } else {
            return false;
        }
    }

    public static void removeStaffMember(String player) {
        List<String> list = getStaffList();
        list.remove(player);
        List<String> posList = getPositionList();
        posList.remove(getPosition(player) + "");
        playerPositions.remove(player);
        File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        File lfile = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
        YamlConfiguration lyc = YamlConfiguration.loadConfiguration(lfile);
        if (lastLogin.containsKey(player)) {
            lastLogin.remove(player);
        }
        lyc.set("Staff", list);
        lyc.set("Size", posList);
        yc.set(player, null);

        SaveMethods.saveFile(file, yc);
        SaveMethods.saveFile(lfile, lyc);

        loadInvSize();
    }

    public static void noCommandPerm(Player player) {
        player.sendMessage(ChatColor.RED + "You do not have access to that command.");
    }

    public static void unknownCommand(Player player) {
        player.sendMessage(ChatColor.RED + "Unknown command. Type /staff help for more information");
    }

    public static void unknownCommand(CommandSender player) {
        player.sendMessage("Unknown command.");
    }

    public static boolean isEssentialsVanished(Player player) {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
       /* if(Bukkit.getPluginManager().getPlugin("StaffPlus").isEnabled()) {
            // has staff plus
            if(StaffPlus.get().userManager.get(player.getUniqueId()).getVanishType() != VanishHandler.VanishType.NONE) {
                // is vanished
                return true;
            }
        }*/
        if (ess != null) {
            List<String> vanished = ess.getVanishedPlayers();
            if (vanished.contains(player.getName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void loadInvSize() {
        if (StaffList.dynamicInv) {
            if (StaffList.debug) {
                Debug.p("getInvSize()");
            }
            File lfile = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
            YamlConfiguration lyc = YamlConfiguration.loadConfiguration(lfile);
            if (lfile.exists() && lyc.contains("Size")) {
                if (lyc.getStringList("Size").size() == 0) {
                    invSize = 666;
                    return;
                } else {
                    //
                }
                List<String> list = lyc.getStringList("Size");
                Iterator it = list.iterator();
                Integer maxSize = null;
                while (it.hasNext()) {
                    String s = (String) it.next();
                    if (StaffList.debug) {
                        Debug.p(s);
                    }
                    try {
                        Integer i = Integer.parseInt(s);
                        if (StaffList.debug) {
                            Debug.p("maxSize = " + maxSize + " | i = " + i);
                        }
                        if (maxSize == null) {
                            maxSize = i;
                        } else {
                            if (maxSize < i) {
                                maxSize = i;
                            }
                        }
                    } catch (NumberFormatException e) {
                        Debug.p(s + " is not an int");
                    }
                }


                int size = 54;
                if (maxSize < 9) {
                    size = 9;
                } else if (maxSize < 18) {
                    size = 18;
                } else if (maxSize < 27) {
                    size = 27;
                } else if (maxSize < 36) {
                    size = 36;
                } else if (maxSize < 45) {
                    size = 45;
                } else if (maxSize < 54) {
                    size = 54;
                }

                int ms = 0;

                for (int i : SpacerMethods.savedSpacerPositions.keySet()) {
                    if (i > ms) {
                        ms = i;
                    }
                }

                int s = 54;

                if (ms < 9) {
                    s = 9;
                } else if (ms < 18) {
                    s = 18;
                } else if (ms < 27) {
                    s = 27;
                } else if (ms < 36) {
                    s = 36;
                } else if (ms < 45) {
                    s = 45;
                } else if (ms < 54) {
                    s = 54;
                }

                if (s > size) {
                    size = s;
                }

                if (StaffList.debug) {
                    Debug.p("getInvSize() returns " + size + " from maxSize of " + maxSize);
                }

                invSize = size;
            } else {
                invSize = 666;
            }
        } else {
            invSize = 54;
        }


    }

    public static Integer getInvSize() {

        return invSize;

    }

    @SuppressWarnings("deprecation,unused")
    public static boolean isNoPacketVanished(String player) {
        if (StaffList.debug) {
            Debug.p("isNoPacketVanished() called");
        }
        if (Bukkit.getServer().getPluginManager().getPlugin("VanishNoPacket") != null) {
            VanishPlugin vnp = (VanishPlugin) Bukkit.getServer().getPluginManager().getPlugin("VanishNoPacket");
            boolean vnpEnabled = vnp.isEnabled();
            if (StaffList.debug) {
                Debug.p("VanishNoPacket is " + vnpEnabled);
            }
            if (vnpEnabled) {

                try {
                    if (VanishNoPacket.isVanished(player)) {
                        if (StaffList.debug) {
                            Debug.p(player + " is vanished");
                        }
                        return true;
                    } else {
                        if (StaffList.debug) {
                            Debug.p(player + " is not vanished");
                        }
                        return false;
                    }
                } catch (Exception e) {
                    Debug.p("Error checking if " + player + "is vanished. Error:");
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (StaffList.debug) {
                Debug.p("VanishNoPacket plugin is not existent");
            }
            return false;
        }

    }

    public static String getCfgValue(String value) {

        File file = new File(StaffList._plugin.getDataFolder(),"config.yml");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        if (yc.contains(value)) {
            return yc.getString(value);
        } else {
            return null;
        }
    }

    public static String getValue(String value) {
        File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        if (yc.contains(value)) {
            return yc.getString(value);
        } else {
            return null;
        }
    }

    public static void loadPlayerPositions() {

        File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);

        List<String> list = getStaffList();

        for (int i = 0; i < list.size(); i++) {
            String player = list.get(i);
            String sposition = yc.getString(player + ".Position");
            int position = 666;
            try {
                position = Integer.parseInt(sposition);
            } catch (NumberFormatException e) {
                playerPositions.put(player, 666);
            }
            if (StaffList.debug) {
                Debug.p("loadPlayerPositions() gets " + position + " from " + player);
            }
            playerPositions.put(player, position);
        }


    }

    public static Integer getPosition(String player) {
        if (playerPositions.size() == 0) {
            loadPlayerPositions();
        }

        if (playerPositions.get(player) == null) {
            loadPlayerPositions();
            if (playerPositions.get(player) == null) {
                return null;
            } else {
                return playerPositions.get(player);
            }
        } else {
            return playerPositions.get(player);
        }

    }

    public static String replaceAll(String string, String player) {
        String s = string;
        s = s.replaceAll("%player%", player);
        if (Bukkit.getServer().getPlayer(player) != null) {
            Player p = Bukkit.getServer().getPlayer(player);
            s = s.replaceAll("%world%", p.getWorld().getName());
            s = s.replaceAll("%x%", p.getLocation().getBlockX() + "");
            s = s.replaceAll("%y%", p.getLocation().getBlockY() + "");
            s = s.replaceAll("%z%", p.getLocation().getBlockZ() + "");
        } else {
            s = s.replaceAll("%world%", "Error getting world");
            s = s.replaceAll("%x%", "?");
            s = s.replaceAll("%y%", "?");
            s = s.replaceAll("%z%", "?");
        }

        if (staffRank.get(player) == null) {
            s = s.replaceAll("%rank%", "Unknown");
        } else {
            s = s.replaceAll("%rank%", staffRank.get(player));
        }

        if (lastLogin.get(player) == null) {
            s = s.replaceAll("%lastlogin%", "Unknown");
        } else {
            s = s.replaceAll("%lastlogin%", lastLogin.get(player));
        }


        String ot = getOnlineTime(player);
        if (ot == null) {
            s = s.replaceAll("%onlinetime%", "Unknown");
        } else {
            s = s.replaceAll("%onlinetime%", ot);
        }


        s = s.replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");

        return s;
    }

    public static void setupBungeeServers() {
        bungeeServers.clear();
        String servers = getCfgValue("BungeeServers");
        if (StaffList.bungee) {
            if (servers == null) {
                Debug.p("Bungee is set to true but there was an error getting the bungee servers list");
            } else {
                if (servers.contains(",")) {
                    String[] serverAr = servers.split(",");
                    for (String server : serverAr) {
                        if (StaffList.debug) {
                            Debug.p("setupBungeeServers(); Put " + server + " into bungeeServers array with null value.");
                        }

                        bungeeServers.put(server, null);
                    }
                } else {
                    if (StaffList.debug) {
                        Debug.p("setupBungeeServers(); Put " + servers + " into bungeeServers array with null value as it was there was no comma. Assuming there is only 1 server.");
                    }

                    bungeeServers.put(servers, null);
                }
            }
        }
    }

    public static void loadLastLogins() {

        File f = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        if (f.exists()) {
            YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);

            List<String> list = getStaffList();

            for (String player : list) {
                if (yc.contains(player)) {
                    String lastLog = getValue(player + ".LastLogin");
                    lastLogin.put(player, lastLog);
                }
            }
        }
    }

    static void saveLastLogins() {
        Set set = lastLogin.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String next = (String) it.next();
            String date = lastLogin.get(next);

            File f = new File(StaffList._plugin.getDataFolder(),"staff.dat");
            YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);

            yc.set(next + ".LastLogin", date);

            SaveMethods.saveFile(f, yc);
        }
    }

    private static void saveRanks() {
        File f = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);
        boolean modified = false;
        for (String p : staffRank.keySet()) {
            if (!yc.contains(p + ".Rank") || !yc.getString(p + ".Rank").equalsIgnoreCase(staffRank.get(p))) {
                modified = true;
                yc.set(p + ".Rank", staffRank.get(p));
            }
        }
        if (modified) {
            SaveMethods.saveFile(f, yc);
        }
    }

    private static void loadRanks() {
        if (staffRank.size() > 0) {
            staffRank.clear();
        }

        List<String> list = getStaffList();

        for (int i = 0; i < list.size(); i++) {
            String player = list.get(i);
            staffRank.put(player, getValue(player + ".Rank"));
        }


    }

    public static void updateGuiTemplates() {

        if (guiTemplate.size() > 0) {

            guiTemplate.clear();

        }

        guiTemplate.put("line1", getCfgValue("line1"));
        guiTemplate.put("line2", getCfgValue("line2"));
        guiTemplate.put("line3", getCfgValue("line3"));
        guiTemplate.put("line4", getCfgValue("line4"));
        guiTemplate.put("line5", getCfgValue("line5"));
        guiTemplate.put("lastline_ONLINE", getCfgValue("lastline_ONLINE"));
        guiTemplate.put("lastline_ONLINE_BUNGEE", getCfgValue("lastline_ONLINE_BUNGEE"));
        guiTemplate.put("lastline_OFFLINE", getCfgValue("lastline_OFFLINE"));

    }

    public static String getGUITemplate(String val) {

        if (guiTemplate.size() != 8) {
            updateGuiTemplates();
        }

        switch (val) {
            case "StaffHeadTitle": {
                return guiTemplate.get("StaffHeadTitle");
            }
            case "line1": {
                return guiTemplate.get("line1");
            }
            case "line2": {
                return guiTemplate.get("line2");
            }
            case "line3": {
                return guiTemplate.get("line3");
            }
            case "line4": {
                return guiTemplate.get("line4");
            }
            case "line5": {
                return guiTemplate.get("line5");
            }
            case "lastline_ONLINE": {
                return guiTemplate.get("lastline_ONLINE");
            }
            case "lastline_OFFLINE": {
                return guiTemplate.get("lastline_OFFLINE");
            }
            case "lastline_ONLINE_BUNGEE": {
                return guiTemplate.get("lastline_ONLINE_BUNGEE");
            }
            default: {
                return "Error";
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        if (checkStaff(e.getPlayer().getName())) {


            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            lastLogin.put(player.getName(), dateFormat.format(date));

            if (StaffList.debug) {
                Debug.p(e.getPlayer().getName() + " is staff, set last login to" + dateFormat.format(date));
            }
        }
    }


}
