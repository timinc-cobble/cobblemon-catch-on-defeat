package us.timinc.mc.cobblemon.catchondefeat.fabric

import net.fabricmc.api.ClientModInitializer
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat
import us.timinc.mc.cobblemon.catchondefeat.fabric.network.CatchOnDefeatFabricNetworkManager

object CatchOnDefeatFabricClient: ClientModInitializer {
    override fun onInitializeClient() {
        (CatchOnDefeat.Network.manager as CatchOnDefeatFabricNetworkManager).registerClientHandlers()
    }
}
