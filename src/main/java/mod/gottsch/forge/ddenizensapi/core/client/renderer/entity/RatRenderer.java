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
package mod.gottsch.forge.ddenizensapi.core.client.renderer.entity;

import mod.gottsch.forge.ddenizensapi.core.DDApi;
import mod.gottsch.forge.ddenizensapi.core.client.model.RatModel;
import mod.gottsch.forge.ddenizensapi.core.entity.monster.Rat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Mark Gottschling on Apr 1, 2022
 *
 */
public class RatRenderer<T extends Rat> extends MobRenderer<T, RatModel<T>> {

    // TODO change this to pull from some sort of registry or config or this is just the DEFAULT texture;
    private static final ResourceLocation TEXTURE = new ResourceLocation(DDApi.MOD_ID, "textures/entity/rat.png");

    private ResourceLocation texture = TEXTURE;

	/**
	 *
	 * @param context
	 */
	public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel<>(context.bakeLayer(RatModel.LAYER_LOCATION)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(Rat entity) {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }
}
