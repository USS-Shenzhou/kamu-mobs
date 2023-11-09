package cn.ussshenzhou.mobs;

import cn.ussshenzhou.t88.config.TConfig;
import com.google.gson.annotations.SerializedName;

/**
 * @author USS_Shenzhou
 */
public class MobsConfig implements TConfig {
    public MobsConfig() {
    }

    public double mobAmountFactor = 3;
    public double mobDamageFactor = 2;
    public double mobKnockBackPlus = 1.5;
}
