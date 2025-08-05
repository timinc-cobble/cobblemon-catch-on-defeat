package us.timinc.mc.cobblemon.catchondefeat

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import us.timinc.mc.cobblemon.catchondefeat.event.JoinDefeatEvent
import us.timinc.mc.cobblemon.catchondefeat.handler.AttemptCancelPokeballHit
import us.timinc.mc.cobblemon.catchondefeat.handler.AttemptJoinOnDefeatHandler
import us.timinc.mc.cobblemon.catchondefeat.network.JoinConfirmReceipt
import us.timinc.mc.cobblemon.timcore.*

const val MOD_ID: String = "catch_on_defeat"

object CatchOnDefeat : AbstractMod<CatchOnDefeat.Config>(MOD_ID, Config::class.java) {
    class Config : AbstractConfig() {
        val heal: Boolean = true
        val preventRegularCapture: Boolean = true
        val thereCanOnlyBeOnePlayerInBattle: Boolean = true
        val everybodysCaughtThisWay: Boolean = true
        val countsAsCapture: Boolean = true
        val alwaysAcceptJoin: Boolean = false
        val rejectsCountAsRelease: Boolean = true
        val defaultJoinChance: Float = 100F
    }

    object CustomProperties {
        val CATCH_ON_DEFEAT = registerCustomPokemonProperty(CustomBooleanProperty(listOf("catch_on_defeat")))
        val MUST_BE_SOLOED = registerCustomPokemonProperty(CustomBooleanProperty(listOf("must_be_soloed")))
        val DEFEAT_JOIN_CHANCE = registerCustomPokemonProperty(CustomFloatProperty(listOf("defeat_join_chance")))
        val PREVENT_REGULAR_CAPTURE = registerCustomPokemonProperty(CustomBooleanProperty(listOf("prevent_regular_capture")))
    }

    object TranslationComponents {
        fun cantCatch(pokemon: Pokemon): MutableComponent =
            Component.translatable("catch_on_defeat.feedback.cant_catch", pokemon.getDisplayName())

        fun thereCanOnlyBeOne(pokemon: Pokemon): MutableComponent =
            Component.translatable("catch_on_defeat.feedback.there_can_only_be_one", pokemon.getDisplayName())

        fun ranAway(pokemon: Pokemon): MutableComponent =
            Component.translatable("catch_on_defeat.feedback.ran_away", pokemon.getDisplayName())

        fun joinedTeam(pokemon: Pokemon): MutableComponent =
            Component.translatable("catch_on_defeat.feedback.joined_team", pokemon.getDisplayName())

        fun wouldLikeToJoinTeam(name: Component): MutableComponent =
            Component.translatable("catch_on_defeat.ui.would_like_to_join", name)

        fun wasReleased(name: Component): MutableComponent =
            Component.translatable("catch_on_defeat.feedback.released", name)
    }

    object Events {
        @JvmField
        val JOIN_DEFEAT_PRE = CancelableObservable<JoinDefeatEvent.Pre>()

        @JvmField
        val JOIN_DEFEAT_POST = EventObservable<JoinDefeatEvent.Post>()
    }

    object Network : AbstractOwoNetwork(modResource("main")) {
        init {
            mainChannel.registerClientbound(JoinConfirmReceipt.Packet::class.java, JoinConfirmReceipt::handleClient)
            mainChannel.registerServerbound(JoinConfirmReceipt.Response::class.java, JoinConfirmReceipt::handleServer)
        }
    }

    object Holders {
        val JOIN_CONFIRM: Holder<JoinConfirmReceipt.Data> = Holder()
    }

    init {
        CustomProperties
        Network
        Holders
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.LOWEST, AttemptJoinOnDefeatHandler::handle)
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(
            Priority.LOWEST, AttemptCancelPokeballHit::handle
        )
    }
}