/*
 * This file is part of Treasure2.
 * Copyright (c) 2025 Mark Gottschling (gottsch)
 *
 * Treasure2 is free software: you can redistribute it and/or modify
 * it under the terms of the Open Software Licence 3.0.
 *
 * Treasure2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Open Software Licence 3.0 for more details.
 *
 * You should have received a copy of the Open Software Licence
 * along with Treasure2. If not, see <https://www.tldrlegal.com/license/open-software-licence-3-0>.
 */
package mod.gottsch.forge.gmm.datagen;

import mod.gottsch.forge.gmm.core.GMM;
import mod.gottsch.forge.gmm.core.tag.GMMTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GMMItemTagsProvider extends ItemTagsProvider {
	public GMMItemTagsProvider(PackOutput output, CompletableFuture<Provider> lookup,
							   CompletableFuture<TagLookup<Block>> blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookup, blockTagProvider, GMM.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider provider) {
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.ROTTEN_FLESH);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.BEEF);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.CHICKEN);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.MUTTON);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.RABBIT);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COD);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.SALMON);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.TROPICAL_FISH);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_BEEF);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_CHICKEN);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_MUTTON);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_RABBIT);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_COD);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.COOKED_SALMON);

		tag(GMMTags.Items.GHOUL_FOOD).add(Items.BONE);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.LEATHER);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.RABBIT_HIDE);
		tag(GMMTags.Items.GHOUL_FOOD).add(Items.FEATHER);
	}
}
