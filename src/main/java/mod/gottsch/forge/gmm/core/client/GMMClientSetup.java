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
package mod.gottsch.forge.gmm.core.client;

import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.client.particle.AcidDripParticle;
import mod.gottsch.forge.gmm.core.client.particle.BurningAuraFlameParticle;
import mod.gottsch.forge.gmm.core.client.particle.ElectricSparkParticle;
import mod.gottsch.forge.gmm.core.particle.GMMParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only mod-bus setup for gmm's shared assets. Currently just binds the sprite-based
 * {@code ParticleProvider}s for {@link GMMParticles}. (Entity renderers stay consumer-registered
 * — see each consumer's ClientSetup — but particle providers are pure gmm-asset wiring, so gmm owns
 * them, mirroring how gmm owns its SoundEvents.)
 *
 * @author Mark Gottschling on 7/3/2026
 */
@Mod.EventBusSubscriber(modid = GMM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class GMMClientSetup {

	@SubscribeEvent
	public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(GMMParticles.ACID_DRIP.get(), AcidDripParticle.Provider::new);
		event.registerSpriteSet(GMMParticles.ELECTRIC_SPARK.get(), ElectricSparkParticle.Provider::new);
		event.registerSpriteSet(GMMParticles.BURNING_AURA.get(), BurningAuraFlameParticle.Provider::new);
	}
}
