package net.logandark.commandhider;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHider {
	public static final Logger LOGGER = LoggerFactory.getLogger(CommandHider.class);

	public static <S extends CommandSource> boolean canUsePath(S source, String path, boolean def) {
		return Permissions.check(source, "command." + path, def);
	}
}
