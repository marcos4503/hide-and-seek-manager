package xyz.windsoft.hidenseek.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/*
 * This class helps generating completely Random Fireworks.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class FireworkFactory {

    //Private static variables
    private static final RandomSource randomSource = RandomSource.create();

    //Public static methods

    public static ItemStack GenerateRandomFirework() {
        //Prepare the Firework
        ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag fireworkTag = fireworkStack.getOrCreateTagElement("Fireworks");
        //Generate a random flight duration
        fireworkTag.putByte("Flight", (byte) (randomSource.nextInt(2) + 1));
        //Generate the burst lit
        ListTag explosionsList = new ListTag();
        explosionsList.add(GenerateRandomBurst());
        //Add a possible second burst, for more complex Firework
        if (randomSource.nextBoolean() == true)
            explosionsList.add(GenerateRandomBurst());
        //Add the NBT to the Firwork
        fireworkTag.put("Explosions", explosionsList);
        //Return the Firework
        return fireworkStack;
    }

    //Private static methods

    private static CompoundTag GenerateRandomBurst() {
        //Prepare the NBT
        CompoundTag explosionTag = new CompoundTag();
        //Generate a random Shape (0=Ball, 1=Large Ball, 2=Star, 3=Creeper, 4=Burst)
        int shapeType = randomSource.nextInt(5);
        explosionTag.putByte("Type", (byte) shapeType);
        //Add possible extra effects
        explosionTag.putByte("Flicker", (byte) (randomSource.nextBoolean() ? 1 : 0));
        explosionTag.putByte("Trail", (byte) (randomSource.nextBoolean() ? 1 : 0));
        //Add random colors
        explosionTag.putIntArray("Colors", GenerateRandomColors(randomSource.nextInt(3) + 1));
        //Add possible fade colors
        if (randomSource.nextBoolean() == true)
            explosionTag.putIntArray("FadeColors", GenerateRandomColors(randomSource.nextInt(2) + 1));
        //Return the NBT
        return explosionTag;
    }

    private static int[] GenerateRandomColors (int quantity) {
        //Generate a array of colors
        int[] colors = new int[quantity];
        for (int i = 0; i < quantity; i++)
            colors[i] = DyeColor.values()[randomSource.nextInt(DyeColor.values().length)].getFireworkColor();   //<- Get a random color of Dye, and get the value of this, for Firework
        //Return the array of colors
        return colors;
    }
}