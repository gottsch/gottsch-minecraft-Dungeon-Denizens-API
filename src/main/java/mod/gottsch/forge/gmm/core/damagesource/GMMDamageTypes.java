/*
 * This file is part of Dungeon Denizens API.
 * Copyright (c) 2026 Mark Gottschling (gottsch)
 *
 * Dungeon Denizens API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dungeon Denizens API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Dungeon Denizens API.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package mod.gottsch.forge.gmm.core.damagesource;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

/**
 * {@link ResourceKey}s for GMM's spell damage types. These are data-driven — the actual
 * {@link DamageType} definitions live as JSON under {@code data/gmm/damage_type/} (GMM ships data,
 * but registers nothing in code). Any mod consuming GMM's spell projectiles gets these for free.
 *
 * @author Mark Gottschling on 7/2/2026
 */
public class GMMDamageTypes {
    public static final ResourceKey<DamageType> FIRESPOUT_SPELL = register("firespout_spell");
    public static final ResourceKey<DamageType> HARM_SPELL = register("harm_spell");
    public static final ResourceKey<DamageType> DISINTEGRATE_SPELL = register("disintegrate_spell");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(GMM.MOD_ID, name));
    }

    /**
     * Builds a {@link DamageSource} for the given damage-type key from the level's registry — the
     * same thing vanilla's {@code DamageSources.source(ResourceKey)} does, but public and without
     * requiring an access transformer (that method is private; a library shouldn't impose an AT).
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
