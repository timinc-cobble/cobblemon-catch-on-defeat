package us.timinc.mc.cobblemon.catchondefeat.client.screen

import com.cobblemon.mod.common.api.gui.ColourLibrary
import com.cobblemon.mod.common.api.gui.MultiLineLabelK
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.TranslationComponents.wouldLikeToJoinTeam
import us.timinc.mc.cobblemon.catchondefeat.CatchOnDefeat.modResource
import us.timinc.mc.cobblemon.catchondefeat.client.widget.ConfirmJoinButton
import us.timinc.mc.cobblemon.catchondefeat.network.JoinConfirmReceipt
import java.io.FileNotFoundException


class ConfirmJoinScreen(private val packet: JoinConfirmReceipt.Packet) :
    Screen(Component.translatable(modResource("confirm_join").toLanguageKey())),
    CobblemonRenderable {
    companion object {
        // Screen root widget params
        const val BASE_WIDTH = 239
        const val BASE_HEIGHT = 116

        private val base = modResource("textures/gui/join_screen_base.png")
        private val backgroundBallResource = cobblemonResource("textures/gui/starterselection/background_poke_ball.png")
        private val platformResource = cobblemonResource("textures/gui/starterselection/platform_base.png")
        private val platformShadow = cobblemonResource("textures/gui/pokedex/platform_shadow.png")
    }

    private var responded: Boolean = false

    var ticksElapsed = 0
    var currentBallBackgroundFrame = 0

    override fun renderBlurredBackground(delta: Float) {}

    override fun renderMenuBackground(context: GuiGraphics) {}

    override fun init() {
        super.init()

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        val pokeWidget = ModelWidget(
            pX = x + 6, pY = y + 7,
            pWidth = 118, pHeight = 100,
            pokemon = packet.renderable,
            baseScale = 2.7f,
            playCryOnClick = true,
            offsetY = -12.0
        )

        addRenderableWidget(pokeWidget)

        val proceed = createButton(
            x + 134,
            y + 63,
            cancel = false,
            countdown = packet.countdown,
            callback = {
                responded = true
                packet.accept()
                onClose()
            }
        )

        addRenderableWidget(proceed)

        val cancel = createButton(
            x + 186,
            y + 63,
            cancel = true,
            callback = {
                responded = false
                packet.reject()
                onClose()
            }
        )

        addRenderableWidget(cancel)

        super.init()
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val matrices = context.pose()
        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        //Background
        blitk(
            matrixStack = matrices,
            texture = cobblemonResource("textures/gui/starterselection/background.png"),
            x = x + 6,
            y = y + 7,
            width = 118,
            height = 100
        )

        blitk(
            matrixStack = matrices,
            texture = backgroundBallResource,
            x = x + 10.5,
            y = y + 2.5,
            width = 109,
            height = 109,
            textureHeight = 1744,
            vOffset = currentBallBackgroundFrame * 109
        )

        //Platform
        blitk(
            matrixStack = matrices,
            texture = platformResource,
            x = x + 8.5, y = y + 78,
            width = 113, height = 32
        )

        val typePlatform = getPlatformResource(packet.renderable.form.primaryType)
        if (typePlatform != null) {
            blitk(
                matrixStack = matrices,
                texture = typePlatform,
                x = x + 8.5, y = y + 72,
                width = 113, height = 30
            )
        }

        blitk(
            matrixStack = matrices,
            texture = platformShadow,
            x = (x + 43) / 0.5F,
            y = (y + 83.5) / 0.5F,
            width = 90, height = 20,
            scale = 0.5F
        )

        // Base
        blitk(
            matrixStack = matrices,
            texture = base,
            x = x, y = y,
            width = BASE_WIDTH, height = BASE_HEIGHT
        )

        // Join text
        val smallTextScale = 0.5F

        matrices.pushPose()
        matrices.scale(smallTextScale, smallTextScale, 1F)
        MultiLineLabelK.create(
            component = wouldLikeToJoinTeam(packet.name),
            width = 96 / smallTextScale,
            maxLines = 5
        ).renderLeftAligned(
            context = context,
            x = (x + 136) / smallTextScale,
            y = (y + 19) / smallTextScale,
            ySpacing = 5.5 / smallTextScale,
            colour = ColourLibrary.WHITE,
            shadow = true
        )
        matrices.popPose()

        super.render(context, mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = when {
        (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) -> {
            responded = false
            packet.reject()
            this.onClose()
            return true
        }

        else -> super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun tick() {
        if (packet.countdown != null && ticksElapsed == (packet.countdown * 20)) {
            packet.reject()
            responded = false
            onClose()
        }

        ticksElapsed++

        val delay = 3
        if (ticksElapsed % delay == 0) currentBallBackgroundFrame++
        if (currentBallBackgroundFrame == 16) currentBallBackgroundFrame = 0
    }

    fun getPlatformResource(type: ElementalType): ResourceLocation? = try {
        cobblemonResource("textures/gui/starterselection/starter_platform_base_${type.showdownId}.png")
    } catch (_: FileNotFoundException) {
        null
    }

    fun createButton(
        x: Int,
        y: Int,
        cancel: Boolean,
        callback: (Button) -> Unit,
        countdown: Int? = null
    ): Button = ConfirmJoinButton(x, y, cancel, callback, countdown)
}
