package me.andreasmelone.directtouch.pojav;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;

public class AndroidLibLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final AndroidLibLoader INSTANCE = new AndroidLibLoader();

    private String path = "";

    public String get(String name) {
        File file = new File(path + "/extracted_library/lib" + name + ".so");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        String pathInJar = "/natives/" + getArch() + "/lib" + name + ".so";
        extractFile(pathInJar, file);
        return file.getPath();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private static String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return switch (arch) {
            case "x86" -> "x86";
            case "amd64", "x86_64" -> "x86_64";
            case "arm", "armv7", "armv7l" -> "armeabi-v7a";
            case "aarch64", "arm64" -> "arm64-v8a";
            default -> throw new UnsupportedOperationException("Unknown architecture: " + arch);
        };
    }


    private static void extractFile(String pathInJar, File file) {
        try {
            extractFile(pathInJar, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to open file!", e);
        }
    }

    public static void extractFile(String pathInJar, OutputStream dest) {
        try(InputStream in = AndroidLibLoader.class.getResourceAsStream(pathInJar)) {
            if(in == null) {
                LOGGER.error("Failed to open {}", pathInJar);
                return;
            }
            in.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("Failed to extract library!", e);
        }
    }
}