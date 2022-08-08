package com.feywild.feywild;

import com.feywild.feywild.block.ModBlocks;
import com.feywild.feywild.block.entity.mana.CapabilityMana;
import com.feywild.feywild.compat.MineMentionCompat;
import com.feywild.feywild.entity.*;
import com.feywild.feywild.entity.model.*;
import com.feywild.feywild.entity.render.*;
import com.feywild.feywild.network.FeywildNetwork;
import com.feywild.feywild.particles.LeafParticle;
import com.feywild.feywild.particles.ModParticles;
import com.feywild.feywild.quest.QuestManager;
import com.feywild.feywild.quest.player.CapabilityQuests;
import com.feywild.feywild.quest.reward.ItemReward;
import com.feywild.feywild.quest.reward.RewardTypes;
import com.feywild.feywild.quest.task.*;
import com.feywild.feywild.sound.FeywildMenuMusic;
import com.feywild.feywild.trade.TradeManager;
import com.feywild.feywild.util.LibraryBooks;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

import javax.annotation.Nonnull;

// UPDATE_TODO Remove all uses of:
//  FeatureUtils.register
//  PlacementUtils.register
//  Registry.register
//  BuiltinRegistries.register

// UPDATE_TODO use TagAccess where possible
// UPDATE_TODO tags
// UPDATE_TODO JEI
// UPDATE_TODO datagen
@Mod("feywild")
public final class FeywildMod extends ModXRegistration {

    public static final Logger logger = LoggerFactory.getLogger("feywild");
    
    private static FeywildMod instance;
    private static FeywildNetwork network;

    public FeywildMod() {
        super(new FeywildTab("feywild"));

        instance = this;
        network = new FeywildNetwork(this);

        GeckoLib.initialize();

        // TODO mythicbotany
//        if (ModList.get().isLoaded("mythicbotany") && CompatConfig.mythic_alfheim.alfheim) {
//            this.addRegistrationHandler(ModAlfheimBiomes::register);
//            FMLJavaModLoadingContext.get().getModEventBus().addListener(ModAlfheimBiomes::setup);
//        }
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CapabilityMana::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CapabilityQuests::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::entityAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerParticles);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::blockColors);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::itemColors);

//        ModStructures.register(eventBus);

        MinecraftForge.EVENT_BUS.addListener(this::reloadData);
//        MinecraftForge.EVENT_BUS.addListener(ModWorldGeneration::loadWorldGeneration);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityQuests::attachPlayerCaps);
        MinecraftForge.EVENT_BUS.addListener(CapabilityQuests::playerCopy);

//        this.addRegistrationHandler(FeywildDimension::register);

        MinecraftForge.EVENT_BUS.register(new EventListener());
//        MinecraftForge.EVENT_BUS.register(new MarketProtectEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(FeywildMenuMusic::playSound));

        // Quest task & reward types. Not in setup as they are required for datagen.
        TaskTypes.register(new ResourceLocation(this.modid, "craft"), CraftTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "fey_gift"), FeyGiftTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "item"), ItemTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "kill"), KillTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "special"), SpecialTask.INSTANCE);
        TaskTypes.register(new ResourceLocation(this.modid, "biome"), BiomeTask.INSTANCE);
//        TaskTypes.register(new ResourceLocation(this.modid, "structure"), StructureTask.INSTANCE);
        RewardTypes.register(new ResourceLocation(this.modid, "item"), ItemReward.INSTANCE);
    }

    @Nonnull
    public static FeywildMod getInstance() {
        return instance;
    }

    @Nonnull
    public static FeywildNetwork getNetwork() {
        return network;
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        //
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
//            ModStructurePieces.setup();

            SpawnPlacements.register(ModEntityTypes.springPixie, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpringPixie::canSpawn);
            SpawnPlacements.register(ModEntityTypes.summerPixie, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SummerPixie::canSpawn);
            SpawnPlacements.register(ModEntityTypes.autumnPixie, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AutumnPixie::canSpawn);
            SpawnPlacements.register(ModEntityTypes.winterPixie, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WinterPixie::canSpawn);
            SpawnPlacements.register(ModEntityTypes.dwarfBlacksmith, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DwarfBlacksmith::canSpawn);
            SpawnPlacements.register(ModEntityTypes.beeKnight, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BeeKnight::canSpawn);
            SpawnPlacements.register(ModEntityTypes.shroomling, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Shroomling::canSpawn);

//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "miner"), ModEntityTypes.dwarfMiner, new BlockPos(11, 64, 20));
//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "baker"), ModEntityTypes.dwarfBaker, new BlockPos(-3, 64, 10));
//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "shepherd"), ModEntityTypes.dwarfShepherd, new BlockPos(0, 63, -3));
//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "artificer"), ModEntityTypes.dwarfArtificer, new BlockPos(7, 63, -2));
//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "dragon_hunter"), ModEntityTypes.dwarfDragonHunter, new BlockPos(21, 63, 20));
//            MarketGenerator.registerMarketDwarf(new ResourceLocation(this.modid, "tool_smith"), ModEntityTypes.dwarfToolsmith, new BlockPos(21, 63, 11));
//
//            FeywildGeneration.setupBiomes();

            if (ModList.get().isLoaded("minemention")) {
                MineMentionCompat.setup();
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntityTypes.beeKnight, BasePixieRenderer.create(BeeKnightModel::new));
        EntityRenderers.register(ModEntityTypes.dwarfToolsmith, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.dwarfArtificer, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.dwarfDragonHunter, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.dwarfBaker, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.dwarfMiner, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.dwarfBlacksmith, DwarfBlacksmithRenderer.create(DwarfBlacksmithModel::new));
        EntityRenderers.register(ModEntityTypes.dwarfShepherd, MarketDwarfRenderer::new);
        EntityRenderers.register(ModEntityTypes.springPixie, BasePixieRenderer.create(SpringPixieModel::new));
        EntityRenderers.register(ModEntityTypes.summerPixie, BasePixieRenderer.create(SummerPixieModel::new));
        EntityRenderers.register(ModEntityTypes.autumnPixie, BasePixieRenderer.create(AutumnPixieModel::new));
        EntityRenderers.register(ModEntityTypes.winterPixie, BasePixieRenderer.create(WinterPixieModel::new));
        EntityRenderers.register(ModEntityTypes.mandragora, MandragoraRenderer.create(MandragoraModel::new));
        EntityRenderers.register(ModEntityTypes.shroomling, ShroomlingRenderer.create(ShroomlingModel::new));
    }

    private void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.springPixie, SpringPixie.getDefaultAttributes().build());
        event.put(ModEntityTypes.summerPixie, SummerPixie.getDefaultAttributes().build());
        event.put(ModEntityTypes.autumnPixie, AutumnPixie.getDefaultAttributes().build());
        event.put(ModEntityTypes.winterPixie, WinterPixie.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfBlacksmith, DwarfBlacksmith.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfArtificer, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfBaker, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfMiner, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfDragonHunter, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfShepherd, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.dwarfToolsmith, MarketDwarf.getDefaultAttributes().build());
        event.put(ModEntityTypes.mandragora, Mandragora.getDefaultAttributes().build());
        event.put(ModEntityTypes.beeKnight, BeeKnight.getDefaultAttributes().build());
        event.put(ModEntityTypes.shroomling, Shroomling.getDefaultAttributes().build());
    }

    public void registerParticles(RegisterParticleProvidersEvent event) {
        event.register(ModParticles.leafParticle, LeafParticle.Factory::new);
        event.register(ModParticles.springLeafParticle, LeafParticle.Factory::new);
        event.register(ModParticles.summerLeafParticle, LeafParticle.Factory::new);
        event.register(ModParticles.winterLeafParticle, LeafParticle.Factory::new);
    }
    
    private void blockColors(RegisterColorHandlersEvent.Block event) {
        // UPDATE_TODO generate models with tint index
        //noinspection SwitchStatementWithTooFewBranches
        BlockColor mossyColor = (state, level, pos, tintIndex) -> switch(tintIndex) {
            case 1 -> level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.get(0.5, 0.5);
            default -> 0xFFFFFF;
        };
        event.register(mossyColor,
                ModBlocks.elvenQuartz.getMossyBrickBlock(), ModBlocks.elvenSpringQuartz.getMossyBrickBlock(),
                ModBlocks.elvenSummerQuartz.getMossyBrickBlock(), ModBlocks.elvenAutumnQuartz.getMossyBrickBlock(),
                ModBlocks.elvenWinterQuartz.getMossyBrickBlock()
        );
    }

    public void itemColors(RegisterColorHandlersEvent.Item event) {
        //noinspection SwitchStatementWithTooFewBranches
        ItemColor mossyColor = (stack, tintIndex) -> switch(tintIndex) {
            case 1 -> GrassColor.get(0.5, 0.5);
            default -> 0xFFFFFF;
        };
        event.register(mossyColor,
                ModBlocks.elvenQuartz.getMossyBrickBlock(), ModBlocks.elvenSpringQuartz.getMossyBrickBlock(),
                ModBlocks.elvenSummerQuartz.getMossyBrickBlock(), ModBlocks.elvenAutumnQuartz.getMossyBrickBlock(),
                ModBlocks.elvenWinterQuartz.getMossyBrickBlock()
        );
    }
    
    public void reloadData(AddReloadListenerEvent event) {
        event.addListener(LibraryBooks.createReloadListener());
        event.addListener(TradeManager.createReloadListener());
        event.addListener(QuestManager.createReloadListener());
    }
}
