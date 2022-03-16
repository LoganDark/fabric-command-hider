package net.logandark.commandhider

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
object CommandHider : ModInitializer {
	private val logger: Logger = LoggerFactory.getLogger(CommandHider.javaClass)

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}
