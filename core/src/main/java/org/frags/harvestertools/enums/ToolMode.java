package org.frags.harvestertools.enums;

public enum ToolMode {

    Collect, AutoSell;


    public static ToolMode getToolMode(String mode) {
        if (mode.equalsIgnoreCase("collect")) {
            return Collect;
        } else if (mode.equalsIgnoreCase("autosell")) {
            return AutoSell;
        }
        return null;
    }

    public static String toString(ToolMode mode) {
        return switch (mode) {
            case Collect -> "Collect";
            case AutoSell -> "AutoSell";
            default -> null;
        };
    }
}
