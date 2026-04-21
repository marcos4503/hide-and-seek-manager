package xyz.windsoft.hidenseek.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
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
 * - This packet is created by the Server and sended to Client, as a Signal of the stage "Finished".
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ServerToClient_GameFinishedPacket {

    //Private final encodable data to store in this Packet
    //private final String roleAnnounceMessage;

    //Public methods

    public ServerToClient_GameFinishedPacket(){
        //This constructor is runned in two scenarios:
        //- On the Client/Server side, when this Packet is created, to be sended to Client/Server.
        //- On the Client/Server side, when the Client/Server receive this Packet, right after the next constructor runs, decoding the data, and then, calling this constructor, to store the decoded data. After this, the "Handle" method is runned.

        //Get the data to be stored in this Packet...
        //this.roleAnnounceMessage = roleAnnounceMessage;
    }

    public ServerToClient_GameFinishedPacket(FriendlyByteBuf decodeBuffer){
        //This constructor is runned in one scenario:
        //- On the Client/Server side, when the Client/Server receive this Packet, so, this constructor is called by the Forge, informing a "decodeBuffer" to be decoded, containing the encoded data that was sended by the Client/Server.

        //Decode each data sended by the Client or Server side, that was created this Packet, and call the previous constructor, informing all the decoded data.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the previous constructor.
        //this(decodeBuffer.readUtf());
    }

    public void Encode(FriendlyByteBuf encodeBuffer){
        //This method is called automatically by the Forge, to encode this Packet data, right before send this Packet to the Client/Server side, that will receive this.

        //Encode each data that will be sended by the Client or Server side. So the side that will receive this Packet will decode this and handle it.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the FIRST constructor.
        //encodeBuffer.writeUtf(this.roleAnnounceMessage);
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
            //Change the UI to stage of "Finished"
            OnRenderGui.currentGui = OnRenderGui.GUI.Finished;
            //Reset the Adrenaline Points counter
            OnRenderGui.progress_adrenalinePoints = -1;

            //Inform that the packet was handled now
            context.setPacketHandled(true);
        });
    }
}