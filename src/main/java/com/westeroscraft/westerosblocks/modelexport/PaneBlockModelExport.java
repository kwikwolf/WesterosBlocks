package com.westeroscraft.westerosblocks.modelexport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.westeroscraft.westerosblocks.WesterosBlockDef;
import com.westeroscraft.westerosblocks.WesterosBlocks;
import com.westeroscraft.westerosblocks.WesterosBlockDef.Subblock;
import com.westeroscraft.westerosblocks.blocks.WCPaneBlock;

import net.minecraft.block.Block;

public class PaneBlockModelExport extends ModelExport {
    private WesterosBlockDef def;
    private boolean[] legacy_model = new boolean[16];
    private boolean[] bars_model = new boolean[16];

    // Template objects for Gson export of block state
    public static class StateObject {
    	public List<States> multipart = new ArrayList<States>();
    }
    public static class States {
    	public WhenRec when = new WhenRec();
    	public Apply apply = new Apply();
    }
    public static class SideStates extends States {
    	SideStates() {
    		when.OR = new ArrayList<WhenRec>();
    	}
    }
    public static class WhenRec {
    	String variant;
    	Boolean north, south, west, east;
    	public List<WhenRec> OR;
    }
    public static class Apply {
    	String model;
    	Integer x;
    	Integer y;
    	Boolean uvlock;
    }
    // Template objects for Gson export of block models
    public static class ModelObjectPost {
        public String parent = "block/pane_post";    // Use 'post' model for single texture
        public Texture textures = new Texture();
    }
    public static class ModelObjectSide {
        public String parent = "block/pane_side";    // Use 'side' model for single texture
        public Texture textures = new Texture();
    }
    public static class ModelObjectSideAlt {
        public String parent = "block/pane_side_alt";    // Use 'side_alt' model for single texture
        public Texture textures = new Texture();
    }
    public static class ModelObjectNoSide {
        public String parent = "block/pane_noside";    // Use 'side' model for single texture
        public Texture textures = new Texture();
    }
    public static class ModelObjectNoSideAlt {
        public String parent = "block/pane_noside_alt";    // Use 'side_alt' model for single texture
        public Texture textures = new Texture();
    }
    public static class Texture {
        public String edge;
        public String pane;
    }
    public static class TextureLayer0 {
        public String layer0;
    }
    public static class ModelObject {
    	public String parent = "item/generated";
        public TextureLayer0 textures = new TextureLayer0();
    }

    public PaneBlockModelExport(Block blk, WesterosBlockDef def, File dest) {
        super(blk, def, dest);
        this.def = def;
        for (Subblock sb : def.subBlocks) {
            addNLSString("tile." + def.blockName + "_" + sb.meta + ".name", sb.label);
            legacy_model[sb.meta] = ((WCPaneBlock) blk).isLegacyModel(sb.meta);
            bars_model[sb.meta] = ((WCPaneBlock) blk).isBarsModel(sb.meta);
        }
    }
    
    @Override
    public void doBlockStateExport() throws IOException {
        StateObject so = new StateObject();

        for (Subblock sb : def.subBlocks) {
        	boolean is_legacy = legacy_model[sb.meta];
            boolean is_bars = bars_model[sb.meta];
            // Record for when legacy model is needed (all false)
            WhenRec wnone = new WhenRec();
            wnone.north = wnone.south = wnone.east = wnone.west = Boolean.FALSE;
            wnone.variant = Integer.toString(sb.meta);
        	// Add post based on our variant
            if (!is_bars) {
                States ps = new States();
                ps.when.variant = Integer.toString(sb.meta);
                ps.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_post_" + sb.meta;
                so.multipart.add(ps);
            }
        	// Add north variant
        	SideStates ssn = new SideStates();
        	WhenRec wr = new WhenRec();
        	wr.variant = Integer.toString(sb.meta);
        	wr.north = true;
        	ssn.when.OR.add(wr);
        	if (is_legacy || is_bars) ssn.when.OR.add(wnone);
        	ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_side_" + sb.meta;
        	ssn.apply.uvlock = true;
        	so.multipart.add(ssn);
        	// Add east variant
        	ssn = new SideStates();
        	wr = new WhenRec();
        	wr.variant = Integer.toString(sb.meta);
        	wr.east = true;
        	ssn.when.OR.add(wr);
        	if (is_legacy || is_bars) ssn.when.OR.add(wnone);
        	ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_side_" + sb.meta;
        	ssn.apply.uvlock = true;
        	ssn.apply.y = 90;
        	so.multipart.add(ssn);
        	// Add south variant
        	ssn = new SideStates();
        	wr = new WhenRec();
        	wr.variant = Integer.toString(sb.meta);
        	wr.south = true;
        	ssn.when.OR.add(wr);
        	if (is_legacy || is_bars) ssn.when.OR.add(wnone);
        	ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_side_alt_" + sb.meta;
        	ssn.apply.uvlock = true;
        	so.multipart.add(ssn);
        	// Add west variant
        	ssn = new SideStates();
        	wr = new WhenRec();
        	wr.variant = Integer.toString(sb.meta);
        	wr.west = true;
        	ssn.when.OR.add(wr);
        	if (is_legacy || is_bars) ssn.when.OR.add(wnone);
        	ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_side_alt_" + sb.meta;
        	ssn.apply.uvlock = true;
        	ssn.apply.y = 90;
        	so.multipart.add(ssn);
        	if (!is_bars) {
        	    // Handle north not connected variant
        	    ssn = new SideStates();
        	    wr = new WhenRec();
        	    wr.variant = Integer.toString(sb.meta);
        	    wr.north = false;
        	    ssn.when.OR.add(wr);
        	    ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_noside_" + sb.meta;
        	    ssn.apply.uvlock = true;
        	    so.multipart.add(ssn);
        	    // Add east not connected variant
        	    ssn = new SideStates();
        	    wr = new WhenRec();
        	    wr.variant = Integer.toString(sb.meta);
        	    wr.east = false;
        	    ssn.when.OR.add(wr);
        	    ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_noside_alt_" + sb.meta;
        	    ssn.apply.uvlock = true;
        	    so.multipart.add(ssn);
        	    // Add south not connected variant
        	    ssn = new SideStates();
        	    wr = new WhenRec();
        	    wr.variant = Integer.toString(sb.meta);
        	    wr.south = false;
        	    ssn.when.OR.add(wr);
        	    ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_noside_alt_" + sb.meta;
        	    ssn.apply.y = 90;
        	    ssn.apply.uvlock = true;
        	    so.multipart.add(ssn);
        	    // Add west not connected variant
        	    ssn = new SideStates();
        	    wr = new WhenRec();
        	    wr.variant = Integer.toString(sb.meta);
        	    wr.west = false;
        	    ssn.when.OR.add(wr);
        	    ssn.apply.model = WesterosBlocks.MOD_ID + ":" + def.blockName + "_noside_" + sb.meta;
        	    ssn.apply.uvlock = true;
        	    ssn.apply.y = 270;
        	    so.multipart.add(ssn);
        	}
        }
        this.writeBlockStateFile(def.blockName, so);
    }

    @Override
    public void doModelExports() throws IOException {
        for (Subblock sb : def.subBlocks) {
            boolean is_bars = bars_model[sb.meta];
            if (!is_bars) {
                ModelObjectPost mod = new ModelObjectPost();
                mod.textures.pane = getTextureID(sb.getTextureByIndex(0)); 
                mod.textures.edge = getTextureID(sb.getTextureByIndex(1)); 
                this.writeBlockModelFile(def.blockName + "_post_" + sb.meta, mod);
            }
            // Side model
            ModelObjectSide smod = new ModelObjectSide();
            smod.textures.pane = getTextureID(sb.getTextureByIndex(0)); 
            smod.textures.edge = getTextureID(sb.getTextureByIndex(1)); 
            if (is_bars) smod.parent = "westerosblocks:block/untinted/bars_side";
            this.writeBlockModelFile(def.blockName + "_side_" + sb.meta, smod);
            // Side-alt model
            ModelObjectSideAlt samod = new ModelObjectSideAlt();
            samod.textures.pane = getTextureID(sb.getTextureByIndex(0)); 
            samod.textures.edge = getTextureID(sb.getTextureByIndex(1)); 
            if (is_bars) samod.parent = "westerosblocks:block/untinted/bars_side_alt";
            this.writeBlockModelFile(def.blockName + "_side_alt_" + sb.meta, samod);
            if (!is_bars) {
                // NoSide model
                ModelObjectNoSide nsmod = new ModelObjectNoSide();
                nsmod.textures.pane = getTextureID(sb.getTextureByIndex(0)); 
                this.writeBlockModelFile(def.blockName + "_noside_" + sb.meta, nsmod);
                // NoSide-alt model
                ModelObjectNoSideAlt nsamod = new ModelObjectNoSideAlt();
                nsamod.textures.pane = getTextureID(sb.getTextureByIndex(0)); 
                this.writeBlockModelFile(def.blockName + "_noside_alt_" + sb.meta, nsamod);
            }
            // Build simple item model that refers to block model
            ModelObject mo = new ModelObject();
            mo.textures.layer0 = getTextureID(sb.getTextureByIndex(0));
            this.writeItemModelFile(def.blockName + "_" + sb.meta, mo);
        }
    }

}
