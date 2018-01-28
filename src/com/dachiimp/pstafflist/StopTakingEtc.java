package com.dachiimp.pstafflist;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by DaChiimp on 4/6/2015.
 */
public class StopTakingEtc implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getInventory().getName().equalsIgnoreCase(Methods.getInvTitle())) {
                if (e.getClickedInventory().getType().equals(InventoryType.CHEST)) {
                    e.setCancelled(true);
                }
            } else if (e.getInventory().getName().equalsIgnoreCase(ChatColor.DARK_RED + "Editing Staff")) {
                Player player = (Player) e.getWhoClicked();
                if (e.getClickedInventory() != null) {
                    if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                        if (e.getCursor().getType().equals(Material.SKULL_ITEM)) {
                            e.setCancelled(true);
                            player.sendMessage("Canceled");
                        }
                    } else if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                        if (e.getCurrentItem().getType().equals(Material.SKULL) || e.getCurrentItem().getType().equals(Material.SKULL)) {
                            e.setCancelled(true);
                            player.getInventory().remove(e.getCurrentItem());
                            player.updateInventory();
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getName().equalsIgnoreCase(ChatColor.DARK_RED + "Editing Staff")) {
            SpacerMethods.saveSpacerPositions(e.getInventory(), (Player) e.getPlayer());
            File file = new File(StaffList._plugin.getDataFolder(),"staff.dat");
            YamlConfiguration yc = YamlConfiguration.loadConfiguration(file);
            File lfile = new File(StaffList._plugin.getDataFolder(),"staffList.dat");
            YamlConfiguration lyc = YamlConfiguration.loadConfiguration(lfile);
            if (e.getPlayer().hasPermission("staff.modify")) {
                ArrayList<String> newlist = new ArrayList<>();
                ArrayList<String> posSList = new ArrayList<>();
                ArrayList<Integer> posList = new ArrayList<>();
                List<String> list = Methods.getStaffList();
                if (StaffList.debug) {
                    Debug.p(list + "");
                }
                for (int i = 0; i <= 53; i++) {
                    if (StaffList.debug) {
                        Debug.p(i + "");
                    }
                    ItemStack item = e.getInventory().getItem(i);
                    ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                    ItemMeta im = head.getItemMeta();
                    if (item != null) {
                        ItemMeta nim = item.getItemMeta();
                        item.setItemMeta(im);
                        if (!item.isSimilar(head)) {
                            if (StaffList.debug) {
                                Debug.p(item + " is not similar");
                            }
                        } else {

                            posList.add(i);
                            SkullMeta sm = (SkullMeta) nim;
                            String owner = sm.getOwner();
                            if (list.contains(owner)) {
                                if (StaffList.debug) {
                                    Debug.p("list contains " + owner);
                                }
                                yc.set(sm.getOwner() + ".Position", i + "");
                                newlist.add(sm.getOwner());
                            } else {
                                for (String s : list) {
                                    if (s.toLowerCase().equalsIgnoreCase(owner)) {
                                        // fuck you lowercase
                                        yc.set(s + ".Position", i + "");
                                        newlist.add(s);
                                    } else {
                                        //
                                    }
                                }

                            }
                        }
                    }

                }
                Iterator it = posList.iterator();
                while (it.hasNext()) {
                    posSList.add(it.next() + "");
                }


                lyc.set("Staff", newlist);
                lyc.set("Size", posList);
                if (StaffList.debug) {
                    Debug.p("Staff " + newlist);
                    Debug.p("Size " + posSList);
                }

                SaveMethods.saveFile(file, yc);
                SaveMethods.saveFile(lfile, lyc);

                SpacerMethods.giveBackOldInv((Player) e.getPlayer());
                Methods.reloadAllFiles();
            }
        }
    }

}
