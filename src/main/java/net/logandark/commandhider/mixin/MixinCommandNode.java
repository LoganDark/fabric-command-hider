package net.logandark.commandhider.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.logandark.commandhider.CommandHider;
import net.logandark.commandhider.ducks.CommandNodeDuck;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CommandNode.class)
public abstract class MixinCommandNode<S> implements CommandNodeDuck {
	@Shadow(remap = false)
	public abstract String getName();

	@Unique
	protected String ancestry;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void commandHider$onInit(Command<S> command, Predicate<S> requirement, CommandNode<S> redirect, RedirectModifier<S> modifier, boolean forks, CallbackInfo ci) {
		this.ancestry = "";
	}

	@Override
	public void setAncestry(String ancestry) {
		this.ancestry = ancestry;
	}

	@Override
	public String getPath() {
		return this.ancestry + this.getName();
	}

	@Unique
	private CommandNodeDuck getDuck(CommandNode<S> parent) {
		return (CommandNodeDuck) parent;
	}

	@Unique
	private void propagateAncestry(CommandNode<S> parent, CommandNode<S> child) {
		getDuck(child).setAncestry(parent instanceof RootCommandNode ? "" : getDuck(parent).getPath() + ".");
	}

	@Unique
	private void propagateAncestryToDescendants(CommandNode<S> parent) {
		for (CommandNode<S> child : parent.getChildren()) {
			propagateAncestry(parent, child);
			propagateAncestryToDescendants(child);
		}
	}

	@Inject(method = "addChild", at = @At("HEAD"), remap = false)
	private void commandHider$onAddChild(CommandNode<S> child, CallbackInfo ci) {
		// noinspection unchecked
		CommandNode<S> parent = (CommandNode<S>) (Object) this;

		if (!(child instanceof RootCommandNode)) {
			this.propagateAncestry(parent, child);
			this.propagateAncestryToDescendants(child);
		}
	}

	@Inject(method = "canUse", at = @At("RETURN"), cancellable = true, remap = false)
	private void commandHider$onCanUse(S source, CallbackInfoReturnable<Boolean> cir) {
		if (source instanceof CommandSource commandSource) {
			cir.setReturnValue(CommandHider.canUsePath(commandSource, this.getPath(), cir.getReturnValue()));
		}
	}
}
