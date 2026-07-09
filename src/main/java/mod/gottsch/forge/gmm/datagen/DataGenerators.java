
package mod.gottsch.forge.gmm.datagen;

import mod.gottsch.forge.gmm.core.GMM;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/**
 * 
 * @author Mark Gottschling on 11/6/2025
 *
 */
@Mod.EventBusSubscriber(modid = GMM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        
        if (event.includeServer()) {
//            generator.addProvider(true, new Recipes(output));
        	GMMBlockTagsProvider blockTags = new GMMBlockTagsProvider(output, lookupProvider, event.getExistingFileHelper());
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new GMMItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), event.getExistingFileHelper()));
            generator.addProvider(true, new GMMEntityTypeTagsProvider(output, lookupProvider, event.getExistingFileHelper()));
//            generator.addProvider(true, new TreasureBiomeTagsProvider(output, lookupProvider, event.getExistingFileHelper()));
//            generator.addProvider(true, new TreasureWorldGenProvider(output, lookupProvider));
//            generator.addProvider(true, TreasureLootTableProvider.create(output));
        }
        if (event.includeClient()) {
//            generator.addProvider(true, new TreasureBlockStateProvider(output, event.getExistingFileHelper()));
//            generator.addProvider(true, new ItemModelsProvider(output, event.getExistingFileHelper()));
//            generator.addProvider(true, new LanguageGen(output, "en_us"));
//            generator.addProvider(true, new JapaneseLanguageGen(output, "ja_jp"));
        }

        // This is where you add your custom TagsProvider.
        // It's crucial to pass the correct parameters from the event and link the dependencies.
//        generator.addProvider(
//                event.includeServer(),
//                new TreasureRarityTagsProvider(
//                        output,
//                        event.getLookupProvider(),
////                            event.getLookupProvider().thenApply(p -> TagKey.create(Registration.RARITIES_REGISTRY_KEY, new ResourceLocation(Treasure.MODID, "example_tag"))),
//                        event.getExistingFileHelper()
//                )
//
//        );
    }
}