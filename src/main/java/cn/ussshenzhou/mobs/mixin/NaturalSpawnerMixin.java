package cn.ussshenzhou.mobs.mixin;

import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @ModifyConstant(method = "isRightDistanceToPlayerAndSpawnPoint", constant = @Constant(doubleValue = 24.0))
    private static double closerMonsters(double constant) {
        return 16.0;
    }
}
