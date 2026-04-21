package xyz.windsoft.hidenseek.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
 * This class have the Mixins for inject code into "Scoreboard" mechanic, to change color of the numbers in each line
 * of the Scoreboard, and to make then disabled.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

//All code changes by Injections, Overwrites, Redirections, etc. in this class, will apply to the "Gui.class" of the game code...
@Mixin(Gui.class)
public class GuiMixin {

    //Private methods
    private Gui self(){
        //Mixin can't use "this" keyword inside methods code, so, calling this method is the same of use "this" keyword, to be used inside of Mixin methods code...
        return (Gui)(Object) this;
    }

    //Public Redirects

    @Redirect(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
    private int SetScoresInEachScoreboardLinesAsTransparent(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean shadow){
        //If the call to "pGuiGraphics.drawString()", inside the method "displayScoreboardSidebar", is passing a formatted score number text wich contains "§c" or "any number", set to be invisible
        if (text.contains("§c") == true || text.matches("-?\\d+") == true)
            return 0;   //<- Set to be invisible
        //If the call to "pGuiGraphics.drawString()", inside the method "displayScoreboardSidebar", don't match with we expect, allow it to run untouched
        return instance.drawString(font, text, x, y, color, shadow);
    }

    @Redirect(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private int zerarLarguraDoNumero(Font instance, String text) {
        //If the call to "Font.width()", inside the method "displayScoreboardSidebar", is passing a formatted score number text wich contains "§c" or "any number", set to be 0
        if (text.contains("§c") == true || text.matches("-?\\d+") == true)
            return 0;   //<- Set to have width of 0
        //If the call to "Font.width()", inside the method "displayScoreboardSidebar", don't match with we expect, allow it to run untouched
        return instance.width(text);
    }
}