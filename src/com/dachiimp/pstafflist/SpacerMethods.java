package com.dachiimp.pstafflist;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by DaChiimp on 5/16/2016.
 */
public class SpacerMethods implements Listener {

     /*
       /staff addSpacer <NAME>
     */

    static HashMap<UUID, ArrayList<ItemStack>> oldInventory = new HashMap<>();

    static HashMap<String, ItemStack> spacers = new HashMap<>();

    static ArrayList<String> spacerNamesArray = new ArrayList<>();

    static HashMap<Integer, String> savedSpacerPositions = new HashMap<>();

    static ArrayList<String> removedSpacers = new ArrayList<>();


    static void addSpacer(String spacerName, ItemStack item) {
        item.setAmount(1);
        // Adds a spacer to the map so that it can be pulled into inv
        spacerNamesArray.add(spacerName);
        spacers.put(spacerName, item);
    }

    static void removeSpacer(String spacerName) {
        // Adds a spacer to the map so that it can be pulled into inv
        spacerNamesArray.remove(spacerName);
        spacers.remove(spacerName);
        if (savedSpacerPositions.containsValue(spacerName)) {
            for (int i = 0; i < 53; i++) {
                if (savedSpacerPositions.containsKey(i)) {
                    if (savedSpacerPositions.get(i).equalsIgnoreCase(spacerName)) {
                        savedSpacerPositions.remove(i);
                        removedSpacers.add(spacerName);
                        Methods.loadInvSize();
                    }
                }
            }
        }
    }

    static void saveSpacers() {
        File f = new File(StaffList._plugin.getDataFolder(),"spacers.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);
        for (String s : spacerNamesArray) {
            if (yc.contains(s)) {
                if (yc.getItemStack(s).equals(spacers.get(s))) {
                    // it's saved so no need to save it
                    if (StaffList.debug) {
                        Debug.p(s + " is saved as a spacer - no need to add");
                    }
                } else {
                    // not the same, so save it
                    yc.set(s, spacers.get(s));
                    if (StaffList.debug) {
                        Debug.p(s + " is saved as a spacer, but is not the same as in the array | saving as " + spacers.get(s));
                    }
                }
            } else {
                // need to add it in the first place
                yc.set(s, spacers.get(s));
                if (StaffList.debug) {
                    Debug.p(s + " is not saved at all | saving as " + spacers.get(s));
                }
            }
        }

        for (String s : removedSpacers) {
            if (yc.contains(s)) {
                yc.set(s, null);
            }
        }

        // set it to the list so you can iterate through the spacers when loading
        yc.set("Spacers", spacerNamesArray);

        SaveMethods.saveFile(f, yc);
    }

    static void loadSpacers() {

        if (spacerNamesArray.size() > 0) {
            spacerNamesArray.clear();
        }

        /*
            Saves as:
            SpacerName: ItemStack
         */
        File f = new File(StaffList._plugin.getDataFolder(),"spacers.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);

        // get spacers from list
        List<String> spacersList = yc.getStringList("Spacers");

        for (String s : spacersList) {
            spacerNamesArray.add(s);
            if (yc.getItemStack(s) != null) {
                spacers.put(s, yc.getItemStack(s));
            } else {
                Debug.p("Error getting spacer " + s);
            }
        }
    }


    static void saveSpacerPositions(Inventory inv, Player player) {
        savedSpacerPositions.clear();

        if (StaffList.debug) {
            Debug.p("saveSpacerPositions() | inv: " + inv);
        }
        for (int i = 0; i <= 53; i++) {
            if (spacers.containsValue(inv.getItem(i))) {
                if (StaffList.debug) {
                    player.sendMessage("saveSpacerPositions() | containsValue: " + i + " / " + inv.getItem(i));
                }
                // is a spacer so save it
                for (String s : spacerNamesArray) {
                    if (spacers.get(s).equals(inv.getItem(i))) {
                        savedSpacerPositions.put(i, s);
                    }
                }

            }
        }
        Methods.loadInvSize();
        if (StaffList.debug) {
            Debug.p("savedSpacerPositions: " + savedSpacerPositions);
        }
    }

    static void saveSpacerPositions() {
        File f = new File(StaffList._plugin.getDataFolder(),"spacerpositions.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);
        if (StaffList.debug) {
            Debug.p("savedSpacers: " + savedSpacerPositions);
        }
        for (int i = 0; i < 54; i++) {


            yc.set(i + "", null);

            if (savedSpacerPositions.containsKey(i)) {
                if (StaffList.debug) {
                    Debug.p("saveSpacerPositions() - Contains Key: " + i + " as " + savedSpacerPositions.get(i));
                }
                if (removedSpacers.contains(savedSpacerPositions.get(i))) {
                    yc.set(i + "", null);
                } else {
                    if (StaffList.debug) {
                        Debug.p("saveSpacerPositions() - Setting: " + i + " as " + savedSpacerPositions.get(i));
                    }
                    yc.set(i + "", savedSpacerPositions.get(i));
                }
            }

            for (String s : removedSpacers) {
                if (yc.contains(i + "")) {
                    if (yc.getString(i + "").equalsIgnoreCase(s)) {
                        yc.set(i + "", null);
                    }
                }
            }
        }

        SaveMethods.saveFile(f, yc);
    }

    public static void loadSpacerPositions() {
        if (savedSpacerPositions.size() > 0) {
            savedSpacerPositions.clear();
        }
        File f = new File(StaffList._plugin.getDataFolder(),"spacerpositions.dat");
        YamlConfiguration yc = YamlConfiguration.loadConfiguration(f);
        for (String s : spacerNamesArray) {
            for (int i = 0; i <= 54; i++) {
                if (yc.contains(i + "")) {
                    if (StaffList.debug) {
                        Debug.p("loadSpacerPositions() - yc contains: " + i + " as " + yc.getString(i + ""));
                    }
                    savedSpacerPositions.put(i, yc.getString(i + ""));
                }
            }
        }
    }

    public static void setupInvWithSpacers(Player player) {
        Inventory inv = player.getInventory();

        ArrayList<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i) != null) {
                if (inv.getItem(i).getType() != Material.AIR) {
                    ItemStack it = player.getInventory().getItem(i);
                    items.add(it);
                    ItemStack air = new ItemStack(Material.AIR);
                    player.getInventory().setItem(i, air);
                }
            }
        }

        oldInventory.put(player.getUniqueId(), items);


        for (String s : spacerNamesArray) {
            ItemStack it = spacers.get(s);
            int ba = it.getAmount();
            it.setAmount(64);
            inv.addItem(it);
            it.setAmount(ba);
        }
        player.updateInventory();
    }

    public static void giveBackOldInv(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (inv.getItem(i) != null) {
                if (inv.getItem(i).getType() != Material.AIR) {
                    ItemStack air = new ItemStack(Material.AIR);
                    player.getInventory().setItem(i, air);
                }
            }
        }
        for (ItemStack it : oldInventory.get(player.getUniqueId())) {
            if (it != null) {
                if (it.getType() != Material.AIR) {
                    player.getInventory().addItem(it);
                }
            }
        }
        player.updateInventory();
        oldInventory.remove(player.getUniqueId());
    }


    public static List getSpacerNames() {
        return spacerNamesArray;
    }

    public static HashMap<String, ItemStack> getSpacers() {
        return spacers;
    }

    public static Inventory addSpacersToGUIInv(Inventory inv) {
        for (int i = 0; i < 54; i++) {
            if (savedSpacerPositions.containsKey(i)) {
                ItemStack it = spacers.get(savedSpacerPositions.get(i));
                inv.setItem(i, it);
            }
        }

        return inv;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (oldInventory.containsKey(e.getPlayer().getUniqueId())) {
            Player player = e.getPlayer();
            Inventory inv = player.getInventory();
            for (int i = 0; i < 36; i++) {
                if (inv.getItem(i) != null) {
                    if (inv.getItem(i).getType() != Material.AIR) {
                        ItemStack air = new ItemStack(Material.AIR);
                        player.getInventory().setItem(i, air);
                    }
                }
            }
            for (ItemStack it : oldInventory.get(player.getUniqueId())) {
                if (it != null) {
                    if (it.getType() != Material.AIR) {
                        player.getInventory().addItem(it);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (oldInventory.containsKey(e.getPlayer().getUniqueId())) {
            Player player = e.getPlayer();
            Inventory inv = player.getInventory();
            for (int i = 0; i < 36; i++) {
                if (inv.getItem(i) != null) {
                    if (inv.getItem(i).getType() != Material.AIR) {
                        ItemStack air = new ItemStack(Material.AIR);
                        player.getInventory().setItem(i, air);
                    }
                }
            }
            for (ItemStack it : oldInventory.get(player.getUniqueId())) {
                if (it != null) {
                    if (it.getType() != Material.AIR) {
                        player.getInventory().addItem(it);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (oldInventory.containsKey(e.getPlayer().getUniqueId())) {
            Player player = e.getPlayer();
            Inventory inv = player.getInventory();
            for (int i = 0; i < 36; i++) {
                if (inv.getItem(i) != null) {
                    if (inv.getItem(i).getType() != Material.AIR) {
                        ItemStack air = new ItemStack(Material.AIR);
                        player.getInventory().setItem(i, air);
                    }
                }
            }
            for (ItemStack it : oldInventory.get(player.getUniqueId())) {
                if (it != null) {
                    if (it.getType() != Material.AIR) {
                        player.getInventory().addItem(it);
                    }
                }
            }
            player.updateInventory();
            oldInventory.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (oldInventory.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().setItemOnCursor(e.getItemDrop().getItemStack());
        }
    }
}
