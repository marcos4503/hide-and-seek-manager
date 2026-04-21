package xyz.windsoft.hidenseek.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import xyz.windsoft.hidenseek.config.Config;
import xyz.windsoft.hidenseek.events.OnRenderGui;

import java.util.function.Supplier;

/*
 * This class is a Packet, and store data that will be created in Server/Client and sended to Server/Client, to be handled. Once the
 * Server/Client receive this Packet, the side get the data and handles it with a code.
 *
 * This Packet is CREATED in:
 * - Server
 * This Packet is HANDLED in:
 * - Client
 * What this Packet does?
 * - This packet is created by the Server and sended to Client, as a Signal of the stage "Count".
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ServerToClient_GameCountPacket {

    //Private final encodable data to store in this Packet
    private final String roleAnnounceMessage;
    private final String roleAnnounceSubMessage;
    private final String roleAnnounceColor;
    private final String countMessage;
    private final String countMessageColor;
    private final int countNumber;
    private final int countMaxNumber;

    //Public methods

    public ServerToClient_GameCountPacket(String roleAnnounceMessage, String roleAnnounceSubMessage, String roleAnnounceColor, String countMessage, String countMessageColor, int countNumber, int countMaxNumber){
        //This constructor is runned in two scenarios:
        //- On the Client/Server side, when this Packet is created, to be sended to Client/Server.
        //- On the Client/Server side, when the Client/Server receive this Packet, right after the next constructor runs, decoding the data, and then, calling this constructor, to store the decoded data. After this, the "Handle" method is runned.

        //Get the data to be stored in this Packet...
        this.roleAnnounceMessage = roleAnnounceMessage;
        this.roleAnnounceSubMessage = roleAnnounceSubMessage;
        this.roleAnnounceColor = roleAnnounceColor;
        this.countMessage = countMessage;
        this.countMessageColor = countMessageColor;
        this.countNumber = countNumber;
        this.countMaxNumber = countMaxNumber;
    }

    public ServerToClient_GameCountPacket(FriendlyByteBuf decodeBuffer){
        //This constructor is runned in one scenario:
        //- On the Client/Server side, when the Client/Server receive this Packet, so, this constructor is called by the Forge, informing a "decodeBuffer" to be decoded, containing the encoded data that was sended by the Client/Server.

        //Decode each data sended by the Client or Server side, that was created this Packet, and call the previous constructor, informing all the decoded data.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the previous constructor.
        this(decodeBuffer.readUtf(), decodeBuffer.readUtf(), decodeBuffer.readUtf(), decodeBuffer.readUtf(), decodeBuffer.readUtf(), decodeBuffer.readInt(), decodeBuffer.readInt());
    }

    public void Encode(FriendlyByteBuf encodeBuffer){
        //This method is called automatically by the Forge, to encode this Packet data, right before send this Packet to the Client/Server side, that will receive this.

        //Encode each data that will be sended by the Client or Server side. So the side that will receive this Packet will decode this and handle it.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the FIRST constructor.
        encodeBuffer.writeUtf(this.roleAnnounceMessage);
        encodeBuffer.writeUtf(this.roleAnnounceSubMessage);
        encodeBuffer.writeUtf(this.roleAnnounceColor);
        encodeBuffer.writeUtf(this.countMessage);
        encodeBuffer.writeUtf(this.countMessageColor);
        encodeBuffer.writeInt(this.countNumber);
        encodeBuffer.writeInt(this.countMaxNumber);
    }

    public void Handle(Supplier<NetworkEvent.Context> contextSupplier){
        //This method is called automatically by the Forge, on the Client/Server side that receives this Packet. This method is called after the Decoding of this Packet data,
        //on the side that receive this.

        //Get the context
        NetworkEvent.Context context = contextSupplier.get();

        //Get the direction of packet, info
        NetworkDirection directionOfThisPacket = context.getDirection();

        //Run this code in the Main Thread of target side
        context.enqueueWork(() -> {
            //Change the UI to stage of "Count"
            OnRenderGui.currentGui = OnRenderGui.GUI.Count;
            OnRenderGui.count_roleAnnounceMessage = roleAnnounceMessage;
            OnRenderGui.count_roleAnnounceSubMessage = roleAnnounceSubMessage;
            OnRenderGui.count_roleAnnounceColor = roleAnnounceColor;
            OnRenderGui.count_countMessage = countMessage;
            OnRenderGui.count_countMessageColor = countMessageColor;
            OnRenderGui.count_currentCount = countNumber;
            OnRenderGui.count_currentMaxCount = countMaxNumber;
            //Reset the Adrenaline Points counter
            OnRenderGui.progress_adrenalinePoints = -1;

            //Get the Minecraft Instance
            Minecraft minecraftInstance = Minecraft.getInstance();
            //Play a count sound, according to remaing time
            if (countMessageColor.equals("WHITE") == true)
                if (minecraftInstance.level != null)
                    minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.0f, 0.5f));
            if (countMessageColor.equals("GOLDEN") == true)
                if (minecraftInstance.level != null)
                    minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP.value(), 1.0f, 1.0f));
            if (countMessageColor.equals("RED") == true)
                if (minecraftInstance.level != null)
                    minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_COW_BELL.value(), 1.0f, 1.0f));
            if (countMessageColor.equals("GREEN") == true)
                if (minecraftInstance.level != null)
                    minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.value(), 2.0f, 1.0f));
            //Play the role sound
            if (countNumber == (countMaxNumber - 1)){
                if (roleAnnounceColor.equals("RED") == true)
                    if (minecraftInstance.level != null)
                        minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VINDICATOR_CELEBRATE, 1.0f, 1.0f));
                if (roleAnnounceColor.equals("BLUE") == true)
                    if (minecraftInstance.level != null)
                        minecraftInstance.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_YES, 1.0f, 1.0f));
            }

            //Inform that the packet was handled now
            context.setPacketHandled(true);
        });
    }
}