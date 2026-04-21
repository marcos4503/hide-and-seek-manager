package xyz.windsoft.hidenseek.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.windsoft.hidenseek.Main;

/*
 * This class manage the Packets registration, for example, how the Packets will encode/decode their data, process the request, etc. Packets
 * can't be sended from Client to Server (and vice-versa), if was not registered here.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModPacketHandler {

    //Private static final variables
    private static final String PROTOCOL_VERSION = "1";

    //Public static final variables
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(Main.MODID, "main"), () -> { return PROTOCOL_VERSION; }, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    //Public static methods

    public static void Register(){
        //Register the "Client To Server Game Start Packet" packet...
        INSTANCE.messageBuilder(ClientToServer_GameStartPacket.class, 1001)
                .encoder(ClientToServer_GameStartPacket::Encode)
                .decoder(ClientToServer_GameStartPacket::new)
                .consumerMainThread(ClientToServer_GameStartPacket::Handle)
                .add();
        //Register the "Client To Server Item Cooldown Confirmation" packet...
        INSTANCE.messageBuilder(ClientToServer_ItemCooldownConfirmation.class, 10011)
                .encoder(ClientToServer_ItemCooldownConfirmation::Encode)
                .decoder(ClientToServer_ItemCooldownConfirmation::new)
                .consumerMainThread(ClientToServer_ItemCooldownConfirmation::Handle)
                .add();
        //Register the "Server To Client Game StandBy Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameStandByPacket.class, 1002)
                .encoder(ServerToClient_GameStandByPacket::Encode)
                .decoder(ServerToClient_GameStandByPacket::new)
                .consumerMainThread(ServerToClient_GameStandByPacket::Handle)
                .add();
        //Register the "Server To Client Waiting Players Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameWaitingPlayersPacket.class, 1003)
                .encoder(ServerToClient_GameWaitingPlayersPacket::Encode)
                .decoder(ServerToClient_GameWaitingPlayersPacket::new)
                .consumerMainThread(ServerToClient_GameWaitingPlayersPacket::Handle)
                .add();
        //Register the "Server To Client Game Count Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameCountPacket.class, 1004)
                .encoder(ServerToClient_GameCountPacket::Encode)
                .decoder(ServerToClient_GameCountPacket::new)
                .consumerMainThread(ServerToClient_GameCountPacket::Handle)
                .add();
        //Register the "Server To Client Game Count Camping Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameCountCampPacket.class, 10041)
                .encoder(ServerToClient_GameCountCampPacket::Encode)
                .decoder(ServerToClient_GameCountCampPacket::new)
                .consumerMainThread(ServerToClient_GameCountCampPacket::Handle)
                .add();
        //Register the "Server To Client Game Progress Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameProgressPacket.class, 1005)
                .encoder(ServerToClient_GameProgressPacket::Encode)
                .decoder(ServerToClient_GameProgressPacket::new)
                .consumerMainThread(ServerToClient_GameProgressPacket::Handle)
                .add();
        //Register the "Server To Client Game Progress Adrenaline Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameProgressAdrenalinePacket.class, 10051)
                .encoder(ServerToClient_GameProgressAdrenalinePacket::Encode)
                .decoder(ServerToClient_GameProgressAdrenalinePacket::new)
                .consumerMainThread(ServerToClient_GameProgressAdrenalinePacket::Handle)
                .add();
        //Register the "Server To Client Game Progress Sound Bait Feedback Packet" packet...
        INSTANCE.messageBuilder(ServerToPlayer_GameProgressSoundBaitFeedbackPacket.class, 10052)
                .encoder(ServerToPlayer_GameProgressSoundBaitFeedbackPacket::Encode)
                .decoder(ServerToPlayer_GameProgressSoundBaitFeedbackPacket::new)
                .consumerMainThread(ServerToPlayer_GameProgressSoundBaitFeedbackPacket::Handle)
                .add();
        //Register the "Server To Client Game Celebration Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameCelebrationPacket.class, 1006)
                .encoder(ServerToClient_GameCelebrationPacket::Encode)
                .decoder(ServerToClient_GameCelebrationPacket::new)
                .consumerMainThread(ServerToClient_GameCelebrationPacket::Handle)
                .add();
        //Register the "Server To Client Game Finished Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_GameFinishedPacket.class, 1007)
                .encoder(ServerToClient_GameFinishedPacket::Encode)
                .decoder(ServerToClient_GameFinishedPacket::new)
                .consumerMainThread(ServerToClient_GameFinishedPacket::Handle)
                .add();
    }

    public static void SendToServer(Object msg){
        //Send the message from Client to Server
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void SendToPlayer(Object msg, ServerPlayer serverPlayer){
        //Send the message from Server to specific Player
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> { return serverPlayer; }), msg);
    }

    public static void SendToAllClients(Object msg){
        //Send the message from Server to all Clients
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}