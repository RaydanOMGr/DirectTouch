package me.andreasmelone.directtouch.mixin;

import com.mojang.logging.LogUtils;
import me.andreasmelone.directtouch.DirectTouchClient;
import me.andreasmelone.directtouch.DirectTouchPlatform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.common.platform.Platform;
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider;

@Mixin(PlatformProvider.class)
public class PlatformProviderMixin {
    @Inject(
            method = "loadPlatform",
            at = @At("HEAD"),
            cancellable = true
    )
    public void loadPlatform(CallbackInfoReturnable<kotlin.jvm.functions.Function0<Platform>> cir) {
        if(DirectTouchClient.isInitializedNative()) {
            LogUtils.getLogger().info("Loading DirectTouchPlatform!");
            cir.setReturnValue(DirectTouchPlatform::new);
        }
    }
}
