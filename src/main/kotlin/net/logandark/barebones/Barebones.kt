package net.logandark.barebones

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
object Barebones : ModInitializer {
	private val logger: Logger = LoggerFactory.getLogger(Barebones.javaClass)

	override fun onInitialize() {
		logger.info("Hello Fabric world!")
	}
}
