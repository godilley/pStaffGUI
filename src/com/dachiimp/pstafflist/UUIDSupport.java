package com.dachiimp.pstafflist;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Created by DaChiimp on 5/16/2016.
 */
public class UUIDSupport implements Listener {


    static HashMap<UUID, String> uuid = new HashMap<>();

    public static void updateUUID(Player player) {
        String name = getOldNameFromUUID(player.getUniqueId());

        uuid.remove(player.getUniqueId());
        uuid.put(player.getUniqueId(), player.getName());

        if (StaffList.debug) {
            Debug.p("The oldname of " + player.getName() + " is " + name);
            Debug.p("uuid array = " + uuid);
        }


        File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        /*
        File lfile = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
        YamlConfiguration lyc = YamlConfiguration.loadConfiguration(lfile);

        List<String> nlist = lyc.getStringList("Staff");
        if(StaffList.debug) {
            Debug.p("Removing " + name + " from " + nlist + " and adding " + player.getName());
        }
        nlist.remove(name);
        nlist.add(player.getName());

        lyc.set("Staff", nlist);

        try{
            lyc.save(lfile);
            Debug.p("Saved new staff list to file /staffList.dat");
        } catch(IOException e) {
            e.printStackTrace();
            Debug.p("Error saving new staff list to file /staffList.dat");
        } finally {
            saveUUIDs();
        }*/


        String rank = yc.getString(name + ".Rank");
        String lastLogin = yc.getString(name + ".LastLogin");
        String position = yc.getString(name + ".Position");

        Methods.removeStaffMember(name);

        try {
            int pos = Integer.parseInt(position);
            Methods.addStaffMember(player.getName(), pos, rank);
            player.sendMessage(ChatColor.GREEN + "[pStaffList] Your username has been updated!");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error updating your new name");
            return;
        } finally {
            Methods.lastLogin.put(player.getName(), lastLogin);
            Methods.saveLastLogins();
        }

    }

    public static void loadUUIDs() {

        uuid.clear();

        File file = new File(StaffList._plugin.getDataFolder(),"uuid.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        Set keys = yc.getKeys(false);
        for (Object k : keys) {
            if (StaffList.debug) {
                Debug.p("Put " + k + " into uuid array with value " + yc.getString(k.toString()));
            }
            uuid.put(UUID.fromString(k.toString()), yc.getString(k.toString()));
        }
    }

    public static void saveUUIDs() {
        File file = new File(StaffList._plugin.getDataFolder(),"uuid.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
        Set set = uuid.keySet();
        Iterator it = set.iterator();

        while (it.hasNext()) {
            UUID uu = (UUID) it.next();
            String name = uuid.get(uu);
            if (StaffList.debug) {
                Debug.p("uuid = " + uu);
                Debug.p("name = " + name);
            }
            yc.set(uu.toString(), name);

        }
        SaveMethods.saveFile(file, yc);

        loadUUIDs();

    }

    public static String getOldNameFromUUID(UUID uu) {
        return uuid.get(uu);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer() != null) {
            Player player = e.getPlayer();
            if (uuid.containsKey(player.getUniqueId())) {
                if (uuid.get(player.getUniqueId()).equalsIgnoreCase(player.getName())) {
                    // Player's name is up to date with UUID
                } else {
                    if (StaffList.debug) {
                        Debug.p("The UUID " + player.getUniqueId() + "'s name of " + player.getName() + " does not match the file name of " + uuid.get(player.getUniqueId()));
                    }
                    updateUUID(player);
                }
            } else {
                if (StaffList.debug) {
                    Debug.p("Added " + player.getUniqueId() + " to uuid array");
                }
                uuid.put(player.getUniqueId(), player.getName());
            }
        }
    }
}
