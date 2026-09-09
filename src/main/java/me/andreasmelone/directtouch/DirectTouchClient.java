package me.andreasmelone.directtouch;

import com.mojang.logging.LogUtils;
import me.andreasmelone.directtouch.pojav.AndroidLibLoader;
import me.andreasmelone.directtouch.pojav.DirectTouchAndroidNative;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.andreasmelone.directtouch.pojav.DirectTouchAndroidNative.*;

public class DirectTouchClient implements ClientModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initializedNative = false;

    static final Queue<ProxyMessage> PROXY_MESSAGE_QUEUE = new LinkedBlockingQueue<>();

    public boolean isPojav = false;

    @Override
    public void onInitializeClient() {
        String packageName = null;
        String userId = null;

        Pattern pattern = Pattern.compile("^/data/user/(\\d+)/([^/]+)/.*");
        for (String path : System.getProperty("java.library.path").split(File.pathSeparator)) {
            Matcher m = pattern.matcher(path);
            if (!m.find()) continue;
            userId = m.group(1);
            packageName = m.group(2);
            break;
        }

        if(packageName != null && userId != null) {
            isPojav = true;
        }

        if(isPojav) {
            AndroidLibLoader.INSTANCE.setPath("/data/user/" + userId + "/" + packageName + "/cache");
            ByteArrayOutputStream dex = new ByteArrayOutputStream();
            AndroidLibLoader.extractFile("/dex/classes.dex", dex);
            DirectTouchAndroidNative.setDexData(dex.toByteArray());
            int result = DirectTouchAndroidNative.init(NATIVE_PATH);
            switch (result) {
                case INIT_SUCCESS -> initializedNative = true;
                case INIT_DEX_NOT_INITIALIZED -> LOGGER.error("Dex did not initialize, mod will not function");
                case INIT_DVM_NOT_FOUND -> {
                    LOGGER.error("Dalvik VM cannot be found, are we running on Android?");
                    LOGGER.error("Mod will not function");
                }
                case INIT_METHOD_NOT_INITIALIZED -> LOGGER.error("JNI could not initialized methods, mod will not function");
                default -> LOGGER.error("An error occurred, mod will not function 0x{}", Integer.toHexString(result).toUpperCase()); // also covers INIT_GENERIC_ERROR
            }
        }

        if(initializedNative) {
            Runtime.getRuntime().addShutdownHook(new Thread(DirectTouchAndroidNative::close, "DirectTouch Shutdown Hook"));
        }
    }

    public static boolean isInitializedNative() {
        return initializedNative;
    }

    public static void receiveMessage(ProxyMessage message) {
        PROXY_MESSAGE_QUEUE.add(message);
    }
}
