package immersive_armors.armor_effects;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class WeaponEfficiency extends ArmorEffect {
    private final float damage;

    public WeaponEfficiency(float damage) {
        this.damage = damage;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(Text.translatable("armorEffect.weaponEfficiency", (int)(damage * 100)).formatted(Formatting.GOLD));
    }

    @Override
    public float applyArmorToAttack(LivingEntity target, DamageSource source, float amount, ItemStack armor) {
        if (!source.isIndirect() && source.getAttacker() instanceof LivingEntity attacker) {
            //todo verify indirect on arrows
            if (isPrimaryArmor(armor, attacker)) {
                    amount *= (1.0f + getSetCount(armor, attacker) * damage);
            }
        }
        return amount;
    }
}
