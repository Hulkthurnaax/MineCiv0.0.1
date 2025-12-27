package net.andrew.mineciv.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class WoodwandItem extends Item {

    private static final int MAX_SCAN_RADIUS = 10;
    private static final int MAX_HEIGHT = 30;

    private static final Map<Block, Block> LOG_TRANSFORM_MAP = new HashMap<>();
    private static final Map<Block, Block> LEAVES_TRANSFORM_MAP = new HashMap<>();

    static {
        LOG_TRANSFORM_MAP.put(Blocks.ACACIA_LOG, Blocks.BIRCH_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.JUNGLE_LOG, Blocks.MANGROVE_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.MANGROVE_LOG, Blocks.DARK_OAK_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.DARK_OAK_LOG, Blocks.OAK_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.OAK_LOG, Blocks.SPRUCE_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.SPRUCE_LOG, Blocks.CHERRY_LOG);
        LOG_TRANSFORM_MAP.put(Blocks.CHERRY_LOG, Blocks.ACACIA_LOG);

        LEAVES_TRANSFORM_MAP.put(Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.JUNGLE_LEAVES, Blocks.MANGROVE_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.MANGROVE_LEAVES, Blocks.DARK_OAK_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.DARK_OAK_LEAVES, Blocks.OAK_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.SPRUCE_LEAVES, Blocks.CHERRY_LEAVES);
        LEAVES_TRANSFORM_MAP.put(Blocks.CHERRY_LEAVES, Blocks.ACACIA_LEAVES);

    }

    public WoodwandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!level.isClientSide) {
            BlockState clickedState = level.getBlockState(clickedPos);
            Block clickedBlock = clickedState.getBlock();

            // Check if clicked block is a log or leaves
            if (isLog(clickedBlock) || isLeaves(clickedBlock)) {

                // Check if player is sneaking (shift-clicking)
                if (player != null && player.isCrouching()) {
                    // Transform only the single clicked block
                    transformSingleBlock(level, clickedPos);

                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "Block transformed!"
                            ),
                            true
                    );
                } else {
                    // Transform the entire tree
                    Set<BlockPos> treeBlocks = findTree(level, clickedPos);
                    transformTree(level, treeBlocks);

                    if (player != null) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "Tree transformed! Changed " + treeBlocks.size() + " blocks."
                                ),
                                true
                        );
                    }
                }

                // Damage the item by 1 durability
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }

                return InteractionResult.SUCCESS;
            } else {
                // Try to apply bonemeal effect
                if (applyBonemealEffect(level, clickedPos, clickedState, player, context)) {
                    // Damage the item by 1 durability
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }

    private boolean applyBonemealEffect(Level level, BlockPos pos, BlockState state, Player player, UseOnContext context) {
        Block block = state.getBlock();

        // Check if the block can be bonemealed
        if (block instanceof net.minecraft.world.level.block.BonemealableBlock bonemealable) {
            if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                if (bonemealable.isBonemealSuccess(level, level.random, pos, state)) {
                    // Apply the bonemeal effect
                    bonemealable.performBonemeal((net.minecraft.server.level.ServerLevel) level,
                            level.random, pos, state);

                    // Show particles (like bonemeal does)
                    level.levelEvent(2005, pos, 0);

                    if (player != null) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        "Growth boosted!"
                                ),
                                true
                        );
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private Set<BlockPos> findTree(Level level, BlockPos startPos) {
        Set<BlockPos> treeBlocks = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        Set<BlockPos> checked = new HashSet<>();

        toCheck.add(startPos);
        checked.add(startPos);

        while (!toCheck.isEmpty() && treeBlocks.size() < 1000) {
            BlockPos current = toCheck.poll();
            BlockState state = level.getBlockState(current);
            Block block = state.getBlock();

            if (isLog(block) || isLeaves(block)) {
                treeBlocks.add(current);

                // Check all adjacent blocks
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;

                            BlockPos neighbor = current.offset(dx, dy, dz);

                            // Limit search area
                            if (Math.abs(neighbor.getX() - startPos.getX()) > MAX_SCAN_RADIUS ||
                                    Math.abs(neighbor.getZ() - startPos.getZ()) > MAX_SCAN_RADIUS ||
                                    Math.abs(neighbor.getY() - startPos.getY()) > MAX_HEIGHT) {
                                continue;
                            }

                            if (!checked.contains(neighbor)) {
                                checked.add(neighbor);
                                BlockState neighborState = level.getBlockState(neighbor);
                                Block neighborBlock = neighborState.getBlock();

                                if (isLog(neighborBlock) || isLeaves(neighborBlock)) {
                                    toCheck.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }

        return treeBlocks;
    }

    private void transformTree(Level level, Set<BlockPos> treeBlocks) {
        for (BlockPos pos : treeBlocks) {
            transformSingleBlock(level, pos);
        }
    }

    private void transformSingleBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        Block newBlock = null;

        if (isLog(block) && LOG_TRANSFORM_MAP.containsKey(block)) {
            newBlock = LOG_TRANSFORM_MAP.get(block);
        } else if (isLeaves(block) && LEAVES_TRANSFORM_MAP.containsKey(block)) {
            newBlock = LEAVES_TRANSFORM_MAP.get(block);
        }

        if (newBlock != null) {
            BlockState newState = newBlock.defaultBlockState();
            // Preserve block properties like axis for logs
            try {
                for (var property : state.getProperties()) {
                    if (newState.hasProperty(property)) {
                        newState = copyProperty(state, newState, property);
                    }
                }
            } catch (Exception e) {
                // If property copying fails, just use default state
            }

            level.setBlock(pos, newState, 3);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState copyProperty(
            BlockState source, BlockState target, net.minecraft.world.level.block.state.properties.Property<?> property
    ) {
        return target.setValue(
                (net.minecraft.world.level.block.state.properties.Property<T>) property,
                source.getValue((net.minecraft.world.level.block.state.properties.Property<T>) property)
        );
    }

    private boolean isLog(Block block) {
        return LOG_TRANSFORM_MAP.containsKey(block);
    }

    private boolean isLeaves(Block block) {
        return LEAVES_TRANSFORM_MAP.containsKey(block);
    }
}