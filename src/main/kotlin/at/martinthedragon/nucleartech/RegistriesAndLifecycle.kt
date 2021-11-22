package at.martinthedragon.nucleartech

import at.martinthedragon.nucleartech.capabilites.contamination.CapabilityContaminationHandler
import at.martinthedragon.nucleartech.containers.ContainerTypes
import at.martinthedragon.nucleartech.datagen.*
import at.martinthedragon.nucleartech.entities.EntityTypes
import at.martinthedragon.nucleartech.entities.NuclearCreeperEntity
import at.martinthedragon.nucleartech.items.NuclearSpawnEggItem
import at.martinthedragon.nucleartech.recipes.RecipeSerializers
import at.martinthedragon.nucleartech.recipes.RecipeTypes
import at.martinthedragon.nucleartech.tileentities.TileEntityTypes
import at.martinthedragon.nucleartech.worldgen.WorldGeneration
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.inventory.container.ContainerType
import net.minecraft.item.Item
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.registry.Registry
import net.minecraft.world.gen.feature.Feature
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.RegistryObject
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.ForgeRegistryEntry
import net.minecraftforge.registries.RegistryManager

@Suppress("unused")
@Mod.EventBusSubscriber(modid = NuclearTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
object RegistriesAndLifecycle {
    val BLOCKS: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, NuclearTech.MODID)
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, NuclearTech.MODID)
    val TILE_ENTITIES: DeferredRegister<TileEntityType<*>> = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, NuclearTech.MODID)
    val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister.create(ForgeRegistries.ENTITIES, NuclearTech.MODID)
    val CONTAINERS: DeferredRegister<ContainerType<*>> = DeferredRegister.create(ForgeRegistries.CONTAINERS, NuclearTech.MODID)
    val RECIPE_SERIALIZERS: DeferredRegister<IRecipeSerializer<*>> = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, NuclearTech.MODID)
    val FEATURES: DeferredRegister<Feature<*>> = DeferredRegister.create(ForgeRegistries.FEATURES, NuclearTech.MODID)
    val SOUNDS: DeferredRegister<SoundEvent> = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NuclearTech.MODID)

    init {
        val modEventBus = FMLJavaModLoadingContext.get().modEventBus
        BLOCKS.register(modEventBus)
        ModBlocks
        ITEMS.register(modEventBus)
        ModBlockItems
        ModItems
        TILE_ENTITIES.register(modEventBus)
        TileEntityTypes
        ENTITIES.register(modEventBus)
        EntityTypes
        CONTAINERS.register(modEventBus)
        ContainerTypes
        RECIPE_SERIALIZERS.register(modEventBus)
        RecipeSerializers
        FEATURES.register(modEventBus)
        WorldGeneration.Features
        SOUNDS.register(modEventBus)
        SoundEvents
    }

    // using kotlin's strong type system
    // automatically uses the correct registry for the field
    inline fun <reified T : ForgeRegistryEntry<T>> retrieve(resourceLocation: ResourceLocation): RegistryObject<T> {
        return RegistryObject.of(resourceLocation, RegistryManager.ACTIVE.getRegistry(T::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    @SubscribeEvent @JvmStatic
    fun commonSetup(event: FMLCommonSetupEvent) {
        NuclearTech.LOGGER.info("Hello World!")
        CapabilityContaminationHandler.register()
    }

    @SubscribeEvent @JvmStatic
    fun createAttributes(event: EntityAttributeCreationEvent) {
        event.put(EntityTypes.nuclearCreeperEntity.get(), NuclearCreeperEntity.createAttributes())
    }

    @SubscribeEvent @JvmStatic
    fun generateData(event: GatherDataEvent) {
        val dataGenerator = event.generator
        val existingFileHelper = event.existingFileHelper
        if (event.includeServer()) {
            val blockTagProvider = BlockTagProvider(dataGenerator, existingFileHelper)
            dataGenerator.addProvider(blockTagProvider)
            dataGenerator.addProvider(ItemTagProvider(dataGenerator, blockTagProvider, existingFileHelper))
            dataGenerator.addProvider(NuclearRecipeProvider(dataGenerator))
            dataGenerator.addProvider(NuclearLootProvider(dataGenerator))
        }

        if (event.includeClient()) {
            dataGenerator.addProvider(NuclearBlockStateProvider(dataGenerator, existingFileHelper))
            dataGenerator.addProvider(NuclearItemModelProvider(dataGenerator, existingFileHelper))
            dataGenerator.addProvider(NuclearModelProvider(dataGenerator, existingFileHelper))
            dataGenerator.addProvider(NuclearSoundsProvider(dataGenerator, existingFileHelper))

            for (translation in NuclearLanguageProviders.getLanguageProviders(dataGenerator))
                dataGenerator.addProvider(translation)
        }
    }

    @SubscribeEvent @JvmStatic
    fun registerRecipeTypes(event: RegistryEvent.Register<IRecipeSerializer<*>>) {
        // no forge registry for recipe types currently available
        RecipeTypes.getTypes().forEach { Registry.register(Registry.RECIPE_TYPE, it.toString(), it) }
    }

    @Suppress("UNUSED_PARAMETER")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @JvmStatic
    fun registerSpawnEggs(event: RegistryEvent.Register<EntityType<*>>) {
        NuclearSpawnEggItem.registerSpawnEggEntities()
    }
}
