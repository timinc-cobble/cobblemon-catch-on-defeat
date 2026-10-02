package us.timinc.mc.cobblemon.catchondefeat.neoforge.network

import com.cobblemon.mod.common.NetworkManager
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import com.cobblemon.mod.neoforge.net.NeoForgePacketInfo
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.fml.ModList
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.HandlerThread
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat

object CatchOnDefeatNeoForgeNetworkManager : NetworkManager {
    fun registerMessages(event: RegisterPayloadHandlersEvent) {
        val modVersion = ModList.get().getModContainerById(CatchOnDefeat.modId).orElseThrow().modInfo.version.toString()
        val protocolVersion = modVersion.substringAfter("-neoforge-")

        val registrar = event.registrar(CatchOnDefeat.modId).versioned(protocolVersion)
        val netRegistrar = event.registrar(CatchOnDefeat.modId).versioned(protocolVersion).executesOn(HandlerThread.MAIN)

        val syncPackets = HashSet<ResourceLocation>()
        val asyncPackets = HashSet<ResourceLocation>()

        CatchOnDefeat.Network.s2cPayloads.map { NeoForgePacketInfo(it) }.forEach {
            val handleAsync = it.info.handler is DataRegistrySyncPacketHandler<*, *>
            when (handleAsync) {
                true -> asyncPackets += it.info.id
                false -> syncPackets += it.info.id
            }

            it.registerToClient(if (handleAsync) netRegistrar else registrar)
        }

        CatchOnDefeat.Network.c2sPayloads.map { NeoForgePacketInfo(it) }.forEach {
            it.registerToServer(registrar)
        }
    }

    override fun sendPacketToPlayer(
        player: ServerPlayer,
        packet: NetworkPacket<*>,
    ) = player.connection.send(packet)

    override fun sendToServer(packet: NetworkPacket<*>) {
        Minecraft.getInstance().connection?.send(packet)
    }
}
