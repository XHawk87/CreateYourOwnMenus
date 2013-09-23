/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author XHawk87
 */
public class FileUpdater {

    private static final long DEFAULT_INTERVAL = 1000;
    private final File file;
    private long saveCount = 0;
    private long lastSave = 0;
    private long interval;
    private BukkitTask saveTask = null;

    public FileUpdater(File file) {
        this(file, DEFAULT_INTERVAL);
    }

    public FileUpdater(File file, long interval) {
        this.file = file;
        this.interval = interval;
    }

    /**
     * Overwrites the file with the given text record
     *
     * This is performed asynchronously, waiting at least the specified interval
     * between saves, and avoiding saving when there is new data already ready
     * to save
     *
     * @param plugin The plugin that is saving the file
     * @param record The text that should replace the existing file's contents
     */
    public void save(final Plugin plugin, final String record) {
        saveCount++;
        final long currentSave = saveCount;
        if (saveTask != null) {
            saveTask.cancel();
        }
        int delay = (int) ((lastSave + interval - System.currentTimeMillis()) / 50);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentSave == saveCount) { // enforce save order
                    synchronized (file) {
                        if (currentSave == saveCount) { // enforce save order
                            try (FileWriter out = new FileWriter(file)) {
                                out.write(record);
                            } catch (IOException ex) {
                                plugin.getLogger().log(Level.SEVERE, "Could not save: " + file.getPath(), ex);
                            }
                            lastSave = System.currentTimeMillis();
                        }
                    }
                }
            }
        };
        if (delay > 1) {
            saveTask = task.runTaskLaterAsynchronously(plugin, delay);
        } else {
            saveTask = task.runTaskAsynchronously(plugin);
        }
    }
}
