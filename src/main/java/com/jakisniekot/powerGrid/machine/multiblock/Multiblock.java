package com.jakisniekot.powerGrid.machine.multiblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI BUILD CLASS
 */
public class Multiblock {

    private Map<Character, Material> keys = new HashMap<>();

    // A null entry means "ignore this position" (not part of the check/build).
    // Material.AIR means "this position must actually be air".
    private List<List<List<Material>>> structure = new ArrayList<>();

    private int controllerLayer;
    private int controllerRow;
    private int controllerCol;

    public Multiblock(ConfigurationSection section) {

        ConfigurationSection keys = section.getConfigurationSection("multiblock.keys");

        Material controllerMaterial;
        boolean controllerUsed = false;

        try {
            controllerMaterial = Material.valueOf(section.getString("item.material"));
        } catch (IllegalArgumentException e) {
            controllerMaterial = Material.STONE;
        }

        if (!controllerMaterial.isBlock()) {
            controllerMaterial = Material.STONE;
        }

        for (String key : keys.getKeys(false)) {
            Material material;
            try {
                material = Material.valueOf(keys.getString(key));
            } catch (IllegalArgumentException e) {
                material = Material.AIR;
            }
            this.keys.put(key.charAt(0), material);
        }

        ConfigurationSection structureSection = section.getConfigurationSection("multiblock.structure");
        List<List<List<Material>>> structureList = new ArrayList<>();

        int layerIdx = 0;
        for (String layer : structureSection.getKeys(false)) {

            List<List<Material>> newLayer = new ArrayList<>();
            List<String> lines = structureSection.getStringList(layer);

            for (int rowIdx = 0; rowIdx < lines.size(); rowIdx++) {
                String line = lines.get(rowIdx);
                List<Material> newLine = new ArrayList<>();

                char[] chars = line.toCharArray();
                for (int colIdx = 0; colIdx < chars.length; colIdx++) {
                    char c = chars[colIdx];

                    if (c == ' ') {
                        newLine.add(null); // ignore — not air, just "don't care"
                    } else if (c == '@') {
                        if (controllerUsed) {
                            newLine.add(Material.STONE);
                        } else {
                            newLine.add(controllerMaterial);
                            controllerUsed = true;
                            controllerLayer = layerIdx;
                            controllerRow = rowIdx;
                            controllerCol = colIdx;
                        }
                    } else {
                        // Unknown/unmapped key char also falls back to "ignore"
                        // rather than forcing AIR, since that's a config gap,
                        // not an intentional "must be air" requirement.
                        newLine.add(this.keys.get(c));
                    }
                }
                newLayer.add(newLine);
            }
            structureList.add(newLayer);
            layerIdx++;
        }

        this.structure = structureList;
    }

    public List<List<List<Material>>> getStructure() {
        return structure;
    }

    public void build(Block controllerBlock) {
        BlockFace facing = getFacing(controllerBlock);
        World world = controllerBlock.getWorld();
        Location origin = controllerBlock.getLocation();

        forEachOffset((dy, depth, side, material) -> {
            if (material == null) return; // ignored slot — leave world untouched

            int[] xz = rotate(facing, depth, side);
            Block target = world.getBlockAt(
                    origin.getBlockX() + xz[0],
                    origin.getBlockY() + dy,
                    origin.getBlockZ() + xz[1]
            );
            target.setType(material, false); // Material.AIR here correctly clears the block
        });
    }

    /**
     * Checks whether the multiblock is already correctly built in the world,
     * anchored on the given controller block. Ignored ({@code null}) slots are
     * skipped; every other slot (including explicit AIR) must match exactly.
     */
    public boolean isBuilt(Block controllerBlock) {
        BlockFace facing = getFacing(controllerBlock);
        World world = controllerBlock.getWorld();
        Location origin = controllerBlock.getLocation();

        boolean[] matched = {true};

        forEachOffset((dy, depth, side, material) -> {
            if (!matched[0] || material == null) return;

            int[] xz = rotate(facing, depth, side);
            Block target = world.getBlockAt(
                    origin.getBlockX() + xz[0],
                    origin.getBlockY() + dy,
                    origin.getBlockZ() + xz[1]
            );

            if (target.getType() != material) {
                matched[0] = false;
            }
        });

        return matched[0];
    }

    private BlockFace getFacing(Block controllerBlock) {
        BlockData data = controllerBlock.getBlockData();
        if (data instanceof Directional directional) {
            return directional.getFacing();
        }
        return BlockFace.NORTH;
    }

    @FunctionalInterface
    private interface OffsetVisitor {
        void visit(int dy, int depth, int side, Material material);
    }

    private void forEachOffset(OffsetVisitor visitor) {
        for (int layerIdx = 0; layerIdx < structure.size(); layerIdx++) {
            List<List<Material>> layer = structure.get(layerIdx);
            int dy = layerIdx - controllerLayer;

            for (int rowIdx = 0; rowIdx < layer.size(); rowIdx++) {
                List<Material> row = layer.get(rowIdx);
                int depth = rowIdx - controllerRow;

                for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                    int side = colIdx - controllerCol;
                    if (dy == 0 && depth == 0 && side == 0) continue; // controller position itself

                    visitor.visit(dy, depth, side, row.get(colIdx));
                }
            }
        }
    }

    private int[] rotate(BlockFace facing, int depth, int side) {
        return switch (facing) {
            case NORTH -> new int[]{ side, -depth };
            case SOUTH -> new int[]{ -side, depth };
            case EAST  -> new int[]{ depth, side };
            case WEST  -> new int[]{ -depth, -side };
            default    -> new int[]{ side, depth };
        };
    }
}