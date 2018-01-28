package com.dachiimp.pstafflist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by DaChiimp on 6/25/2016.
 */
public class onJoinEventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("staff.modify")) {
            final Player p = player;
            Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
                @Override
                public void run() {

                    if (!StaffList.newestVersionBol && !StaffList.hideUpdateMessage) {
                        p.sendMessage(ChatColor.DARK_BLUE + "[pStaffList]" + ChatColor.RED + " You are not using the newest plugin version. There is an update available at " + ChatColor.GREEN + "http://www.dachiimp.com/r/stafflist");
                    }
                }
            }, 20L);
        }
    }


}
