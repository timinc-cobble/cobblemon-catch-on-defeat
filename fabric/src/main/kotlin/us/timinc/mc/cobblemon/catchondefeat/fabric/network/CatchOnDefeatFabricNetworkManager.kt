package us.timinc.mc.cobblemon.catchondefeat.fabric.network

import com.cobblemon.mod.common.NetworkManager
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.fabric.net.FabricPacketInfo
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat

object CatchOnDefeatFabricNetworkManager : NetworkManager {
    fun registerMessages() {
        CatchOnDefeat.Network.s2cPayloads.map { FabricPacketInfo(it) }.forEach { it.registerPacket(client = true) }
        CatchOnDefeat.Network.c2sPayloads.map { FabricPacketInfo(it) }.forEach { it.registerPacket(client = false) }
    }

    fun registerClientHandlers() = CatchOnDefeat.Network.s2cPayloads.map { FabricPacketInfo(it) }.forEach { it.registerClientHandler() }

    fun registerServerHandlers() = CatchOnDefeat.Network.c2sPayloads.map { FabricPacketInfo(it) }.forEach { it.registerServerHandler() }

    override fun sendPacketToPlayer(player: ServerPlayer, packet: NetworkPacket<*>) = ServerPlayNetworking.send(player, packet)

    override fun sendToServer(packet: NetworkPacket<*>) = ClientPlayNetworking.send(packet)
}
