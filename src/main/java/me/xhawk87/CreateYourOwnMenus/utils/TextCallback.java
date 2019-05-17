/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.util.List;

/**
 * @author XHawk87
 */
public interface TextCallback {

    void onLoad(List<String> lines);

    void fail(Exception ex);
}
