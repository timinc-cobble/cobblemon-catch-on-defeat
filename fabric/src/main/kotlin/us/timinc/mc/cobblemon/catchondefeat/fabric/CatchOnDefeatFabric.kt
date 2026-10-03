package us.timinc.mc.cobblemon.catchondefeat.fabric

import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat
import us.timinc.mc.cobblemon.catchondefeat.fabric.network.CatchOnDefeatFabricNetworkManager
import us.timinc.mc.cobblemon.timcore.fabric.AbstractFabricMod

object CatchOnDefeatFabric : AbstractFabricMod(CatchOnDefeat) {
    override fun onInitialize() {
        with(CatchOnDefeatFabricNetworkManager) {
            registerMessages()
            registerServerHandlers()
            CatchOnDefeat.Network.manager = this
        }
    }
}
