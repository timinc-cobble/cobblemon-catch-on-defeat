package us.timinc.mc.cobblemon.catchondefeat.network

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent
import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.RenderablePokemon
import net.minecraft.client.Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.Holders.JOIN_CONFIRM
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.TranslationComponents.wasReleased
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.config
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.debugger
import us.timinc.mc.cobblemon.catchondefeat.handler.AttemptJoinOnDefeatHandler
import us.timinc.mc.cobblemon.catchondefeat.client.screen.ConfirmJoinScreen
import us.timinc.mc.cobblemon.timcore.Holder
import java.util.*

object JoinConfirmReceipt {
    @JvmRecord
    data class Packet(
        val uuid: UUID,
        val name: Component,
        val renderable: RenderablePokemon,
        val countdown: Int?,
        override val id: ResourceLocation = ID,
    ): NetworkPacket<Packet> {

        companion object {
            val ID = CatchOnDefeat.modResource("net.packet")

            fun decode(buffer: RegistryFriendlyByteBuf) = Packet(
                ByteBufCodecs.STRING_UTF8.decode(buffer).let { UUID.fromString(it) },
                ByteBufCodecs.STRING_UTF8.decode(buffer).let { Component.translatable(it) },
                RenderablePokemon.loadFromBuffer(buffer),
                ByteBufCodecs.optional(ByteBufCodecs.INT).decode(buffer).orElse(null)
            )
        }

        override fun encode(buffer: RegistryFriendlyByteBuf) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, uuid.toString())
            ByteBufCodecs.STRING_UTF8.encode(buffer, name.string)
            renderable.saveToBuffer(buffer)
            ByteBufCodecs.optional(ByteBufCodecs.INT).encode(buffer, Optional.ofNullable(countdown))
        }

        fun accept() = Response(uuid, true).sendToServer()

        fun reject() = Response(uuid, false).sendToServer()
    }

    @JvmRecord
    data class Response(
        val uuid: UUID,
        val accepted: Boolean,
        override val id: ResourceLocation = ID, // last arg to gracefully omit it when constructing new instance
    ): NetworkPacket<Response> {

        companion object {
            val ID = CatchOnDefeat.modResource("net.response")

            fun decode(buffer: RegistryFriendlyByteBuf) = Response(
                ByteBufCodecs.STRING_UTF8.decode(buffer).let { UUID.fromString(it) },
                ByteBufCodecs.BOOL.decode(buffer)
            )
        }

        override fun encode(buffer: RegistryFriendlyByteBuf) {
            ByteBufCodecs.STRING_UTF8.encode(buffer, uuid.toString())
            ByteBufCodecs.BOOL.encode(buffer, accepted)
        }
    }

    class Data(
        val pokemon: Pokemon,
    ) : Holder.ReceiptPacketMaker<Packet> {
        override fun toPacket(id: UUID) = Packet(
            id,
            pokemon.getDisplayName(),
            pokemon.asRenderablePokemon(),
            if (!config.enableCountdown) null else config.countdownSeconds
        )
    }

    object HandlePacket: ClientNetworkPacketHandler<Packet> {
        override fun handle(
            packet: Packet,
            client: Minecraft
        ) {
            if (config.alwaysAcceptJoin) {
                packet.accept()
                return
            }
            client.setScreen(ConfirmJoinScreen(packet))
        }

    }

    object HandleResponse: ServerNetworkPacketHandler<Response> {
        override fun handle(
            packet: Response,
            server: MinecraftServer,
            player: ServerPlayer
        ) {
            try {
                val receipt = JOIN_CONFIRM.pullReceipt(packet.uuid, player)
                if (!packet.accepted) {
                    receipt.player.sendSystemMessage(wasReleased(receipt.data.pokemon.getDisplayName()))
                    if (config.rejectsCountAsRelease) {
                        AttemptJoinOnDefeatHandler.finishJoin(receipt.player, receipt.data.pokemon, true)

                        val storage = receipt.data.pokemon.storeCoordinates.get()?.store ?: return
                        val pokemon = receipt.data.pokemon
                        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.postThen(
                            event = ReleasePokemonEvent.Pre(receipt.player, pokemon, storage),
                            ifSucceeded = {
                                storage.remove(pokemon)
                                CobblemonEvents.POKEMON_RELEASED_EVENT_POST.post(
                                    ReleasePokemonEvent.Post(
                                        receipt.player,
                                        pokemon,
                                        storage
                                    )
                                )
                            }
                        )
                    }
                    return
                }
                AttemptJoinOnDefeatHandler.finishJoin(receipt.player, receipt.data.pokemon)
            } catch (e: Error) {
                debugger.debug(e.message ?: "An error occurred while handling JoinConfirmReceipt on server.", true)
            }
        }
    }
}
