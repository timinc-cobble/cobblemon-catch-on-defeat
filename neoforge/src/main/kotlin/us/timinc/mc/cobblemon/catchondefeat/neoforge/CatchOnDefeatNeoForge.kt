package us.timinc.mc.cobblemon.catchondefeat.neoforge

import net.neoforged.fml.common.Mod
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat
import us.timinc.mc.cobblemon.catchondefeat.MOD_ID
import us.timinc.mc.cobblemon.catchondefeat.neoforge.network.CatchOnDefeatNeoForgeNetworkManager
import us.timinc.mc.cobblemon.timcore.neoforge.AbstractNeoForgeMod
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(MOD_ID)
object CatchOnDefeatNeoForge : AbstractNeoForgeMod(CatchOnDefeat) {
    init {
        CatchOnDefeat.Network.manager = CatchOnDefeatNeoForgeNetworkManager
        MOD_BUS.addListener((CatchOnDefeat.Network.manager as CatchOnDefeatNeoForgeNetworkManager)::registerMessages)
    }
}
