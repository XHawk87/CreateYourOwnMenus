/*
 * Copyright (C) 2013-2019 XHawk87
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
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
        final List<String> lines;

        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            String contents = Charset.forName("UTF8").decode(ByteBuffer.wrap(encoded)).toString();
            lines = Arrays.asList(contents.replace('&', ChatColor.COLOR_CHAR)
                    .replace("\r\n", "\n").split("\n"));
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
