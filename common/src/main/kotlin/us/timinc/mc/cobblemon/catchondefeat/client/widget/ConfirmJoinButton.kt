package us.timinc.mc.cobblemon.catchondefeat.client.widget

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.render.drawScaledText
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.modResource

class ConfirmJoinButton(
    pX: Int, pY: Int,
    val cancel: Boolean,
    onPress: (Button) -> Unit,
    val countdown: Int? = null
) : Button(pX, pY, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("ConfirmJoinButton"), onPress, DEFAULT_NARRATION),
    CobblemonRenderable {

    companion object {
        private val cancelResource = modResource("textures/gui/join_screen_btn_cancel.png")
        private val proceedResource = modResource("textures/gui/join_screen_btn_proceed.png")
        const val BUTTON_WIDTH = 48
        const val BUTTON_HEIGHT = 16
        const val TEXT_HEIGHT = 10
    }

    val initMillis = Util.getMillis()

    override fun playDownSound(soundManager: SoundManager) {}

    fun getSeconds(): Long? {
        if (countdown == null) return null

        val endMillis = initMillis + (countdown * 1000)

        return ((endMillis - Util.getMillis()) / 1000) % 60
    }

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        blitk(
            matrixStack = matrices,
            texture = if (cancel) cancelResource else proceedResource,
            x = x,
            y = y,
            width = BUTTON_WIDTH,
            height = BUTTON_HEIGHT,
            textureHeight = BUTTON_HEIGHT * 2,
            vOffset = if (isHovered) BUTTON_HEIGHT else 0
        )

        drawScaledText(
            context = context,
            font = CobblemonResources.DEFAULT_LARGE,
            text = when (cancel) {
                true -> CommonComponents.GUI_CANCEL.copy()
                false -> when (countdown) {
                    null -> CommonComponents.GUI_PROCEED.copy()
                    else -> Component.translatable(
                        "catch_on_defeat.ui.proceed_cooldown",
                        CommonComponents.GUI_PROCEED,
                        getSeconds()
                    )
                }
            },
            x = x + BUTTON_WIDTH / 2,
            y = y + BUTTON_HEIGHT / 2 - TEXT_HEIGHT / 2,
            shadow = true,
            centered = true
        )
    }
}
