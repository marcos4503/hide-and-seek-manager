package xyz.windsoft.hidenseek.events;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.config.Config;

/*
 * This class manages the HUD renderization of Hide'n Seek games that is running.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnRenderGui {

    //Private static final variables
    private static final ResourceLocation ADRENALINE_GUI = new ResourceLocation(Main.MODID, "textures/gui/adrenaline_gui.png");
    private static final ResourceLocation SOUND_BAIT_FEEDBACK_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/sound_bait_feedback.png");

    //Public enums
    public static enum GUI{
        StandBy,
        WaitingPlayers,
        Preparation,
        Count,
        Progress,
        Celebration,
        Finished
    }

    //Public static variables
    public static GUI currentGui = GUI.StandBy;
    public static String standBy_welcomeMsg = "";
    public static String waitingPlayers_waitingMsg = "";
    public static String count_roleAnnounceMessage = "";
    public static String count_roleAnnounceSubMessage = "";
    public static String count_roleAnnounceColor = "";
    public static String count_countMessage = "";
    public static String count_countMessageColor = "";
    public static int count_currentCount = -1;
    public static int count_currentMaxCount = -1;
    public static int progress_adrenalinePoints = -1;
    public static long progress_soundBaitFeedbackLastTriggerTimeMs = -1;
    public static int progress_soundBaitFeedbackDistanceToSeeker = -1;
    public static String celebration_endGameResultMsg = "";
    public static String celebration_endGameResultMsgColor = "";

    //Public events

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        //If not is the logical client, stop here
        if (FMLEnvironment.dist != Dist.CLIENT)
            return;

        //If the renderization layer is different from "HOTBAR", cancel here
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()) == false)
            return;



        //Get the GUI Graphics and data
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font minecraftFont = Minecraft.getInstance().font;
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        //Render the GUI, according to requested GUI
        if (currentGui == GUI.StandBy){
            //Prepare the position of the Welcome Message
            int welcomeMsgWidth = minecraftFont.width(standBy_welcomeMsg);
            int welcomeMsgX = (int)(((float)screenWidth / 2.0f) - ((float)welcomeMsgWidth / 2.0f));
            int welcomeMsgY = 4 + Config.guiYOffset;
            //Draw the Welcome Message, on the center top of screen
            guiGraphics.drawString(minecraftFont, standBy_welcomeMsg, welcomeMsgX, welcomeMsgY, 0xFFFFFF, false);
        }
        if (currentGui == GUI.WaitingPlayers){
            //Prepare the position of the Waiting Message
            int waitingMsgWidth = minecraftFont.width(waitingPlayers_waitingMsg);
            int waitingMsgX = (int)(((float)screenWidth / 2.0f) - ((float)waitingMsgWidth / 2.0f));
            int waitingMsgY = 4 + Config.guiYOffset;
            //Draw the Waiting Message, on the center top of screen
            guiGraphics.drawString(minecraftFont, waitingPlayers_waitingMsg, waitingMsgX, waitingMsgY, 0xFFFFFF, false);
        }
        if (currentGui == GUI.Preparation){
            //...
        }
        if (currentGui == GUI.Count){
            //Prepare the position of the Count Message
            float messageScale = 1.5f;
            int countMsgWidth = minecraftFont.width(count_countMessage);
            int countMsgX = (int)(((float)screenWidth / (2.0f * messageScale)) - ((float)countMsgWidth / 2.0f) + 1);
            int countMsgY = (int)(((float)screenHeight / (2.0f * messageScale)) - 16);
            //Draw the Count Message, on the center top of screen
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(messageScale, messageScale, messageScale);
            if (count_countMessageColor.equals("WHITE") == true)
                guiGraphics.drawString(minecraftFont, count_countMessage, countMsgX, countMsgY, 0xFFFFFF, false);
            if (count_countMessageColor.equals("GOLDEN") == true)
                guiGraphics.drawString(minecraftFont, count_countMessage, countMsgX, countMsgY, 0xFFAA00, false);
            if (count_countMessageColor.equals("RED") == true)
                guiGraphics.drawString(minecraftFont, count_countMessage, countMsgX, countMsgY, 0xFF5555, false);
            if (count_countMessageColor.equals("GREEN") == true)
                guiGraphics.drawString(minecraftFont, count_countMessage, countMsgX, countMsgY, 0x55FF55, false);
            guiGraphics.pose().popPose();
            //Render the Role Announce, if necessary
            if (count_currentCount >= (count_currentMaxCount - 5)){
                //Prepare the position of the Role Announce Message
                float roleAnnounceMessageScale = 3.0f;
                int roleAnnounceMsgWidth = minecraftFont.width(count_roleAnnounceMessage);
                int roleAnnounceMsgX = (int)(((float)screenWidth / (2.0f * roleAnnounceMessageScale)) - ((float)roleAnnounceMsgWidth / 2.0f) - 2);
                int roleAnnounceMsgY = 4 + Config.guiYOffset;
                //Draw the Role Announce Message, on the center top of screen
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(roleAnnounceMessageScale, roleAnnounceMessageScale, roleAnnounceMessageScale);
                if (count_roleAnnounceColor.equals("RED") == true)
                    guiGraphics.drawString(minecraftFont, Component.literal(count_roleAnnounceMessage).withStyle(ChatFormatting.BOLD), roleAnnounceMsgX, roleAnnounceMsgY, 0xFF5555, false);
                if (count_roleAnnounceColor.equals("BLUE") == true)
                    guiGraphics.drawString(minecraftFont, Component.literal(count_roleAnnounceMessage).withStyle(ChatFormatting.BOLD), roleAnnounceMsgX, roleAnnounceMsgY, 0x5555FF, false);
                guiGraphics.pose().popPose();
                //Prepare the position of the Role Announce SubMessage
                int roleAnnounceSubMsgWidth = minecraftFont.width(count_roleAnnounceSubMessage);
                int roleAnnounceSubMsgX = (int)(((float)screenWidth / 2.0f) - ((float)roleAnnounceSubMsgWidth / 2.0f));
                int roleAnnounceSubMsgY = (38 + (int)((float)Config.guiYOffset * 3.0f));
                //Draw the Welcome Message, on the center top of screen
                if (count_roleAnnounceColor.equals("RED") == true)
                    guiGraphics.drawString(minecraftFont, count_roleAnnounceSubMessage, roleAnnounceSubMsgX, roleAnnounceSubMsgY, 0xFF5555, false);
                if (count_roleAnnounceColor.equals("BLUE") == true)
                    guiGraphics.drawString(minecraftFont, count_roleAnnounceSubMessage, roleAnnounceSubMsgX, roleAnnounceSubMsgY, 0x5555FF, false);
            }
        }
        if (currentGui == GUI.Progress){
            //If is required to render the Adrenaline Points GUI, continue...
            if (progress_adrenalinePoints != -1){
                //Get the title of the Adrenaline Points panel
                String adrenalinePanelTitle = Component.translatable("gui.hidenseek.game_stage.progress.hidder_adrenaline_panel_title").getString();
                int adrenalinePanelTitleWidth = (minecraftFont.width(adrenalinePanelTitle) + 10);
                int adrenalinePanelTitleHeight = minecraftFont.lineHeight;
                int adrenalinePanelContentHeight = 22;
                //Prepare the position of the Panel
                int conditionalAdrenalinePanelX = 0;
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null)
                    if (localPlayer.getOffhandItem().isEmpty() == false)
                        conditionalAdrenalinePanelX = 29;
                int adrenalinePanelX = (int)(((float)screenWidth / 2.0f) - ((float)adrenalinePanelTitleWidth / 2.0f) - 128 - conditionalAdrenalinePanelX - Config.guiAdrenalineXOffset);
                int adrenalinePanelY = (screenHeight - adrenalinePanelContentHeight);
                //Render the Adrenaline Points Panel
                guiGraphics.blitNineSliced(ADRENALINE_GUI, adrenalinePanelX, adrenalinePanelY, (adrenalinePanelTitleWidth), (adrenalinePanelContentHeight), 2, 32, 32, 0, 0);
                //Render the Adrenaline Points title
                drawStringWithOutline(guiGraphics, minecraftFont, adrenalinePanelTitle, (adrenalinePanelX + 5), (adrenalinePanelY - 5), 0xFFFFFF, 0x000000, false);
                //Render the Adreenaline Points
                drawStringWithOutline(guiGraphics, minecraftFont, String.valueOf(progress_adrenalinePoints), (adrenalinePanelX + 5), (adrenalinePanelY + 7), 0xFFFFD200, 0x000000, false);
            }
            //If is required to render the Sound Bait Distance Feedback GUI, continue...
            if ((System.currentTimeMillis() - progress_soundBaitFeedbackLastTriggerTimeMs) < 5000l){
                //Prepare the position of the Feedback Message
                int feedbackMsgWidth = minecraftFont.width(Component.translatable("gui.hidenseek.game_stage.progress.sound_bait_distance_feedback", progress_soundBaitFeedbackDistanceToSeeker));
                int feedbackMsgX = (int)(((float)screenWidth / 2.0f) - ((float)feedbackMsgWidth / 2.0f) + 1);
                int feedbackMsgY = (int)(((float)screenHeight / 2.0f) + 14);
                //Draw the Feedback Message, on the middle bottom of screen
                guiGraphics.drawString(minecraftFont, Component.translatable("gui.hidenseek.game_stage.progress.sound_bait_distance_feedback", progress_soundBaitFeedbackDistanceToSeeker), feedbackMsgX, feedbackMsgY, 0xFFFFFF, false);
            }
            //If is required to render the Sound Bait Feedback GUI, continue...
            if ((System.currentTimeMillis() - progress_soundBaitFeedbackLastTriggerTimeMs) < 1500l)
                guiGraphics.blit(SOUND_BAIT_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 81) / 2.0f), (int)((float)(screenHeight - 81) / 2.0f), 0, 0, 81, 81, 512, 512);
        }
        if (currentGui == GUI.Celebration){
            //Prepare the position of the Game End Result Message
            float gameEndResultMessageScale = 3.0f;
            int gameEndResultMsgWidth = minecraftFont.width(celebration_endGameResultMsg);
            int gameEndResultMsgX = (int)(((float)screenWidth / (2.0f * gameEndResultMessageScale)) - ((float)gameEndResultMsgWidth / 2.0f) - 6);
            int gameEndResultMsgY = 8 + Config.guiYOffset;
            //Draw the Game End Result Message, on the center top of screen
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(gameEndResultMessageScale, gameEndResultMessageScale, gameEndResultMessageScale);
            if (celebration_endGameResultMsgColor.equals("RED") == true)
                guiGraphics.drawString(minecraftFont, Component.literal(celebration_endGameResultMsg).withStyle(ChatFormatting.BOLD), gameEndResultMsgX, gameEndResultMsgY, 0xFF5555, false);
            if (celebration_endGameResultMsgColor.equals("BLUE") == true)
                guiGraphics.drawString(minecraftFont, Component.literal(celebration_endGameResultMsg).withStyle(ChatFormatting.BOLD), gameEndResultMsgX, gameEndResultMsgY, 0x5555FF, false);
            if (celebration_endGameResultMsgColor.equals("GOLDEN") == true)
                guiGraphics.drawString(minecraftFont, Component.literal(celebration_endGameResultMsg).withStyle(ChatFormatting.BOLD), gameEndResultMsgX, gameEndResultMsgY, 0xFFAA00, false);
            guiGraphics.pose().popPose();
        }
        if (currentGui == GUI.Finished){
            //...
        }
    }

    //Private auxiliar methods

    public void drawStringWithOutline(GuiGraphics graphics, Font font, String text, int x, int y, int color, int outlineColor, boolean useEnforcedOutline) {
        //Draw the Outline, before, drawing the string in 4 different corners
        graphics.drawString(font, text, x + 1, y, outlineColor, false);
        graphics.drawString(font, text, x - 1, y, outlineColor, false);
        graphics.drawString(font, text, x, y + 1, outlineColor, false);
        graphics.drawString(font, text, x, y - 1, outlineColor, false);

        //If is desired a Enforced Outline, draw in more 4 different directions, but in diagonal
        if (useEnforcedOutline == true){
            graphics.drawString(font, text, x + 1, y + 1, outlineColor, false);
            graphics.drawString(font, text, x - 1, y - 1, outlineColor, false);
            graphics.drawString(font, text, x + 1, y - 1, outlineColor, false);
            graphics.drawString(font, text, x - 1, y + 1, outlineColor, false);
        }

        //Finally, draw the final String
        graphics.drawString(font, text, x, y, color, false);
    }
}