package com.corgam.cagedmobsreborn.helpers;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * 物品资源句柄包装器，用于限制外部方块（如漏斗）对生物笼内部槽位的读写操作。
 */
public class AdaptedItemHandler implements ResourceHandler<ItemResource> {

    private final ResourceHandler<ItemResource> handler;

    public AdaptedItemHandler(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return handler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return handler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return handler.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return handler.insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return handler.extract(index, resource, amount, transaction);
    }
}
