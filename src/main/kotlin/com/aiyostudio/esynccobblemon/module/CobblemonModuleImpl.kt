package com.aiyostudio.esynccobblemon.module

import com.aiyostudio.esync.common.module.AbstractModule
import com.aiyostudio.esync.internal.handler.CacheHandler
import com.aiyostudio.esynccobblemon.EfficientSyncCobblemon
import com.aiyostudio.esynccobblemon.entity.CobblemonEntity
import com.cobblemon.mod.common.Cobblemon
import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.TagParser
import java.util.*

class CobblemonModuleImpl : AbstractModule<CobblemonEntity>() {
    override val uniqueKey: String = "cobblemon"

    init {
        if (EfficientSyncCobblemon.instance.config.getBoolean("depend")) {
            CacheHandler.dependModules.add(uniqueKey)
        }
    }

    override fun firstLoad(uuid: UUID, bytea: ByteArray?): Boolean {
        if (bytea == null || bytea.isEmpty()) {
            this.caches[uuid] = CobblemonEntity()
        } else {
            this.caches[uuid] = wrapper(bytea)
        }
        return true
    }

    override fun attemptLoad(uuid: UUID, bytea: ByteArray?): Boolean {
        return firstLoad(uuid, bytea)
    }

    override fun preLoad(uuid: UUID) {
        val registryAccess = requireNotNull(Cobblemon.implementation.server()?.registryAccess())
        if (EfficientSyncCobblemon.instance.config.getBoolean("option.always-clear")) {
            val partyStorage = Cobblemon.storage.getParty(uuid, registryAccess)
            val pcStorage = Cobblemon.storage.getPC(uuid, registryAccess)
            partyStorage.clearParty()
            pcStorage.clearPC()
        }
    }

    override fun apply(uuid: UUID): Boolean {
        return this.find(uuid)?.apply(uuid) ?: false
    }

    override fun toByteArray(uuid: UUID): ByteArray? {
        val buf = Unpooled.buffer()
        val registryAccess = requireNotNull(Cobblemon.implementation.server()?.registryAccess())
        // write party data
        val partyStorage = Cobblemon.storage.getParty(uuid, registryAccess)
        val partyCompound = partyStorage.saveToNBT(CompoundTag(), registryAccess)
        val partyBytea = partyCompound.toString().toByteArray(Charsets.UTF_8)
        buf.writeInt(partyBytea.size)
        buf.writeBytes(partyBytea)
        // write pc data
        val pcStorage = Cobblemon.storage.getPC(uuid, registryAccess)
        val pcCompound = pcStorage.saveToNBT(CompoundTag(), registryAccess)
        val pcBytea = pcCompound.toString().toByteArray(Charsets.UTF_8)
        buf.writeInt(pcBytea.size)
        buf.writeBytes(pcBytea)
        return buf.array()
    }

    override fun wrapper(bytea: ByteArray): CobblemonEntity {
        val entity = CobblemonEntity()
        val buf = Unpooled.wrappedBuffer(bytea)
        // read party data
        val partyByteaLength = buf.readInt()
        val partyBytea = ByteArray(partyByteaLength)
        buf.readBytes(partyBytea)
        entity.partyCompound = TagParser.parseTag(String(partyBytea, Charsets.UTF_8))
        // read pc data
        val pcByteaLength = buf.readInt()
        val pcBytea = ByteArray(pcByteaLength)
        buf.readBytes(pcBytea)
        entity.pcCompound = TagParser.parseTag(String(pcBytea, Charsets.UTF_8))
        return entity
    }
}