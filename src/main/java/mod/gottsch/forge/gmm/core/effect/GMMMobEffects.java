package mod.gottsch.forge.gmm.core.effect;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * gmm-owned status effects. Like {@link mod.gottsch.forge.gmm.core.sound.GMMSounds} /
 * {@link mod.gottsch.forge.gmm.core.particle.GMMParticles}, this is registry <em>infrastructure</em> a
 * mob AI needs to be usable (a {@link MobEffect} must be registered before
 * {@code LivingEntity#addEffect} can apply it) rather than player-facing content -- nothing ever
 * grants it via a potion, item, or command; only GMM mob code applies it directly.
 *
 * @author Mark Gottschling on 7/9/2026
 */
public class GMMMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GMM.MOD_ID);

    /**
     * Stacking, temporary max-health reduction -- the Wight's life-drain bite. Each amplifier level
     * scales the attribute modifier automatically (vanilla's built-in {@code MobEffect} attribute-
     * modifier scaling, the same mechanism {@code MobEffects.HEALTH_BOOST} uses); vanilla's own
     * {@code MobEffectInstance} duration/removal machinery re-clamps the target's health when the
     * effect expires, so no bespoke expiry tracking is needed (see {@code Wight#applyWither}).
     */
    public static final RegistryObject<MobEffect> WITHERED = EFFECTS.register("withered",
            // MobEffect's constructor is protected (package-private-ish); the empty {} anonymous
            // subclass body is what lets a caller outside net.minecraft.world.effect invoke it.
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4B3B47) {}
                    .addAttributeModifier(Attributes.MAX_HEALTH, "f22b1e0a-6d1e-4e0a-9b1a-6b2c1a9d5e3a",
                            -2.0D, AttributeModifier.Operation.ADDITION));

    /** Called from the GMM constructor to attach the registry to the mod event bus. */
    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }
}
