/*
 * This file is part of Dungeon Denizens API.
 * Copyright (c) 2025 Mark Gottschling (gottsch)
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
package mod.gottsch.forge.gmm.core;

import mod.gottsch.forge.gmm.core.config.GMMRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * gottsch's Monster Manual (gmm) is a pure base-class library, NOT a content mod.
 * It deliberately registers no EntityTypes, items, or other game objects of its own.
 * Instead it supplies reusable base mob classes, AI goals, models, renderers, textures,
 * and helpers. Consuming mods (e.g. Village Dungeons, Dungeon Denizens) depend on gmm
 * and register their own EntityTypes (villagedungeons:rat, dungeondenizens:rat, ...),
 * reusing the gmm-namespaced assets. One-off mobs may register in their own mod while
 * still contributing their model/renderer/texture back to this library for reuse.
 * <p>
 * The single exception is a datapack registry ({@link GMMRegistries#MOB_CONFIG}): gmm defines
 * the registry + codec, but ships no entries -- consumers supply the JSON. No game content.
 *
 * @author by Mark Gottschling on 4/11/2025
 */
@Mod(GMM.MOD_ID)
public class GMM {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "gmm";

    public GMM() {
        // by design gmm registers no game content; only its datapack registry (see class javadoc).
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(GMMRegistries.class);
    }
}
