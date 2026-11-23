package com.corgam.cagedmobsreborn.serializers.entity;

import com.corgam.cagedmobsreborn.CagedMobsReborn;
import com.corgam.cagedmobsreborn.serializers.SerializationHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public class LootData {

    public static final Codec<LootData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(
            Ingredient.CODEC.fieldOf("output").forGetter(LootData::getItem),
            Ingredient.CODEC.optionalFieldOf("output_cooked", Ingredient.of(Items.AIR)).forGetter(LootData::getCookedItem),
            Codec.FLOAT.fieldOf("chance").forGetter(LootData::getChance),
            Codec.INT.fieldOf("minAmount").forGetter(LootData::getMinAmount),
            Codec.INT.fieldOf("maxAmount").forGetter(LootData::getMaxAmount),
            Codec.BOOL.fieldOf("lightning").orElse(false).forGetter(LootData::isLighting),
            Codec.BOOL.fieldOf("needsArrow").orElse(false).forGetter(LootData::isArrow),
            Codec.INT.fieldOf("color").orElse(-1).forGetter(LootData::getColor),
            Codec.BOOL.fieldOf("randomDurability").orElse(false).forGetter(LootData::ifRandomDurability),
            Codec.STRING.fieldOf("nbtName").orElse("").forGetter(LootData::getNbtName),
            Codec.STRING.fieldOf("nbtData").orElse("").forGetter(LootData::getNbtData)
    ).apply(builder, LootData::new));

    private final float chance;
    private Ingredient item;
    private final Ingredient cookedItem;
    private final int minAmount;
    private final int maxAmount;
    private final boolean lighting;
    private final boolean arrow;
    private final int color;
    private final boolean randomDurability;
    private final String nbtName;
    private final String nbtData;

    public LootData(Ingredient item, Ingredient cookedItem, float chance, int min, int max, boolean lighting, boolean arrow, int color, boolean randomDurability, String nbtName, String nbtData) {
        this.chance = chance;
        this.item = item;
        this.cookedItem = cookedItem;
        this.minAmount = min;
        this.maxAmount = max;
        this.lighting = lighting;
        this.arrow = arrow;
        this.color = color;
        this.randomDurability = randomDurability;
        this.nbtName = nbtName;
        this.nbtData = nbtData;
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException("Amounts must not be negative!");
        }
        if (min > max) {
            throw new IllegalArgumentException("Min amount must not be greater than max amount!");
        }
        // 为掉落物品写入对应的原版数据组件或自定义数据（先拷贝避免污染原始 Ingredient 缓存）
        if (!nbtName.isEmpty() && !nbtData.isEmpty() && item.getItems().length > 0) {
            ItemStack newItem = writeNBTtoItem(nbtName, nbtData, item.getItems()[0].copy());
            this.item = Ingredient.of(newItem);
        }
    }

    public static void serializeBuffer(RegistryFriendlyByteBuf buffer, LootData lootData) {
        buffer.writeFloat(lootData.getChance());
        ItemStack.STREAM_CODEC.encode(buffer, lootData.getItem().getItems()[0]);
        buffer.writeInt(lootData.getMinAmount());
        buffer.writeInt(lootData.getMaxAmount());
        buffer.writeBoolean(lootData.isLighting());
        buffer.writeBoolean(lootData.isArrow());
        final boolean hasCookedItem = !lootData.getCookedItem().isEmpty();
        buffer.writeBoolean(hasCookedItem);
        if (hasCookedItem) {
            ItemStack.STREAM_CODEC.encode(buffer, lootData.getCookedItem().getItems()[0]);
        }
        buffer.writeInt(lootData.getColor());
        buffer.writeBoolean(lootData.ifRandomDurability());
        buffer.writeUtf(lootData.getNbtName());
        buffer.writeUtf(lootData.getNbtData());
    }

    public static LootData deserializeBuffer(RegistryFriendlyByteBuf buffer) {
        final float chance = buffer.readFloat();
        final Ingredient item = Ingredient.of(ItemStack.STREAM_CODEC.decode(buffer));
        final int min = buffer.readInt();
        final int max = buffer.readInt();
        final boolean isLightning = buffer.readBoolean();
        final boolean isArrow = buffer.readBoolean();
        final Ingredient cookedItem = buffer.readBoolean()
                ? Ingredient.of(ItemStack.STREAM_CODEC.decode(buffer))
                : Ingredient.EMPTY;
        final int color = buffer.readInt();
        final boolean randomDurability = buffer.readBoolean();
        final String nbtName = buffer.readUtf();
        final String nbtData = buffer.readUtf();

        return new LootData(item, cookedItem, chance, min, max, isLightning, isArrow, color, randomDurability, nbtName, nbtData);
    }

    // 在 1.21.1 DataComponents 体系下将药水与山羊角映射到原版数据组件，其余键保留已有自定义数据后写入
    public static ItemStack writeNBTtoItem(String nbtName, String nbtData, ItemStack stack) {
        if ("Potion".equals(nbtName)) {
            ResourceLocation id = ResourceLocation.tryParse(nbtData);
            if (id != null) {
                Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION.getHolder(id);
                if (potion.isPresent()) {
                    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion.get()));
                    CagedMobsReborn.LOGGER.debug("[LOOT-DATA] Applied POTION_CONTENTS={} to {}", nbtData, stack.getItem());
                    return stack;
                }
            }
        } else if ("instrument".equals(nbtName)) {
            ResourceLocation id = ResourceLocation.tryParse(nbtData);
            if (id != null) {
                Optional<Holder.Reference<Instrument>> instrument = BuiltInRegistries.INSTRUMENT.getHolder(id);
                if (instrument.isPresent()) {
                    stack.set(DataComponents.INSTRUMENT, instrument.get());
                    CagedMobsReborn.LOGGER.debug("[LOOT-DATA] Applied INSTRUMENT={} to {}", nbtData, stack.getItem());
                    return stack;
                }
            }
        }
        CompoundTag nbt = SerializationHelper.getCustomData(stack);
        nbt.putString(nbtName, nbtData);
        SerializationHelper.setCustomData(stack, nbt);
        CagedMobsReborn.LOGGER.debug("[LOOT-DATA] Applied custom data {}={} to {}", nbtName, nbtData, stack.getItem());
        return stack;
    }

    @Override
    public String toString() {
        return "Loot data - item: " + this.item.toString() + ", chance: " + this.chance + ", min: " + this.minAmount + ", max: " + this.maxAmount;
    }

    public float getChance() {
        return this.chance;
    }

    public Ingredient getItem() {
        return this.item;
    }

    public Ingredient getCookedItem() {
        return this.cookedItem;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public boolean isLighting() {
        return this.lighting;
    }

    public boolean isCooking() {
        return !this.cookedItem.isEmpty();
    }

    public boolean isArrow() {
        return this.arrow;
    }

    public boolean hasColor() {
        return this.getColor() != -1;
    }

    public int getColor() {
        return this.color;
    }

    public boolean ifRandomDurability() {
        return this.randomDurability;
    }

    public String getNbtName() {
        return this.nbtName;
    }

    public String getNbtData() {
        return this.nbtData;
    }
}
