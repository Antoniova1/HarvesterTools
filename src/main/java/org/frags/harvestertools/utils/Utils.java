package org.frags.harvestertools.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class Utils {

    public static String formatNumber(BigDecimal number) {
        String[] suffixes = {"", "K", "M", "B", "T", "Q", "Qi", "S", "Sp", "O", "N", "D"};
        BigDecimal divisor = new BigDecimal(1000);
        int suffixIndex = 0;

        while (number.compareTo(divisor) >= 0 && suffixIndex < suffixes.length - 1) {
            number = number.divide(divisor, 2, RoundingMode.HALF_UP);
            suffixIndex++;
        }

        return String.format("%.2f%s", number, suffixes[suffixIndex]);
    }

    public static ItemStack getHead(final String base64) {
        Material type = Material.PLAYER_HEAD;
        final ItemStack head = new ItemStack(type, 1);
        @SuppressWarnings("TypeMayBeWeakened") final SkullMeta meta = (SkullMeta) head.getItemMeta();
        if(McVersion.current().isAtLeast(1,18,1)) {
            setBase64ToSkullMeta(base64, meta);
        } else {
            setBase64ToSkullMeta_Old(base64, meta);
        }
        head.setItemMeta(meta);
        return head;
    }

    private static final UUID RANDOM_UUID = UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4"); // We reuse the same "random" UUID all the time


    private static PlayerProfile getProfileBase64(String base64) {
        PlayerProfile profile = Bukkit.createPlayerProfile(RANDOM_UUID); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = getUrlFromBase64(base64);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject); // Set the skin of the player profile to the URL
        profile.setTextures(textures); // Set the textures back to the profile
        return profile;
    }

    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        // We simply remove the "beginning" and "ending" part of the JSON, so we're left with only the URL. You could use a proper
        // JSON parser for this, but that's not worth it. The String will always start exactly with this stuff anyway
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    private static void setBase64ToSkullMeta(String base64, SkullMeta meta) {
        PlayerProfile profile = getProfileBase64(base64);
        meta.setOwnerProfile(profile);
    }

    private static void setBase64ToSkullMeta_Old(@NotNull String base64, SkullMeta meta) {
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", base64));
        final Field profileField;
        assert meta != null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, gameProfile);

        } catch (final NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
