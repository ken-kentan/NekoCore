package jp.kentan.minecraft.nekocore.command

import org.bukkit.command.CommandSender

class CommandArgument(
    private vararg val arguments: String,
    private val permission: String? = null
) {

    companion object {
        const val PLAYER = "[player]"
    }

    fun matchFirst(sender: CommandSender, arg: String) =
        (permission == null || sender.hasPermission(permission))
                && arguments.isNotEmpty()
                && arguments[0].startsWith(arg, true)

    fun get(args: Array<String>) = if (args.size <= arguments.size) arguments[args.size - 1] else null
}