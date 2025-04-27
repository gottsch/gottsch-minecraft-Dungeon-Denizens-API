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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.Predicate;

// TODO could implement vanilla TraceableEntity for getOwner()

/**
 * @author Mark Gottschling on Apr 13, 2022
 *
 */
public abstract class DenizensMonster extends Monster implements IDenizensMonster {

	private static final int UNDERGROUND_HEIGHT = 60;

	public final Predicate<LivingEntity> playerNotOwner = (entity) -> {
		if (entity instanceof Player) {
			return getSummonedOwner() == null || !(getSummonedOwner() instanceof Player);
		}
		return true;
	};

	protected DenizensMonster(EntityType<? extends Monster> mob, Level level, MonsterSize size) {
		super(mob, level);
	}

	// TODO add some checkDDMonster...() spawn rules methods signatures to the interface
	// TODO could implement some basic rules

//	public static boolean checkDDMonsterSpawnRules(EntityType<? extends Mob> mob, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
//		IMobConfig mobConfig = Config.Mobs.MOBS.get(EntityType.getKey(mob));
//		CommonSpawnConfig config = mobConfig.getSpawnConfig();
//		return config.enabled.get()
//				&& level.getDifficulty() != Difficulty.PEACEFUL
//				&& isValidHeight(pos, config)
//				&& isDarkEnoughToSpawn(level, pos, random)
//				&& checkMobSpawnRules(mob, level, spawnType, pos, random);
//	}

//	public static boolean checkDDMonsterCanSeeSkySpawnRules(EntityType<? extends Mob> mob, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
//		Config.IMobConfig mobConfig = Config.Mobs.MOBS.get(EntityType.getKey(mob));
//		Config.CommonSpawnConfig config = mobConfig.getSpawnConfig();
//		return config.enabled.get()
//				&& level.getDifficulty() != Difficulty.PEACEFUL
//				&& isValidHeight(pos, config)
//				&& level.canSeeSky(pos)
//				&& checkMobSpawnRules(mob, level, spawnType, pos, random);
//	}

//	public static boolean isValidHeight(BlockPos pos, CommonSpawnConfig config) {
//		return pos.getY() > config.minHeight.get() && pos.getY() < config.maxHeight.get();
//	}
	
//	public static boolean checkDDMonsterNetherSpawnRules(EntityType<? extends Mob> mob, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
//		IMobConfig mobConfig = Config.Mobs.MOBS.get(EntityType.getKey(mob));
//		NetherSpawnConfig config = ((INetherMobConfig)mobConfig).getNetherSpawn();
//		return config.enabled.get()
//				&& level.getDifficulty() != Difficulty.PEACEFUL
//				&& isValidHeight(pos, config)
//				&& isDarkEnoughToSpawn(level, pos, random)
//				&& checkMobSpawnRules(mob, level, spawnType, pos, random);
//	}

//	public static boolean checkIsNetherSpawnRules(EntityType<? extends Mob> mob, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
//		if (level.getBiome(pos).is(BiomeTags.IS_NETHER) ) {
//			return DenizensMonster.checkDDMonsterNetherSpawnRules(mob, level, spawnType, pos, random);
//		}
//		else {
//			return DenizensMonster.checkDDMonsterSpawnRules(mob, level, spawnType, pos, random);
//		}
//	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		if (this.getSummonedOwner() != null) {
			if (this.getSummonedOwner() instanceof Player) {
				tag.putBoolean(PLAYER_OWNER, true);
			} else {
				tag.putBoolean(PLAYER_OWNER, false);
			}
			tag.putUUID(SUMMONED_OWNER, getSummonedOwner().getUUID());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.hasUUID(SUMMONED_OWNER)) {
			UUID uuid = tag.getUUID(SUMMONED_OWNER);

			LivingEntity owner = null;
			if (tag.contains(PLAYER_OWNER) && tag.getBoolean(PLAYER_OWNER)) {
				owner = this.level().getPlayerByUUID(uuid);
			} else {
				owner = (LivingEntity)((ServerLevel)this.level()).getEntity(uuid);
			}
			this.setSummonedOwner(owner);
		}
	}
}
