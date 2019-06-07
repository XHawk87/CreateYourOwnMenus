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
package me.xhawk87.CreateYourOwnMenus.i18n;

import me.xhawk87.LanguageAPI.ISOCode;
import me.xhawk87.LanguageAPI.PluginLanguageLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * LanguageWrapper
 * <p>
 * This can be used in exactly the same way as the Language class from the
 * LanguageAPI plugin, however it will still work (in the default language only)
 * if the LanguageAPI.jar is not installed.
 *
 * @author XHawk87
 */
public class LanguageWrapper {

    /**
     * A generalised object for holding the PluginLanguageLibrary if it exists
     */
    private Object langObj;

    /**
     * Creates a new LanguageWrapper for the LanguageAPI plugin
     *
     * @param plugin Your plugin
     */
    public LanguageWrapper(Plugin plugin, String code) {
        if (Bukkit.getPluginManager().getPlugin("LanguageAPI") != null) {
            plugin.saveResource("languages/lang-eng.yml", false);
            langObj = new PluginLanguageLibrary(plugin, ISOCode.findMatch(code));
        }
    }

    /**
     * A wrapper for the PluginLanguageLibrary.get method. It checks if the
     * LanguageAPI is installed, and if it is not, returns the default string
     * instead.
     *
     * @param forWhom  The intended recipient to translate for
     * @param key      The template key
     * @param template The default template for the plugin
     * @param params   The parameters to be inserted
     * @return The formatted string
     */
    public String get(CommandSender forWhom, String key, String template, Object... params) {
        if (langObj != null) {
            PluginLanguageLibrary language = (PluginLanguageLibrary) langObj;
            return language.get(forWhom, key, template, params);
        } else {
            return compile(template, params);
        }
    }

    /**
     * A wrapper for the PluginLanguageLibrary.get method. It checks if the
     * LanguageAPI is installed, and if it is not, returns the default string
     * instead.
     *
     * @param preferredLocale The preferred language ISO code to translate to
     * @param key             The template key
     * @param template        The default template for the plugin
     * @param params          The parameters to be inserted
     * @return The formatted string
     */
    public String get(ISOCode preferredLocale, String key, String template, Object... params) {
        if (langObj != null) {
            PluginLanguageLibrary language = (PluginLanguageLibrary) langObj;
            return language.get(preferredLocale, key, template, params);
        } else {
            return compile(template, params);
        }
    }

    /**
     * Taken directly from the PluginLanguageLibrary class of the LanguageAPI
     * plugin.
     * <p>
     * Inserts the given parameters into the template at the correct locations
     * and returns the formatted string
     *
     * @param template The string template
     * @param params   The dynamic data to be inserted
     * @return The formatted string
     * @throws IllegalArgumentException If template tries to reference a
     *                                  parameter index that does not exist
     */
    private static String compile(String template, Object[] params) throws IllegalArgumentException {
        if (params.length == 0) {
            return template; // For the sake of efficiency, don't parse templates with no dynamic data
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            if (c == '{') {
                try {
                    int endIndex = template.indexOf('}', i);
                    if (endIndex != -1) {
                        int param = Integer.parseInt(template.substring(i + 1, endIndex));
                        if (param >= params.length) {
                            throw new IllegalArgumentException();
                        }
                        sb.append(params[param].toString());
                        i = endIndex;
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    // then read it as it is
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
