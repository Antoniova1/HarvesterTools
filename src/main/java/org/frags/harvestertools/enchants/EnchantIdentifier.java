package org.frags.harvestertools.enchants;

import org.frags.harvestertools.HarvesterTools;
import org.frags.harvestertools.enums.Tools;

import java.util.Objects;

public class EnchantIdentifier {
    private final Tools tool;
    private final String key;

    public EnchantIdentifier(Tools tool, String key) {
        this.tool = tool;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Tools getTool() {
        return tool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnchantIdentifier that = (EnchantIdentifier) o;
        return tool == that.tool && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tool, key);
    }
}
