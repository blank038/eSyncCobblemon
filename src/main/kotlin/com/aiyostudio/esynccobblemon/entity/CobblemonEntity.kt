package com.aiyostudio.esynccobblemon.entity

import com.aiyostudio.esync.common.module.IEntity
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.nbt.CompoundTag
import java.util.*


class CobblemonEntity : IEntity {
    lateinit var partyCompound: CompoundTag
    lateinit var pcCompound: CompoundTag

    override fun apply(player: Any): Boolean {
        if (player !is UUID) {
            return false
        }
        val registryAccess = Cobblemon.implementation.server()?.registryAccess() ?: return false
        val partyStorage = Cobblemon.storage.getParty(player, registryAccess)
        val pcStorage = Cobblemon.storage.getPC(player, registryAccess)

        partyStorage.loadFromNBT(partyCompound, registryAccess)
        pcStorage.loadFromNBT(pcCompound, registryAccess)

        partyStorage.initialize()
        pcStorage.initialize()


        player.getPlayer()?.let(Cobblemon.storage::onPlayerDataSync)
        return true
    }
}