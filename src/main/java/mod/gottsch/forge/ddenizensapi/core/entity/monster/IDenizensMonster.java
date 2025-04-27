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
package mod.gottsch.forge.ddenizensapi.core.entity.monster;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

/**
 *
 * @author Mark Gottschling on Jan 9, 2024
 * @version 2.0
 * @since DDApi v1.0 4/12/2025
 *
 */
public interface IDenizensMonster {
    public static final String SUMMONED_OWNER = "summonedOwner";
    public static final String PLAYER_OWNER = "playerOwner";

    default public boolean canSummonedHaveOwner() {
        return false;
    }

    default public int getSummonedLifespan() {
        return 0;
    }

    default public void setSummonedLifespan(int lifespan) { }

    default public LivingEntity getSummonedOwner() {
        return null;
    }

    default public void setSummonedOwner(LivingEntity entity) {}
}
