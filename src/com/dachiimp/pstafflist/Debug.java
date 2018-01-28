package com.dachiimp.pstafflist;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by DaChiimp on 4/8/2015.
 */
public class Debug {

    public static void p(String s) {
        ConsoleCommandSender ccs = StaffList._plugin.getServer().getConsoleSender();
        ccs.sendMessage(ChatColor.DARK_RED + "[pStaffList] " + ChatColor.BLUE + s);
    }
}
