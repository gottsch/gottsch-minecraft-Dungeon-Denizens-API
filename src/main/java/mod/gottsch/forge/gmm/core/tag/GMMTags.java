package mod.gottsch.forge.gmm.core.tag;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;

/**
 * @author by Mark Gottschling on 11/6/2025
 */
@Mod.EventBusSubscriber(modid = GMM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GMMTags {
    public static class Items {

        public static final TagKey<Item> GHOUL_FOOD = mod(GMM.MOD_ID, "ghoul/food");

        // SkeletonWarrior equipment pools (consumer-populated, one per slot)
        public static final TagKey<Item> SKELETON_WARRIOR_WEAPONS = mod(GMM.MOD_ID, "skeleton_warrior/weapons");
        public static final TagKey<Item> SKELETON_WARRIOR_HELMETS = mod(GMM.MOD_ID, "skeleton_warrior/helmets");
        public static final TagKey<Item> SKELETON_WARRIOR_CHESTPLATES = mod(GMM.MOD_ID, "skeleton_warrior/chestplates");
        public static final TagKey<Item> SKELETON_WARRIOR_LEGGINGS = mod(GMM.MOD_ID, "skeleton_warrior/leggings");
        public static final TagKey<Item> SKELETON_WARRIOR_BOOTS = mod(GMM.MOD_ID, "skeleton_warrior/boots");

        // Orc weapon pool (consumer-populated)
        public static final TagKey<Item> ORC_WEAPONS = mod(GMM.MOD_ID, "orc/weapons");

        // Shadow spawn weapon pool (consumer-populated)
        public static final TagKey<Item> SHADOW_WEAPONS = mod(GMM.MOD_ID, "shadow/weapons");
        // Shadow-bane weapons: bypass the Shadow's metal resistance and deal bonus damage.
        // BANE = +2 (gold-tier bane), MINOR_BANE = +1. Gold swords are handled intrinsically.
        public static final TagKey<Item> SHADOW_BANE = mod(GMM.MOD_ID, "shadow/bane");
        public static final TagKey<Item> SHADOW_MINOR_BANE = mod(GMM.MOD_ID, "shadow/minor_bane");

        public static TagKey<Item> mod(String domain, String path) {
            return ItemTags.create(new ResourceLocation(domain, path));
        }
    }

    /**
     * Entity-type tags used by gmm's reusable AI goals. gmm owns the tag KEYS but
     * deliberately leaves them empty; CONSUMER mods populate them (e.g. a mod adds
     * its own mobs to HEADLESS_HURT_ALLIES via data/gmm/tags/entity_types/headless/hurt_allies.json)
     * so the shared goals never need to reference consumer mob classes.
     */
    public static class EntityTypes {

        // mobs a Headless alerts when it is hurt
        public static final TagKey<EntityType<?>> HEADLESS_HURT_ALLIES = mod(GMM.MOD_ID, "headless/hurt_allies");
        // mobs a Headless alerts when it acquires a target
        public static final TagKey<EntityType<?>> HEADLESS_TARGET_ALLIES = mod(GMM.MOD_ID, "headless/target_allies");

        public static TagKey<EntityType<?>> mod(String domain, String path) {
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(domain, path));
        }
    }
}
