package xyz.windsoft.hidenseek.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import xyz.windsoft.hidenseek.Main;
import xyz.windsoft.hidenseek.entity.custom.SoundBaitEntity;

/*
 * This class creates the custom rendere for the entity of "Sound Bait"
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class SoundBaitEntityRenderer extends EntityRenderer<SoundBaitEntity> {

    //Private static final variables
    private static final ResourceLocation TEXTURE_AIR = new ResourceLocation(Main.MODID, "textures/entity/sound_bait/air.png");
    private static final ResourceLocation TEXTURE_PLANTED = new ResourceLocation(Main.MODID, "textures/entity/sound_bait/planted.png");
    private static final ResourceLocation TEXTURE_NOISING = new ResourceLocation(Main.MODID, "textures/entity/sound_bait/noising.png");

    //Public methods

    public SoundBaitEntityRenderer(EntityRendererProvider.Context context) {
        //Initialize the Renderer, informing the context of instantiation
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SoundBaitEntity entity) {
        //Inform the Texture to use during render of this Entity, where the Vanilla game requests it...
        return TEXTURE_AIR;
    }

    @Override
    public void render(SoundBaitEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //Get the current state of the Renderization of Elements in-game, before do changes in Renderization of this Entity
        poseStack.pushPose();

        //Get informations about the Sound Bait entity...
        boolean isPlanted = entity.isPlanted();
        boolean isNoising = entity.isNoising();
        //Prepare the texture to use for the Sound Bait entity...
        ResourceLocation textureToUse = null;
        float textureToUseSize = 0.35f;

        //If is in air...
        if (isPlanted == false && isNoising == false){
            //Make the Sound Bait always face the camera
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            //Make the Sound Bait rotate on Z axis, to do a effect of throw
            float rotation = (entity.tickCount + partialTicks) * 15.0f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            //Inform the texture to use now
            textureToUse = TEXTURE_AIR;
        }
        //If is planted, but not noising...
        if (isPlanted == true && isNoising == false){
            //Make the Sound Bait always face the camera
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            //Inform the texture to use now
            textureToUse = TEXTURE_PLANTED;
        }
        //If is planted and noising...
        if (isPlanted == true && isNoising == true){
            //Make the Sound Bait always face the camera
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            //Inform the texture to use now
            textureToUse = TEXTURE_NOISING;
        }

        //Create the render buffer that will prepare the "cutout" using the texture as reference
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(textureToUse)); //<- Uses "entityCutoutNoCull", to render the texture on both sides of the Quad

        //Prepare the Quad data
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();
        //Draw each of the 4 vertex of the Quad
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -textureToUseSize, -textureToUseSize, 0.0f, 0.0f, 1.0f);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, textureToUseSize, -textureToUseSize, 0.0f, 1.0f, 1.0f);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, textureToUseSize, textureToUseSize, 0.0f, 1.0f, 0.0f);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -textureToUseSize, textureToUseSize, 0.0f, 0.0f, 0.0f);

        //Restore the previous state of the Renderization of Elements in-game, now, that was finished the Renderization of this Entity
        poseStack.popPose();
        //Re-pass this call, to the Parent class of this, running the default steps of the Game
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    //Private auxiliar methods

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int light, float x, float y, float z, float u, float v) {
        //Draw the Vertex with the informations provided
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
    }
}