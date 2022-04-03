package net.logandark.commandhider;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.logandark.commandhider.ducks.CommandNodeDuck;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CommandHider {
	/**
	 * An easy mixin target if other mods want to add support for their
	 * {@link CommandDispatcher}s to Command Hider. Basically, if you want
	 * Command Hider to do permission checks for your commands, you can mix into
	 * this method to make it return a string when called with one of your
	 * {@link RootCommandNode}s, like it does with Minecraft's.
	 * <p>
	 * It's not feasible to try to control the prefix separately depending on
	 * which command is being executed, due to how much mixing and muddling mods
	 * can do. Instead, it's recommended to choose a generic prefix like
	 * {@code command} for all commands run through the server
	 * {@link CommandDispatcher} - which is the default.
	 *
	 * @param source The {@link CommandSource} to get the prefix for.
	 * @param root   The {@link RootCommandNode} which is being checked.
	 * @param <S>    The type of {@link CommandSource} executing the command.
	 * @return Either a {@link String} containing the prefix to use (may be
	 * empty), or {@code null} indicating that this root should not be checked.
	 */
	@Nullable
	public static <S extends CommandSource> String getPrefix(S source, RootCommandNode<S> root) {
		if (source instanceof ServerCommandSource serverCommandSource && root == serverCommandSource.getServer().getCommandManager().getDispatcher().getRoot()) {
			return "command";
		}

		return null;
	}

	/**
	 * Checks whether the provided {@link CommandSource} is explicitly allowed
	 * or disallowed to use the provided {@link CommandNode} relative to the
	 * provided {@link RootCommandNode}. Permissions will be checked using the
	 * provided {@code prefix}, with proper inheritance.
	 * <p>
	 * This checks all ancestries of the provided {@link CommandNode} that lead
	 * to the given {@link RootCommandNode}, but not the ancestries of any other
	 * {@link RootCommandNode}s.
	 * <p>
	 * I don't imagine this method being a fun mixin target; please open an
	 * issue if you'd like to do something specific.
	 *
	 * @param source The {@link CommandSource} whose permissions to check.
	 * @param root   The {@link RootCommandNode} to check relative to.
	 * @param node   The {@link CommandNode} to check.
	 * @param prefix The prefix to add (i.e. {@code command}). This prefix may
	 *               be empty, in which case no prefixing will be performed.
	 * @param <S>    The type of {@link CommandSource} executing the command.
	 * @return The {@link TriState} result of the permission check.
	 * {@link TriState#FALSE} will be returned if <b>any</b> of the node's
	 * ancestries report a {@link TriState#FALSE} result; otherwise,
	 * {@link TriState#TRUE} will be reported if <b>any</b> of the node's
	 * ancestries report a {@link TriState#TRUE} result; otherwise,
	 * {@link TriState#DEFAULT} will be returned.
	 */
	public static <S extends CommandSource> TriState canUse(S source, RootCommandNode<S> root, CommandNode<S> node, String prefix) {
		// noinspection unchecked
		List<List<CommandNode<S>>> ancestries = ((CommandNodeDuck<S>) node).getAncestries().get(root);
		if (ancestries == null) return TriState.DEFAULT;

		boolean anyTrue = false;

		for (List<CommandNode<S>> ancestry : ancestries) {
			List<String> permissions = new ArrayList<>();
			if (!prefix.isEmpty()) permissions.add(prefix);

			StringBuilder sb = new StringBuilder(prefix);
			ListIterator<CommandNode<S>> ancestryIter = ancestry.listIterator(ancestry.size());

			while (ancestryIter.hasPrevious()) {
				CommandNode<S> ancestor = ancestryIter.previous();
				if (!sb.isEmpty()) sb.append('.');
				sb.append(ancestor.getName());
				permissions.add(sb.toString());
			}

			TriState result = TriState.DEFAULT;
			ListIterator<String> permissionIter = permissions.listIterator(permissions.size());

			while (permissionIter.hasPrevious()) {
				result = Permissions.getPermissionValue(source, permissionIter.previous());
				if (result != TriState.DEFAULT) break;
			}

			switch (result) {
				case FALSE -> {
					return TriState.FALSE;
				}

				case TRUE -> anyTrue = true;
			}
		}

		return anyTrue ? TriState.TRUE : TriState.DEFAULT;
	}
}
