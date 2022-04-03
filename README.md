# Command Hider

Command Hider is a Fabric mod that adds permission overrides to **all commands,
even modded ones**. This means that you can now block players (even ops!) from
using certain commands, or even allow players to use commands they wouldn't
normally have access to.

Please note that you will need a permission manager such as [LuckPerms](
https://luckperms.net) installed in order to use this mod. Any permission
manager that supports the [Fabric Permissions API](
https://github.com/lucko/fabric-permissions-api) should work.

## Warning

This mod relies on [the **next** version of Fabric Loader](
https://github.com/FabricMC/fabric-loader/pull/630) to be able to mix into
Brigadier, and therefore will not work on real servers until it is fully
released. The mod itself works, but the loader is not ready yet. Installing this
mod on a loader with no support will simply have no effect. 0.13.4 or 0.14.0
will come with support for this out of the box.

When the changes are finally released, this section will be replaced by another
warning that Command Hider only works on very recent versions of Fabric Loader.
For now, it only works on an **unreleased** version.

## Usage

By default, this mod does nothing visible, since all commands default to vanilla
behavior. However, it is already doing its permission checks in the background,
so you can modify permissions to see the changes apply instantly.

Due to the fact that the entire command tree is checked every time a player
joins, your permission manager (i.e. LuckPerms) should already have a list of
every permission that corresponds to every command (and its arguments) on the
server. All permissions are of the format `command.<name...>`. So, for example,
the `/kill` command is `command.kill`, and `/kill @a` is `command.kill.targets`.

**DO NOT USE THE WILDCARD PERMISSION**

I repeat, **DO NOT USE THE WILDCARD**. It may be tempting, but this mod already
includes a useful inheritance model that can be used to allow or deny subtrees
of commands without resorting to wildcards.

The reasoning for this is that wildcards do not apply to a permission's
immediate children, but to **all descendants**. That means this set of
permissions will not do what you want:

- `command.*` denied
- `command.me` allowed

`/me` has an argument called `action` that is actually blocked by `command.*`!
Since `/me` cannot be used without any arguments, that doesn't actually do what
you want. Instead, do this:

- `command` denied (no wildcard!)
- `command.me` allowed

This will exhibit the correct behavior of allowing you to use all of `/me`, but
not anything else. That's because, since `command.me.action` is unset, Command
Hider will keep checking upwards until it finds one (`command.me`) that is
explicitly set to something (`true` in this case). Everything else will find the
root permission (`command`) set to `false`, and therefore not be allowed.
