package com.westeroscraft.westerosblocks.tileentity;

import java.util.List;
import java.util.Random;

import com.westeroscraft.westerosblocks.blocks.WCSoundBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WCTileEntitySound extends TileEntity implements ITickable
{
    // Index of sound in blocks sound list
    public byte soundIndex;
    // Base period for periodic playback (in seconds): 0 = no periodic playback
    public int playback_period = 0;
    // Random addition range for random playback (in seconds): 0 = no randomness on periodic playback
    public int random_playback_addition = 0;
    // Start/end time for playback (0=6:00 AM, 12000=6:00PM)
    public int startTime = 0;
    public int endTime = 0;
            
    public boolean previousRedstoneState;
    public int nextPlaybackTimer = 0;
    private boolean initDone = false;
    private static Random rnd = new Random();

    /**
     * Writes a tile entity to NBT.
     * @return 
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (!initDone) {
            initBlock();
        }
        int[] val = new int[4];
        val[0] = playback_period;
        val[1] = random_playback_addition;
        val[2] = soundIndex & 0x7F;
        val[3] = (startTime << 16) | endTime;
        nbt.setIntArray("val", val);
        return nbt;
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        int[] val = nbt.getIntArray("val");
        if ((val != null) && (val.length >= 4)) {
            playback_period = val[0];
            random_playback_addition = val[1];
            soundIndex = (byte) (val[2] & 0x7F);
            startTime = (val[3] >> 16) & 0xFFFF;
            endTime = val[3] & 0xFFFF;
        }
        initDone = true;
    }

    /**
     * change sound selection
     */
    public void changeSoundSelection(WCSoundBlock sb, int meta)
    {
        List<String> sndids = sb.getWBDefinition().getSoundIDList(meta);
        if ((sndids == null) || sndids.isEmpty()) {
            this.soundIndex = (byte)-1;
        }
        else {
            this.soundIndex = (byte)((this.soundIndex + 1) % sndids.size());
        }
        this.markDirty();
    }

    /**
     * plays the stored note
     */
    public void triggerSound(WCSoundBlock sb, World world, BlockPos pos)
    {
        world.addBlockEvent(pos, sb, 0, this.soundIndex);
        nextPlaybackTimer = 0; // Reset timer for next automatic sound playback
    }
    
    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    @Override
    public void update() {
        if (!initDone) {
            initBlock();
        }
        if (playback_period <= 0) {
            return;
        }
        nextPlaybackTimer--;    // Decrement timer
        if (nextPlaybackTimer > 0) {
            return;
        }
        if (nextPlaybackTimer == 0) { // Not first trigger: trigger sound
            long wt = (this.world.getWorldTime() % 24000);
            if (wt < 0) wt += 24000;
            boolean trigger = true;
            if (this.startTime >= this.endTime) { // Split across 0
                trigger = ((wt < this.endTime) || (wt >= this.startTime));// And between end and start
            }
            else {
                trigger = ((wt >= this.startTime) && (wt < this.endTime));
            }
            if (trigger) {
                Block b = this.world.getBlockState(this.pos).getBlock();
                if (b instanceof WCSoundBlock) {    
                    this.triggerSound((WCSoundBlock) b, this.world, this.pos);
                }
            }
        }
        nextPlaybackTimer = playback_period + rnd.nextInt(random_playback_addition + 1);
    }
    private void initBlock() {
        IBlockState state = this.world.getBlockState(this.pos);
        WCSoundBlock sb = (WCSoundBlock) state.getBlock();
        int meta = sb.getMetaFromState(state);
        this.playback_period = sb.def_period_by_meta[meta];
        this.random_playback_addition = sb.def_addition_by_meta[meta];
        this.startTime = sb.def_starttime_by_meta[meta];
        this.endTime = sb.def_endtime_by_meta[meta];
        initDone = true;
    }
}
