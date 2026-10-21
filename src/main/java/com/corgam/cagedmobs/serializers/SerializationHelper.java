package com.corgam.cagedmobsreborn.serializers;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

public class SerializationHelper {

    public static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean hasCustomData(ItemStack stack) {
        return !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).isEmpty();
    }

    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    //String collection
    public static void serializeStringCollection(RegistryFriendlyByteBuf buffer, List<String> categories) {
        buffer.writeInt(categories.size());
        for(String s : categories){
            buffer.writeUtf(s);
        }
    }

    public static void deserializeStringCollection(RegistryFriendlyByteBuf buffer, List<String> categories) {
        final int len = buffer.readInt();
        for(int i=0 ; i < len ; i++){
            categories.add(buffer.readUtf());
        }
    }

    // Block

    public static void serializeBlock(RegistryFriendlyByteBuf buffer, Block renderBlock) {
        String locationString = BuiltInRegistries.BLOCK.getKey(renderBlock).toString();
        buffer.writeUtf(locationString);
    }

    public static Block deserializeBlock(RegistryFriendlyByteBuf buffer) {
        String locationString = buffer.readUtf();
        ResourceLocation location = ResourceLocation.parse(locationString);
        return BuiltInRegistries.BLOCK.get(location);
    }

    // Entity Type

    public static CompoundTag serializeEntityTypeNBT(CompoundTag nbt, EntityType<?> entityType) {
        nbt.putString("entity", EntityType.getKey(entityType).toString());
        return nbt;
    }

    public static EntityType<?> deserializeEntityTypeNBT(CompoundTag nbt) {
        // Prepare the resourceLocation
        String resString = nbt.getString("entity");
        if(resString.isEmpty()){return null;}
        String[] splitted = resString.split(":");
        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(splitted[0], splitted[1]);
        // Search for the entity type in the registry and return it
        return BuiltInRegistries.ENTITY_TYPE.get(res);
    }
}
