package com.dachiimp.pstafflist;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;


/**
 * Created by DaChiimp on 6/12/2016.
 */
public class SaveMethods {


    static int inQ = 0;

    private static String lastSaveMsg = "";

    public static void saveFile(File f, YamlConfiguration yc) {

        inQ = inQ + 1;

        if (inQ > 1) {
            long time;
            time = inQ * 2;


            final File lf = f;

            final YamlConfiguration lyc = yc;

            Bukkit.getScheduler().scheduleSyncDelayedTask(StaffList._plugin, new Runnable() {
                @Override
                public void run() {
                    doSave(lf, lyc);
                }
            }, time);
        } else {
            doSave(f, yc);
        }


    }

    private static void doSave(File f, YamlConfiguration yc) {
        try {
            yc.save(f);
            if (!lastSaveMsg.equalsIgnoreCase("Saved file " + f)) {
                Debug.p("Saved file " + f);
            }
        } catch (IOException e) {
            Debug.p("Error saving file : " + f);
            Debug.p("Re-Queuing the file for save");
            saveFile(f, yc);
        } finally {
            inQ = inQ - 1;
        }
    }


}
