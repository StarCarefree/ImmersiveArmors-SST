package immersive_armors.client.render.entity.piece;

import immersive_armors.client.render.entity.model.CapeModel;
import immersive_armors.item.ExtendedArmorItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class CapePiece<M extends CapeModel<LivingEntity>> extends Piece {
    private final M model;

    public CapePiece(M model) {
        this.model = model;
    }

    private Identifier getCapeTexture(ExtendedArmorItem item, boolean overlay) {
        return new Identifier("immersive_armors", "textures/models/armor/" + item.getMaterial().getName() + "/cape" + (overlay ? "_overlay" : "") + ".png");
    }

    public <T extends LivingEntity, A extends BipedEntityModel<T>> void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, ItemStack itemStack, float tickDelta, EquipmentSlot armorSlot, A armorModel) {
        if (itemStack.getItem() instanceof ExtendedArmorItem armor) {
            //update cape motion
            CapeAngles angles = new CapeAngles(itemStack);
            angles.updateCapeAngles(entity, tickDelta);
            angles.store(itemStack);

            matrices.push();
            matrices.translate(0.0D, 0.0D, 0.125D);

            float n = entity.prevBodyYaw + (entity.bodyYaw - entity.prevBodyYaw);
            double o = MathHelper.sin(n * 0.017453292F);
            double p = -MathHelper.cos(n * 0.017453292F);
            double q = angles.deltaY * 40.0F;
            q = MathHelper.clamp(q, -6.0F, 32.0F);
            double r = (angles.deltaX * o + angles.deltaZ * p) * 100.0F;
            r = MathHelper.clamp(r, 0.0F, 150.0F);
            double s = (angles.deltaX * p - angles.deltaZ * o) * 100.0F;
            s = MathHelper.clamp(s, -20.0F, 20.0F);
            if (r < 0.0F) {
                r = 0.0F;
            }

            if (entity.isInSneakingPose()) {
                q += 22.5F;
                matrices.translate(0.0, 0.25, 0.0);
            }

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) (6.0F + r / 2.0F + q)));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) (s / 2.0F)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (180.0F - s / 2.0F)));

            model.setAngles(entity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

            VertexConsumer vertexConsumer;
            if (isColored()) {
                int i = ((DyeableItem) armor).getColor(itemStack);
                float red = (float) (i >> 16 & 255) / 255.0F;
                float green = (float) (i >> 8 & 255) / 255.0F;
                float blue = (float) (i & 255) / 255.0F;

                vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getCapeTexture(armor, false)));
                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, red, green, blue, 1.0f);

                vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getCapeTexture(armor, true)));
            } else {
                vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(getCapeTexture(armor, false)));
            }
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
            matrices.pop();
        }
    }

    private static class CapeAngles {
        private static final String ANGLE_TAG = "capeAngles";

        private double capeX;
        private double capeY;
        private double capeZ;
        private double deltaX;
        private double deltaY;
        private double deltaZ;
        private float lastTickDelta;

        private Vec3d predictPosition(Entity entity, float tickDelta) {
            return new Vec3d(
                    MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
                    MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
                    MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ())
            );
        }

        private void updateCapeAngles(Entity entity, float tickDelta) {
            Vec3d pos = predictPosition(entity, tickDelta);

            double dx = pos.getX() - capeX;
            double dy = pos.getY() - capeY;
            double dz = pos.getZ() - capeZ;

            if (dx > 10.0D || dx < -10D) {
                this.capeX = pos.getX();
                dx = 0;
            }

            if (dy > 10.0D || dy < -10D) {
                this.capeY = pos.getY();
                dy = 0;
            }

            if (dz > 10.0D || dz < -10D) {
                this.capeZ = pos.getZ();
                dz = 0;
            }

            float delta = tickDelta - lastTickDelta;
            if (delta < 0.0) {
                delta = 1.0f + delta;
            }
            delta *= 0.25f;
            lastTickDelta = tickDelta;

            this.capeX += dx * delta;
            this.capeZ += dz * delta;
            this.capeY += dy * delta;

            this.deltaX = capeX - pos.getX();
            this.deltaY = capeY - pos.getY();
            this.deltaZ = capeZ - pos.getZ();
        }

        public CapeAngles(ItemStack cape) {
            NbtCompound tag = cape.getOrCreateNbt();
            if (tag.contains(ANGLE_TAG)) {
                NbtCompound angles = tag.getCompound(ANGLE_TAG);
                capeX = angles.getDouble("capeX");
                capeY = angles.getDouble("capeY");
                capeZ = angles.getDouble("capeZ");
                lastTickDelta = angles.getFloat("lastTickDelta");
            }
        }

        public void store(ItemStack cape) {
            NbtCompound tag = cape.getOrCreateNbt();
            NbtCompound angles = new NbtCompound();
            angles.putDouble("capeX", capeX);
            angles.putDouble("capeY", capeY);
            angles.putDouble("capeZ", capeZ);
            angles.putFloat("lastTickDelta", lastTickDelta);
            tag.put(ANGLE_TAG, angles);
        }
    }
}
