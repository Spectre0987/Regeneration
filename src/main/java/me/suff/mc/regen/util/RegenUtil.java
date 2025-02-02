package me.suff.mc.regen.util;

import me.suff.mc.regen.Regeneration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Random;

import static me.suff.mc.regen.Regeneration.GSON;

public class RegenUtil {

    public static ITag.INamedTag< Block > BANNED_BLOCKS = makeBlock("weeping_angels", "angel_proof");
    public static ITag.INamedTag< Block > ARS = makeBlock("tardis", "ars");
    public static ITag.INamedTag< Item > TIMELORD_CURRENCY = makeItem(RConstants.MODID, "timelord_currency");

    public static Random RAND = new Random();
    public static String[] USERNAMES = new String[]{};

    public static ITag.INamedTag< Block > makeBlock(String domain, String path) {
        return BlockTags.bind(new ResourceLocation(domain, path).toString());
    }

    public static ITag.INamedTag< Item > makeItem(String domain, String path) {
        return ItemTags.bind(new ResourceLocation(domain, path).toString());
    }

    public static double round(float value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

    public static void setupNames() {
        if (USERNAMES.length == 0) {
            try {

                ResourceLocation resourceLocation = new ResourceLocation(RConstants.MODID, "names.json");

                InputStream stream = ServerLifecycleHooks.getCurrentServer().getDataPackRegistries().getResourceManager().getResource(resourceLocation).getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                stream.close();
                //
                String[] names = GSON.fromJson(sb.toString(), String[].class);

                if (names != null) {
                    USERNAMES = names;
                }

            } catch (IOException e) {
                Regeneration.LOG.catching(e);
            }

        }

    }


    public static float randFloat(float min, float max) {
        return RAND.nextFloat() * (max - min) + min;
    }

    public static boolean doesHaveInternet() {
        try {
            Socket socket = new Socket("www.google.com", 80);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getJsonFromURL(String URL) {
        java.net.URL url = null;
        try {
            url = new URL(URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            return bufferedReaderToString(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bufferedReaderToString(BufferedReader e) {
        StringBuilder builder = new StringBuilder();
        String aux = "";

        try {
            while ((aux = e.readLine()) != null) {
                builder.append(aux);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return builder.toString();
    }

    public static byte[] fileToBytes(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }


    public static String colorToHex(Color color) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffff));
        while (hex.length() < 6) {
            hex.insert(0, "0");
        }
        return "#" + hex;
    }

    public interface IEnum< E extends Enum< E > > {
        int ordinal();

        default E next() {
            E[] ies = this.getAllValues();
            return this.ordinal() != ies.length - 1 ? ies[this.ordinal() + 1] : null;
        }

        default E previous() {
            return this.ordinal() != 0 ? this.getAllValues()[this.ordinal() - 1] : null;
        }

        @SuppressWarnings("unchecked")
        default E[] getAllValues() {
            IEnum[] ies = this.getClass().getEnumConstants();
            return (E[]) ies;
        }
    }


}
