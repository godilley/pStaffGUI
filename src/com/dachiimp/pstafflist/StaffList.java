package com.dachiimp.pstafflist;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by DaChiimp on 4/6/2015.
 */
public class StaffList extends JavaPlugin implements Listener, PluginMessageListener {

    static Plugin _plugin = null;

    static boolean bungee;
    static boolean dynamicInv;

    static boolean s1;
    static boolean s2;
    static boolean s3;
    static boolean s4;
    static boolean s5;
    static boolean sio;

    static boolean hideUpdateMessage = false;


    static boolean showOnlyIfOnline;

    static boolean addHeadsInNextFreeSlot;

    static boolean debug = false;
    static boolean newestVersionBol = true;
    static long updateInterval;
    private static long interval;
    private String readurl = "http://www.dachiimp.com/pStaffList/currentVersion.txt";
    private String currentVersion = this.getDescription().getVersion();

    static void setupBooleans() {
        if (new File(_plugin.getDataFolder(),"config.yml").exists()) {
            if (Methods.getCfgValue("ShowLine1").equalsIgnoreCase("true")) {
                s1 = true;
            } else {
                s1 = false;
            }

            if (Methods.getCfgValue("ShowLine2").equalsIgnoreCase("true")) {
                s2 = true;
            } else {
                s2 = false;
            }

            if (Methods.getCfgValue("ShowLine3").equalsIgnoreCase("true")) {
                s3 = true;
            } else {
                s3 = false;
            }

            if (Methods.getCfgValue("ShowLine4").equalsIgnoreCase("true")) {
                s4 = true;
            } else {
                s4 = false;
            }

            if (Methods.getCfgValue("ShowLine5").equalsIgnoreCase("true")) {
                s5 = true;
            } else {
                s5 = false;
            }

            if (Methods.getCfgValue("UsingBungee").equalsIgnoreCase("true")) {
                bungee = true;
            } else {
                bungee = false;
            }

            if (Methods.getCfgValue("DynamicInvSize").equalsIgnoreCase("true")) {
                dynamicInv = true;
            } else {
                dynamicInv = false;
            }

            if (Methods.getCfgValue("ShowIfOnline").equalsIgnoreCase("true")) {
                sio = true;
            } else {
                sio = false;
            }

            if (Methods.getCfgValue("OnlyShowIfOnline").equalsIgnoreCase("true")) {
                showOnlyIfOnline = true;
            } else {
                showOnlyIfOnline = false;
            }

            if (Methods.getCfgValue("AddHeadsInNextFreeSlot").equalsIgnoreCase("true")) {
                addHeadsInNextFreeSlot = true;
            } else {
                addHeadsInNextFreeSlot = false;
            }

            if (Methods.getCfgValue("SaveInterval") == null) {
                interval = 600;
            } else {
                try {
                    interval = Integer.parseInt(Methods.getCfgValue("SaveInterval"));
                    Debug.p("Saves data every " + interval + " seconds");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    interval = 600;
                }
            }

            if(Methods.getCfgValue("DateFormat")== null) {
                Debug.p("Config doesn't contain 'DateFormat' therefore, using default");
            } else {
                Methods.dateFormat = Methods.getCfgValue("DateFormat");
            }

            if(Methods.getCfgValue("HideUpdateMessage")== null) {
                Debug.p("Config doesn't contain 'HideUpdateMessage' therefore, using default of false");
                hideUpdateMessage = false;
            } else {
                if(Methods.getCfgValue("HideUpdateMessage").equalsIgnoreCase("true")) {
                    hideUpdateMessage = true;
                } else {
                    hideUpdateMessage = false;
                }
            }

            if (Methods.getCfgValue("BungeeUpdateInterval") == null) {
                updateInterval = 30;
            } else {
                try {
                    updateInterval = Integer.parseInt(Methods.getCfgValue("BungeeUpdateInterval"));
                    Debug.p("Updates player list from bungee servers every " + updateInterval + " seconds");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    updateInterval = 30;
                }
            }


        } else {
            setupConfig();
            setupBooleans();
        }

    }

    private static void setupDir() {
        File file = new File(_plugin.getDataFolder(),"");
        File f = new File(_plugin.getDataFolder(),"spacers.dat");
        File f2 = new File(_plugin.getDataFolder(),"spacerpositions.dat");
        File f3 = new File(_plugin.getDataFolder(),"uuid.dat");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                Debug.p("Error creating spacers file");
                e.printStackTrace();
            }
        }
        if (!f2.exists()) {
            try {
                f2.createNewFile();
            } catch (IOException e) {
                Debug.p("Error creating spacer positions file");
                e.printStackTrace();
            }
        }
        if (!f3.exists()) {
            try {
                f3.createNewFile();
            } catch (IOException e) {
                Debug.p("Error creating spacer positions file");
                e.printStackTrace();
            }
        }
    }

    private static void setupConfig() {
        File file = new File(_plugin.getDataFolder(),"config.yml");

        if (!file.exists()) {
            Debug.p("Created config as one didn't exist");
            _plugin.saveDefaultConfig();
        }

    }

    public void onEnable() {
        System.out.println("[" + getDescription().getName() + "] enabled");
        _plugin = Bukkit.getServer().getPluginManager().getPlugin("pStaffList");
        setupDataFolder();
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new StopTakingEtc(), this);
        pm.registerEvents(this, this);
        pm.registerEvents(new SpacerMethods(), this);
        pm.registerEvents(new Methods(), this);
        pm.registerEvents(new UUIDSupport(), this);
        pm.registerEvents(new onJoinEventListener(), this);
        getCommand("Staff").setExecutor(new onCommandExecutorClass());
        if (new File(_plugin.getDataFolder(),"deleteMe.txt").exists()) {
            System.out.print("Deleting deleteMe.txt");
            new File(_plugin.getDataFolder(),"deleteMe.txt").delete();
        }
        setupDir();
        setupConfig();
        updateConfig();
        setupBooleans();
        Methods.loadAllFiles();
        onCommandExecutorClass.setupBlockedNames();

        if (bungee)
            Methods.playerListScheduler();

        Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    Debug.p("Retrieving players as there are already players online. Assumed as a /reload");
                    Methods.bungeeServersUpdatePlayers();
                }
                Methods.loadInvSize();

            }
        }, 8L);

        //BungeeCord Shit
        if (bungee) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        }

        //Check for pStaffList
        Methods.checkPStaffList();

        setupSave();

        checkForUpdate();


    }

    public void onDisable() {
        System.out.println("[" + getDescription().getName() + "] disabled");
        Methods.saveAllFiles();
        _plugin = null;
    }

    private void setupSave() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable() {
            @Override
            public void run() {

                Methods.reloadAllFiles();
                setupSave();
            }
        }, interval * 20);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(msg);
        String subchannel = in.readUTF();
        if (debug) {
            Debug.p("onPluginMessageRecieved");
            Debug.p("subchannel = " + subchannel);

        }
        if (subchannel.equals("PlayerList")) {
            @SuppressWarnings("unused")
            String server = in.readUTF();
            String[] playerList = in.readUTF().split(", ");
            if (server.equals("ALL")) {
                if (debug) {
                    Debug.p(server + "");
                    Debug.p(playerList + "");
                }
            } else {
                ArrayList<String> s = new ArrayList<>();
                for (String st : playerList) {
                    s.add(st);
                }

                Methods.bungeeServers.put(server, s);
                if (debug) {
                    Debug.p("@onPluginMessageReceived; Put server " + server + " with array " + s);
                }
            }
        }
    }

    private void setupDataFolder() {
        File dir = new File(_plugin.getDataFolder(),"");
        File file = new File(_plugin.getDataFolder(),"staff.dat");
        File lfile = new File(_plugin.getDataFolder(),"staffList.dat");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error creating staff.dat file.");
            }
        }
        if (!lfile.exists()) {
            try {
                lfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error creating dat list file.");
            }
        }
    }

    private void updateConfig() {
        // check for each string
        File file = new File(_plugin.getDataFolder(),"config.yml");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        boolean modified = false;
        if (!yc.contains("OnlyShowIfOnline")) {
            yc.set("OnlyShowIfOnline", "false");
            Debug.p("Updated config to add option 'OnlyShowIfOnline'");
            modified = true;
        }

        if (!yc.contains("AddHeadsInNextFreeSlot")) {
            yc.set("AddHeadsInNextFreeSlot", "true");
            Debug.p("Updated config to add option 'AddHeadsInNextFreeSlot'");
            modified = true;
        }
        if (!yc.contains("BungeeUpdateInterval")) {
            yc.set("BungeeUpdateInterval", "30");
            Debug.p("Updated config to add option 'BungeeUpdateInterval'");
            modified = true;
        }

        boolean saved = false;

        if (modified) {
            try {
                yc.save(file);
                saved = true;
            } catch (IOException e) {
                Debug.p("Error saving updated config, will retry once.");
            } finally {
                if (!saved) {
                    try {
                        yc.save(file);
                    } catch (IOException e) {
                        Debug.p("Error saving updated config, therefore the plugin will be disabled. If this error keeps occuring, delete the config and it will reset.");
                        e.printStackTrace();
                        Bukkit.getServer().getPluginManager().disablePlugin(this);
                    }
                }
            }
        }
    }

    private void checkForUpdate() {
        try {
            Debug.p(ChatColor.AQUA + "Checking for a new version...");
            URL url = new URL(readurl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = br.readLine()) != null) {
                if (!str.equalsIgnoreCase(currentVersion)) {
                    newestVersionBol = false;
                    Debug.p("You are not using the newest plugin version. Newest Version = " + str);
                } else {
                    Debug.p("You are using the newest plugin version.");
                }
            }
            br.close();
        } catch (IOException e) {
            Debug.p(ChatColor.RED + "The UpdateChecker URL is invalid! Please let me know!");
        }
    }


}
