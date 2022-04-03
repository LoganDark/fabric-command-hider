package net.logandark.commandhider.ducks;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface used to access all the custom fields and methods added to
 * {@link CommandNode}s by Command Hider. Any {@link CommandNode} can be casted
 * to this interface.
 *
 * @param <S> The type of {@link CommandSource} executing the command. May not
 *            actually be a {@link CommandSource}, but in practice it probably
 *            will be.
 */
public interface CommandNodeDuck<S> {
	/**
	 * @return All the parents which contain this {@link CommandNode} as a
	 * child. There may be any number of parents, which aren't guaranteed to be
	 * descendants of the same {@link RootCommandNode}.
	 * <p>
	 * <b>This set should never be mutated.</b> Use exclusively the
	 * {@link #addParent(CommandNode)} method if you really need to do something
	 * special - but generally you should never need to use it as Command Hider
	 * already mixes into {@link CommandNode#addChild(CommandNode)}.
	 */
	Set<CommandNode<S>> getParents();

	/**
	 * @return For each {@link RootCommandNode}, all the ancestries that this
	 * {@link CommandNode} has to that root. Each ancestry list always has this
	 * node first, then its parent, grandparent, great-grandparent and so on.
	 * The final {@link RootCommandNode} is not included.
	 * <p>
	 * There may be any number of ancestries that lead to any given root and
	 * there may be ancestries to any number of roots, due to the fact that each
	 * child is not limited to only having one parent, and the same goes for
	 * each parent.
	 * <p>
	 * <b>This map and the lists it contains should never be mutated.</b> Use
	 * exclusively the {@link #addParent(CommandNode)} and
	 * {@link #recalculateAncestriesFromParents()} methods if you really need to
	 * do something special - generally Command Hider does everything for you.
	 */
	Map<RootCommandNode<S>, List<List<CommandNode<S>>>> getAncestries();

	/**
	 * Notifies this {@link CommandNode} that a parent has been added, allowing
	 * it to partially recalculate the ancestry. Do not call this function
	 * manually, {@link CommandNode#addChild(CommandNode)} already does so.
	 *
	 * @param parent The parent that now contains this {@link CommandNode}.
	 */
	void addParent(CommandNode<S> parent);

	/**
	 * Notifies this {@link CommandNode} that some ancestry change up the tree
	 * has happened, requiring it to completely rebuild its ancestries and the
	 * ancestries of all its descendants. Automatically called by
	 * {@link #addParent(CommandNode)} (and therefore
	 * {@link CommandNode#addChild(CommandNode)}), so you should not use this.
	 */
	void recalculateAncestriesFromParents();
}
