package xyz.windsoft.hidenseek.screen.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.block.entity.GameTotemHeadPoweredBlockEntity;
import xyz.windsoft.hidenseek.network.ClientToServer_GameStartPacket;
import xyz.windsoft.hidenseek.network.ModPacketHandler;
import xyz.windsoft.hidenseek.screen.menu.GameTotemHeadPoweredMenu;
import xyz.windsoft.hidenseek.utils.PlayerScore;

/*
 * This class creates the renderization of the Screen "Game Totem Head" and is used by the Client to render the UI
 * for the Player.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class GameTotemHeadPoweredScreen extends AbstractContainerScreen<GameTotemHeadPoweredMenu> {

    //Private static final variables
    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/game_totem_head_powered_gui.png");
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/widgets.png");

    //Private variables
    private Button startGameButton = null;
    private Button completeLeaderboardButton = null;

    //Public methods

    public GameTotemHeadPoweredScreen(GameTotemHeadPoweredMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        //Repass the call to parent class of this class
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public boolean isPauseScreen() {
        //Return "false" to prevent game from pausing, while in Single Player
        return false;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //Do the render of background using client GUI Graphics instance
        renderBackground(pGuiGraphics);

        //Repass the call to parent class of this class
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        //Do the render of Tooltips on the cursor position, using client GUI Graphics instance
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);

        //Render the score leaderboard top 3 text
        PlayerScore firstPlayer = menu.GetScoreboardInfoAboutPosition(1);
        PlayerScore secondPlayer = menu.GetScoreboardInfoAboutPosition(2);
        PlayerScore thirstPlayer = menu.GetScoreboardInfoAboutPosition(3);
        if (firstPlayer.GetScore() >= 0)
            pGuiGraphics.drawString(this.font,
                    Component.translatable("gui.hidenseek.game_totem_head_powered_screen.score_leaderboard_title"),
                    (this.leftPos + 10), (this.topPos + 20), 0x404040, false);  //<- 0x404040 is a HEX for Gray
        if (firstPlayer.GetScore() >= 0)
            pGuiGraphics.drawString(this.font,
                    Component.translatable("gui.hidenseek.game_totem_head_powered_screen.first_score", firstPlayer.GetNickname(), firstPlayer.GetScore()),
                    (this.leftPos + 10), (this.topPos + 38), 0x8C8C8C, false);  //<- 0x8C8C8C is a HEX for Light Gray
        if (secondPlayer.GetScore() >= 0)
            pGuiGraphics.drawString(this.font,
                    Component.translatable("gui.hidenseek.game_totem_head_powered_screen.second_score", secondPlayer.GetNickname(), secondPlayer.GetScore()),
                    (this.leftPos + 10), (this.topPos + 48), 0x8C8C8C, false);  //<- 0x8C8C8C is a HEX for Light Gray
        if (thirstPlayer.GetScore() >= 0)
            pGuiGraphics.drawString(this.font,
                    Component.translatable("gui.hidenseek.game_totem_head_powered_screen.third_score", thirstPlayer.GetNickname(), thirstPlayer.GetScore()),
                    (this.leftPos + 10), (this.topPos + 58), 0x8C8C8C, false);  //<- 0x8C8C8C is a HEX for Light Gray

        //Render the block icon that shows status of currently running game
        if (menu.isNecessaryTheLightPumpkin() == false)
            pGuiGraphics.blit(WIDGETS_TEXTURE, (this.leftPos + 146), (this.topPos + 102), 0, 236, 20, 20);
        if (menu.isNecessaryTheLightPumpkin() == true)
            pGuiGraphics.blit(WIDGETS_TEXTURE, (this.leftPos + 146), (this.topPos + 102), 20, 236, 20, 20);

        //Render the Start Game button, according to current status of the game
        startGameButton.active = !menu.isGameRunning();
        //Render the Complete Leadeboard button, according to current status of the scores
        if (firstPlayer.GetScore() == -1)
            completeLeaderboardButton.active = false;
        if (firstPlayer.GetScore() >= 0)
            completeLeaderboardButton.active = true;
    }

    //Private methods

    @Override
    protected void init() {
        //Repass the call to parent class of this class
        super.init();

        //Prepare the proportions, Sizes, elements Potions and other things of this Screen
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.leftPos = (int) (this.width - ((float) this.width / 2.0f) - ((float) this.imageWidth / 2.0f) - 0.0f);
        this.topPos = (int) (this.height - ((float) this.height / 2.0f) - ((float) this.imageHeight / 2.0f) - 0.0f);
        this.titleLabelY = (3 + 3);           //<- The value here is considered multiplicated by 2, by the game. The first value of "3" is the border of the texture PNG
        this.titleLabelX = (3 + 4);           //<- The value here is considered multiplicated by 2, by the game. The first value of "3" is the border of the texture PNG
        this.inventoryLabelY = (3 + 122 + 3); //<- The value here is considered multiplicated by 2, by the game. The first value of "3" is the border of the texture PNG
        this.inventoryLabelX = (3 + 4);       //<- The value here is considered multiplicated by 2, by the game. The first value of "3" is the border of the texture PNG
        //this.width = ???;   <- The Width is the total width of the game screen, and may vary between PCs
        //this.height = ??;   <- The Height is the total height of the game screen, and may vary between PCs

        //Prepare the start game button
        this.startGameButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.hidenseek.game_totem_head_powered_screen.start_game_button"), btn -> { this.OnStartGameButtonClick(btn); })
                .bounds((this.leftPos + 43), (this.topPos + 102), 90, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.hidenseek.game_totem_head_powered_screen.start_game_button_tooltip")))
                .build());
        //Prepare the all players of leaderboard button
        this.completeLeaderboardButton = addRenderableWidget(new ImageButton(
                (this.leftPos + 10), (this.topPos + 102),
                20, 20,
                0, 118,
                20,
                WIDGETS_TEXTURE,
                256, 256,
                btn -> { this.OnCompleteLeaderboardButtonClick(btn); },
                Component.literal("")){
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                //Get the normal Y of button texture, at normal state
                int currentYTex = this.yTexStart;
                //Enable different parts of the button texture, according to button state
                if (this.active == false) {
                    //If deactivate, search for the button texture 20px above the normal...
                    currentYTex -= this.yDiffTex;
                } else if (this.isHoveredOrFocused()) {
                    //If active, and with the cursor on the button, search for the button texture 20px below the normal...
                    currentYTex += this.yDiffTex;
                }
                //Finally, draw the button using the fixed Y coordinates, processed by this method...
                guiGraphics.blit(this.resourceLocation, this.getX(), this.getY(), this.xTexStart, currentYTex, this.width, this.height, this.textureWidth, this.textureHeight);
            }
        });
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        //Define the shader and the way to render the background texture, of this Screen
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BG_TEXTURE);

        //Do the calcs to get a position to place the texture in the center of the Screen
        int x = ((width - imageWidth) / 2);
        int y = ((height - imageHeight) / 2);

        //Render this Screen background texture using position calculated above
        pGuiGraphics.blit(BG_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        //If necessary, tender the white progress arrow...
        RenderProgressArrow(pGuiGraphics, x, y);
    }

    //Private auxiliar methods

    private void RenderProgressArrow(GuiGraphics guiGraphics, int x, int y){
        //If is some progress going, render the arrow based on it...
        if (menu.isProgressGoing() == true)
            guiGraphics.blit(BG_TEXTURE, (x + 153), (y + 47), 176, 0, 8, menu.GetScaledProgress());
    }

    private void OnStartGameButtonClick(Button button){
        //Send a packet to the server, to start a new game
        ModPacketHandler.SendToServer(new ClientToServer_GameStartPacket(menu.GetCurrentBlockEntityPosition(), System.currentTimeMillis()));
    }

    private void OnCompleteLeaderboardButtonClick(Button button){
        //Request the top 10 scores to the Menu of the Game Totem
        menu.GetScoreboardInfoAndSendTheTop10();
    }
}
