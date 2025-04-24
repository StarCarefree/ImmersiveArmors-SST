package immersive_armors.armor_effects;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExplosionProtectionArmorEffect extends ArmorEffect {
    private final float strength;

    public ExplosionProtectionArmorEffect(float strength) {
        this.strength = strength;
    }

    @Override
    public float applyArmorToDamage(LivingEntity entity, DamageSource source, float amount, ItemStack armor) {
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            return amount * (1.0f - strength);
        } else {
            return amount;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(Text.translatable("armorEffect.explosionResistance", (int)(strength * 100)).formatted(Formatting.DARK_GREEN));
    }
}
