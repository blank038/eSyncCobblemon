package com.aiyostudio.esynccobblemon

import com.aiyostudio.esync.common.EfficientSync
import com.aiyostudio.esynccobblemon.module.CobblemonModuleImpl
import com.aystudio.core.bukkit.plugin.AyPlugin

class EfficientSyncCobblemon : AyPlugin() {

    companion object {
        lateinit var instance: EfficientSyncCobblemon
    }

    override fun onLoad() {
        instance = this
        EfficientSync.api.registerModule("cobblemon", CobblemonModuleImpl::class.java, true)
    }

    override fun onEnable() {
        this.loadConfig()
    }

    private fun loadConfig() {
        this.saveDefaultConfig()
        this.reloadConfig()
    }
}