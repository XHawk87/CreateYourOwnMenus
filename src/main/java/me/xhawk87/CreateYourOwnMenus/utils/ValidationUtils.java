/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XHawk87
 */
public class ValidationUtils {

    public static List<Integer> getSlotRange(String slotsString) throws IllegalArgumentException {
        List<Integer> slots = new ArrayList<>();
        String[] slotStrings = slotsString.split(",");
        for (String slotString : slotStrings) {
            if (slotString.contains("-")) {
                String[] fromToStrings = slotString.split("-");
                if (fromToStrings.length != 2) {
                    throw new IllegalArgumentException("Error in menu script line (expected slot range from-to): " + slotString);
                }
                int fromSlot;
                try {
                    fromSlot = Integer.parseInt(fromToStrings[0]);
                    if (fromSlot < 0) {
                        throw new IllegalArgumentException("Error in menu script line (expected positive slot range from number): " + fromSlot);
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Error in menu script line (expected slot range from as whole number): " + fromToStrings[0]);
                }
                int toSlot;
                try {
                    toSlot = Integer.parseInt(fromToStrings[1]);
                    if (fromSlot > toSlot) {
                        throw new IllegalArgumentException("Error in menu script line (expected slot range to number higher than from number " + fromSlot + "): " + toSlot);
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Error in menu script line (expected slot range to as whole number): " + fromToStrings[1]);
                }
                for (int i = fromSlot; i <= toSlot; i++) {
                    slots.add(i);
                }
            } else {
                try {
                    int slotNumber = Integer.parseInt(slotString);
                    if (slotNumber < 0) {
                        throw new IllegalArgumentException("Error in menu script line (expected positive slot number): " + slotNumber);
                    }
                    slots.add(slotNumber);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Error in menu script line (expected whole number for slot): " + slotString);
                }
            }
        }
        return slots;
    }
}
