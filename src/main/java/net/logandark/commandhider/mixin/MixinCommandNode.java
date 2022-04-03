package net.logandark.commandhider.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.logandark.commandhider.CommandHider;
import net.logandark.commandhider.ducks.CommandNodeDuck;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(CommandNode.class)
public abstract class MixinCommandNode<S> implements CommandNodeDuck<S> {
	@Shadow(remap = false)
	@Final
	private Map<String, CommandNode<S>> children;

	@Unique
	private final Set<CommandNode<S>> parents = new HashSet<>();

	// IdentityHashMap is required because RootCommandNode does not properly
	// implement equals/hashCode. Ask me how I know.
	@Unique
	private final Map<RootCommandNode<S>, List<List<CommandNode<S>>>> ancestries = new IdentityHashMap<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void commandHider$onInit(Command<S> command, Predicate<S> requirement, CommandNode<S> redirect, RedirectModifier<S> modifier, boolean forks, CallbackInfo ci) {
		// noinspection ConstantConditions,unchecked
		if (((CommandNode<S>) (Object) this) instanceof RootCommandNode<S>) {
			List<CommandNode<S>> ancestryToRoot = new ArrayList<>();
			List<List<CommandNode<S>>> ancestriesToRoot = new ArrayList<>();
			ancestriesToRoot.add(ancestryToRoot);
			// noinspection unchecked
			ancestries.put((RootCommandNode<S>) (Object) this, ancestriesToRoot);
		}
	}

	@Override
	public Set<CommandNode<S>> getParents() {
		return parents;
	}

	@Override
	public Map<RootCommandNode<S>, List<List<CommandNode<S>>>> getAncestries() {
		return ancestries;
	}

	@Override
	public void addParent(CommandNode<S> parent) {
		parents.add(parent);
		addAncestryFromParent(parent);
		recalculateAncestriesOfDescendants();
	}

	@Unique
	private void recalculateAncestriesOfDescendants() {
		for (CommandNode<S> child : children.values()) {
			// noinspection unchecked
			CommandNodeDuck<S> duck = ((CommandNodeDuck<S>) child);
			duck.recalculateAncestriesFromParents();
		}
	}

	@Override
	public void recalculateAncestriesFromParents() {
		ancestries.clear();
		parents.forEach(this::addAncestryFromParent);
		recalculateAncestriesOfDescendants();
	}

	@Unique
	private void addAncestryFromParent(CommandNode<S> parent) {
		// noinspection unchecked
		CommandNodeDuck<S> duck = (CommandNodeDuck<S>) parent;

		for (Map.Entry<RootCommandNode<S>, List<List<CommandNode<S>>>> entry : duck.getAncestries().entrySet()) {
			RootCommandNode<S> root = entry.getKey();

			List<List<CommandNode<S>>> parentAncestriesToRoot = entry.getValue();
			List<List<CommandNode<S>>> ancestriesToRoot = null;

			for (List<CommandNode<S>> parentAncestry : parentAncestriesToRoot) {
				// Prevent infinite ancestry loops
				// noinspection unchecked
				if (parentAncestry.contains((CommandNode<S>) (Object) this)) {
					continue;
				}

				if (ancestriesToRoot == null) {
					ancestriesToRoot = ancestries.computeIfAbsent(root, k -> new ArrayList<>());
				}

				List<CommandNode<S>> newAncestryToRoot = new ArrayList<>(parentAncestry.size() + 1);
				// noinspection unchecked
				newAncestryToRoot.add((CommandNode<S>) (Object) this);
				newAncestryToRoot.addAll(parentAncestry);
				ancestriesToRoot.add(newAncestryToRoot);
			}
		}
	}

	@Inject(method = "addChild", at = @At("HEAD"), remap = false)
	private void commandHider$onAddChild(CommandNode<S> child, CallbackInfo ci) {
		// noinspection unchecked
		((CommandNodeDuck<S>) child).addParent((CommandNode<S>) (Object) this);
	}

	@Inject(method = "canUse", at = @At("RETURN"), cancellable = true, remap = false)
	private void commandHider$onCanUse(S source, CallbackInfoReturnable<Boolean> cir) {
		if (source instanceof CommandSource commandSource) {
			boolean anyTrue = false;

			for (RootCommandNode<S> root : ancestries.keySet()) {
				// noinspection unchecked
				String prefix = CommandHider.getPrefix(commandSource, (RootCommandNode<CommandSource>) root);
				if (prefix == null) continue;

				// noinspection unchecked
				switch (CommandHider.canUse(commandSource, (RootCommandNode<CommandSource>) root, (CommandNode<CommandSource>) (Object) this, prefix)) {
					case FALSE -> {
						cir.setReturnValue(false);
						return;
					}
					case TRUE -> anyTrue = true;
				}
			}

			if (anyTrue) cir.setReturnValue(true);
		}
	}
}
