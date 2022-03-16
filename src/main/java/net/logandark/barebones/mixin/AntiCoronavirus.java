package net.logandark.barebones.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashTextResourceSupplier.class)
public abstract class AntiCoronavirus {
	@Inject(
		at = @At("HEAD"),
		cancellable = true,
		method = "get()Ljava/lang/String;"
	)
	private void onGet(CallbackInfoReturnable<String> cir) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			cir.setReturnValue("");
		}
	}
}
