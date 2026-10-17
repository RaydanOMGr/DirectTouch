package me.andreasmelone.directtouch.mixin;

import me.andreasmelone.directtouch.DirectTouchClient;
import me.andreasmelone.directtouch.DirectTouchPlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.common.platform.Platform;

@Pseudo
@Mixin(targets = { "top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider", "top.fifthlight.touchcontroller.common.platform.PlatformProvider" })
public class PlatformProviderMixin {
    @Inject(
            method = { "loadPlatform$touchcontroller_common_platform_provider_provider", "loadPlatform" },
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            allow = 1
    )
    public void loadPlatform(
            //? if touchcontroller: >=0.3 {
            CallbackInfoReturnable<kotlin.jvm.functions.Function0<Platform>> cir
            //? } else
            //CallbackInfoReturnable<Platform> cir
    ) {
        Logger logger = LogManager.getLogger("me.andreasmelone.directtouch.mixin.PlatformProviderMixin");
        if(DirectTouchClient.isInitializedNative()) {
            logger.info("Loading DirectTouchPlatform!");
            //? if touchcontroller: >=0.3 {
            cir.setReturnValue(DirectTouchPlatform::new);
            //? } else
            //cir.setReturnValue(new DirectTouchPlatform());
        } else {
            logger.warn("DirectTouch is not initialized!");
        }
    }
}
