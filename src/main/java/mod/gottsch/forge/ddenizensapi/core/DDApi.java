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
package mod.gottsch.forge.ddenizensapi.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author by Mark Gottschling on 4/11/2025
 */
@Mod(DDApi.MOD_ID)
public class DDApi {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "ddenizensapi";

    public DDApi(FMLJavaModLoadingContext context) {
        // NOTE there won't be any registering of anything
    }
}
