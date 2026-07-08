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
package mod.gottsch.forge.gmm.core.particle;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * gmm-owned particle types. Like {@link mod.gottsch.forge.gmm.core.sound.GMMSounds}, this is shared
 * <em>asset</em> infrastructure (registry-required-to-be-usable), not per-consumer game content, so
 * it lives in gmm rather than each consumer mod. The client-side {@code ParticleProvider}s are wired
 * in {@code GMMClientSetup}; the sprite JSON + textures ship under {@code assets/gmm}.
 *
 * @author Mark Gottschling on 7/3/2026
 */
public class GMMParticles {
	public static final DeferredRegister<ParticleType<?>> PARTICLES =
			DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GMM.MOD_ID);

	/** A caustic acid droplet: hangs briefly, then falls and vanishes on contact. */
	public static final RegistryObject<SimpleParticleType> ACID_DRIP =
			PARTICLES.register("acid_drip", () -> new SimpleParticleType(false) {});

	/** A blood droplet — same drip behaviour as {@link #ACID_DRIP}, red teardrop sprite (Bloody Bones). */
	public static final RegistryObject<SimpleParticleType> BLOOD_DRIP =
			PARTICLES.register("blood_drip", () -> new SimpleParticleType(false) {});

	/** A short-lived, additively-lit electric spark used for lightning arcs / crackle. */
	public static final RegistryObject<SimpleParticleType> ELECTRIC_SPARK =
			PARTICLES.register("electric_spark", () -> new SimpleParticleType(false) {});

	/**
	 * A flame "runner" for the Burning Skeleton's aura pulse: travels flat outward to a passed reach,
	 * then bends upward. The outward direction + flat reach are carried in the spawn velocity args (see
	 * {@code BurningAuraFlameParticle}). {@code true} = always show (a telegraph shouldn't be culled by
	 * the client's particle-density setting).
	 */
	public static final RegistryObject<SimpleParticleType> BURNING_AURA =
			PARTICLES.register("burning_aura", () -> new SimpleParticleType(true) {});

	public static void register(IEventBus modEventBus) {
		PARTICLES.register(modEventBus);
	}
}
