package net.logandark.commandhider;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHider {
	public static final Logger LOGGER = LoggerFactory.getLogger(CommandHider.class);

	public static <S extends CommandSource> boolean canUsePath(S source, String path, boolean def) {
		String permission = "commands." + path;

		for (int i = permission.length(); i >= 0; i--) {
			if (i == permission.length() || permission.charAt(i) == '.') {
				permission = permission.substring(0, i);

				TriState value = Permissions.getPermissionValue(source, permission);
				if (value.getBoxed() != null) return value.get();
			}
		}

		return def;
	}
}
