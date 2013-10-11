/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author XHawk87
 */
public class TextFileLoader implements Runnable {

    private Plugin plugin;
    private File file;
    private TextCallback callback;

    public TextFileLoader(Plugin plugin, File file, TextCallback callback) {
        this.plugin = plugin;
        this.file = file;
        this.callback = callback;
    }

    public void load() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
    }

    @Override
    public void run() {
        if (!file.exists()) {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    callback.fail(new FileNotFoundException(file.getPath() + " does not exist"));
                }
            });
        }
        final List<String> lines = new ArrayList<>();

        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            String contents = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
            lines.addAll(Arrays.asList(contents.replace('&', ChatColor.COLOR_CHAR).replace("\r\n", "\n").split("\n")));
        } catch (final IOException ex) {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    callback.fail(ex);
                }
            });
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                callback.onLoad(lines);
            }
        });
    }
}
