package com.github.atelieramber.impureworld.blocks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.atelieramber.impureworld.ImpureWorld;
import com.github.atelieramber.impureworld.blocks.PollutedAir;
import com.github.atelieramber.impureworld.lists.TileEntityTypes;
import com.github.atelieramber.impureworld.materials.ModMaterials;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityPollutedAir extends TileEntity implements ITickableTileEntity {
	protected static final Logger LOGGER = LogManager.getLogger(ImpureWorld.MODID);
	protected IProfiler profiler;
	
	private class AirComposition {
		private float clean = 1.0f;
		private double carbon = 0.0;
		private double sulfur = 0.0;
		private double particulate = 0.0;

		public AirComposition(double c, double s, double p) {
			setComposition(c, s, p);
		}

		public void carbonize(double amount) {
			carbon = (carbon + amount) / 2.0;
			rebalance();
		}

		public void sulfurize(double amount) {
			sulfur = (sulfur + amount) / 2.0;
			rebalance();
		}

		public void particulize(double amount) {
			particulate = (particulate + amount) / 2.0;
			rebalance();
		}

		public void setComposition(double c, double s, double p) {
			carbon = c;
			sulfur = s;
			particulate = p;
			rebalance();
		}

		public void setComposition(AirComposition newComp) {
			setComposition(newComp.carbon, newComp.sulfur, newComp.particulate);
		}

		public void pollute(AirComposition other) {
			pollute(other.carbon, other.sulfur, other.particulate);
		}

		public void pollute(double c, double s, double p) {
			carbon += c;
			sulfur += s;
			particulate += p;
			rebalance();
		}

		public void polluteDxDt(double cAvg, double sAvg, double pAvg, double ratio) {
			carbon += ((cAvg - carbon) * ratio);
			sulfur += ((sAvg - sulfur) * ratio);
			particulate += ((pAvg - particulate) * ratio);
			rebalance();
		}

		private void rebalance() {
			double total = carbon + sulfur + particulate;
			clean = 1.0f - (float) total;
			if (clean < 0.0f) {
				// TODO: Implement compression
				carbon /= total;
				sulfur /= total;
				particulate /= total;
				clean = 0.0f;
			}
		}

		public float purity() {
			return clean;
		}

		public void purify() {
			purify(0.0, 0.0, 0.0);
		}

		public void purify(AirComposition difference) {
			purify(difference.carbon, difference.sulfur, difference.particulate);
		}

		public void purify(double c, double s, double p) {
			setComposition(carbon - c, sulfur - s, particulate - p);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AirComposition) {
				AirComposition other = (AirComposition) obj;
				return (other.carbon == this.carbon && other.sulfur == this.sulfur
						&& other.particulate == this.particulate);
			}
			return super.equals(obj);
		}

		public boolean greaterThan(AirComposition other) {
			return other.clean > this.clean;
		}

		public boolean lessThan(AirComposition other) {
			return other.clean < this.clean;
		}

		public void scale(double amount) {
			setComposition(carbon * amount, sulfur * amount, particulate * amount);
		}

		public AirComposition difference(AirComposition other) {
			return new AirComposition(this.carbon - other.carbon, this.sulfur - other.sulfur,
					this.particulate - other.particulate);
		}

		public float pollutedDifference(AirComposition other) {
			return this.clean - other.clean;
		}
	}

	private AirComposition airComposition = new AirComposition(0.0, 0.0, 0.0);

	private boolean neighborUpdated = true;
	
	private boolean initialized = true;

	private static int MIN_TICK_FREQUENCY = 40;
	private static int MAX_ADDITIONAL_TICK_FREQUENCY = 64;
	private int spreadFrequency = 0;
	private int spreadTimer = 0;

	public TileEntityPollutedAir() {
		this(TileEntityTypes.POLLUTED_AIR.get());
	}
	
	public TileEntityPollutedAir(final TileEntityType<?> tileEntityTypeIn) {
		this(tileEntityTypeIn, 0.0, 0.0, 0.0);
	}
	
	public TileEntityPollutedAir(final TileEntityType<?> tileEntityTypeIn, AirComposition composition) {
		this(tileEntityTypeIn, composition.carbon, composition.carbon, composition.carbon);
	}

	public TileEntityPollutedAir(final TileEntityType<?> tileEntityTypeIn, double carbon, double sulfur, double particulate) {
		super(tileEntityTypeIn);
		airComposition.setComposition(carbon, sulfur, particulate);
		initialized = false;
	}
	
	public void setComposition(double carbon, double sulfur, double particulate) {
		airComposition.setComposition(carbon, sulfur, particulate);
	}

	protected static final List<Direction> SIDES = Collections.unmodifiableList(Arrays.asList(Direction.WEST,
			Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN));

	private enum Spreadability {
		NONE(0), REPLACE(1), OVERLAY(2), PASSTHROUGH(3);

		public int value;

		private Spreadability(int i) {
			value = i;
		}
	}

	protected static final Map<Block, Spreadability> SPREADABLE_BLOCKS = ImmutableMap.of(Blocks.AIR,
			Spreadability.REPLACE, Blocks.CAVE_AIR, Spreadability.REPLACE);

	public void onReplaced(World worldIn, BlockState oldState, BlockState newState) {
		if (newState.getMaterial() != Material.AIR || newState.getMaterial() != Material.GLASS) {

		}
	}

	@Override
	public void tick() {
		if(!initialized) {
			init();
			initialized = true;
		}
		if(profiler == null) {
			profiler = world.getProfiler();
		}
		profiler.startSection("polluted_air:TE");
		if (!world.isAreaLoaded(pos, 1))
			return;
		++spreadTimer;
		if (spreadTimer >= getSpreadFrequency()) {
			profiler.startSection("polluted_air:TE.spread");
			if (neighborUpdated) {
				List<Pair<Direction, Spreadability>> spreadDirections = getSpreadDirs();
				if (spreadDirections.size() > 0) {
					float scale = spreadToSides(spreadDirections);
					this.airComposition.scale(scale);
					world.notifyBlockUpdate(pos, getBlockState(), world.getBlockState(pos), 3);
				}

				neighborUpdated = false;
			}
			profiler.endStartSection("polluted_air:TE.update");
			/* Composition update */
			updateComposition();
			profiler.endStartSection("polluted_air:update");
			markDirty();
			((PollutedAir)(getBlockState().getBlock())).updateImpurity(world, pos, getBlockState(), getImpurity());
			spreadTimer = 0;
			profiler.endSection();
		}
		profiler.endSection();
	}
	
	private void init() {
		((PollutedAir)(getBlockState().getBlock())).updateImpurity(world, pos, getBlockState(), getImpurity());
		profiler = world.getProfiler();
	}

	private int getSpreadFrequency() {
		if (spreadFrequency == 0) {
			if (world != null) {
				if (world.rand != null) {
					spreadFrequency = MIN_TICK_FREQUENCY + world.rand.nextInt(MAX_ADDITIONAL_TICK_FREQUENCY);
				} else {
					Random random = new Random();
					spreadFrequency = MIN_TICK_FREQUENCY + random.nextInt(MAX_ADDITIONAL_TICK_FREQUENCY);
				}
			} else {
				spreadFrequency = MIN_TICK_FREQUENCY;
			}
		}
		return spreadFrequency;
	}

	private class EmissionMergeData{
		public double carbon = 0.0;
		public double sulfur = 0.0;
		public double particulate = 0.0;
		public int totalDirs = 0;
		TileEntityPollutedAir lastInstance;
	}
	
	private void updateComposition() {
		EmissionMergeData mergeData = getMergeTotals();

		if (mergeData.totalDirs > 1) {
			airComposition.polluteDxDt(mergeData.carbon / mergeData.totalDirs, mergeData.sulfur / mergeData.totalDirs,
					mergeData.particulate / mergeData.totalDirs, .25);
		} else if (mergeData.totalDirs > 0) {
			airComposition.setComposition(mergeData.carbon / 2.0, mergeData.sulfur / 2.0, mergeData.particulate / 2.0);
			mergeData.lastInstance.airComposition.setComposition(airComposition);
		}
	}

	private EmissionMergeData getMergeTotals() {
		EmissionMergeData neighborData = new EmissionMergeData();
		for (int i = 0; i < SIDES.size(); ++i) {
			BlockPos offset = pos.offset(SIDES.get(i));
			TileEntity te = world.getTileEntity(offset);
			if (te != null && te instanceof TileEntityPollutedAir) {
				TileEntityPollutedAir air = (TileEntityPollutedAir)te;
				neighborData.carbon += air.airComposition.carbon;
				neighborData.sulfur += air.airComposition.sulfur;
				neighborData.particulate += air.airComposition.particulate;
				++neighborData.totalDirs;
				neighborData.lastInstance = air;
			}
		}
		return neighborData;
	}

	//TODO: Cache spread directions
	private List<Pair<Direction, Spreadability>> getSpreadDirs() {
		List<Pair<Direction, Spreadability>> spreadDirections = new ArrayList<Pair<Direction, Spreadability>>();
		for (int i = 0; i < SIDES.size(); ++i) {
			BlockPos offset = pos.offset(SIDES.get(i));
			Spreadability spread = canSpreadTo(offset);
			if (spread.value > 0) {
				spreadDirections.add(Pair.of(SIDES.get(i), spread));
			}
		}
		return spreadDirections;
	}
	
	private float spreadToSides(List<Pair<Direction, Spreadability>> sides) {
		if (airComposition.purity() < .95f) {
			float spreadPerBlock = 1.0f / (float) (sides.size() + 1);
			for (int i = 0; i < sides.size(); ++i) {
				Pair<Direction, Spreadability> pair = sides.get(i);
				spreadTo(pair.getRight(), spreadPerBlock, pos.offset(pair.getLeft()));
			}
			return spreadPerBlock;
		}
		return 1.0f;
	}

	private boolean spreadTo(Spreadability spreadability, float percentage, BlockPos blockPos) {
		switch (spreadability) {
		case OVERLAY:

			break;
		case PASSTHROUGH:

			break;
		case REPLACE:
			if (world.setBlockState(blockPos, getBlockState().getBlock().getDefaultState(), 3)) {
				TileEntityPollutedAir newTE = (TileEntityPollutedAir) world.getTileEntity(blockPos);

				if (newTE != null) {
					newTE.airComposition.setComposition(airComposition.carbon * percentage,
							airComposition.sulfur * percentage, airComposition.particulate * percentage);
				}
			} else {
				return false;
			}
			break;
		case NONE:
		default:
			return false;
		}
		return true;
	}
	private Spreadability canSpreadTo(BlockPos blockPos) {
		Block blockAt = world.getBlockState(blockPos).getBlock();
		return SPREADABLE_BLOCKS.getOrDefault(blockAt, Spreadability.NONE);
	}

	public void onNeighborStateUpdated(BlockPos neighbor) {
		neighborUpdated = true;
	}

	public float getPurity() {
		return airComposition.purity();
	}
	public float getImpurity() {
		return 1.0f-airComposition.purity();
	}


	@Override
	public String toString() {
		return "Air Composition of Targetted Block :" + "\n  Carbon:      " + airComposition.carbon
				+ "\n  Sulfur:       " + airComposition.sulfur + "\n  Particulate: " + airComposition.particulate
				+ "\n  Clean: " + airComposition.clean;
	}

	/* Handle Server NBT Updates */
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt.putDouble("carbon", this.airComposition.carbon);
		nbt.putDouble("sulfur", this.airComposition.sulfur);
		nbt.putDouble("particulate", this.airComposition.particulate);

		nbt.putInt("spreadFrequency", this.spreadFrequency);
		nbt.putInt("spreadTimer", this.spreadTimer);
		return super.write(nbt);
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		readToNBT(nbt);
	}
	
	private void readToNBT(CompoundNBT nbt) {
		this.airComposition.carbon = nbt.getDouble("carbon");
		this.airComposition.sulfur = nbt.getDouble("sulfur");
		this.airComposition.particulate = nbt.getDouble("particulate");
		this.airComposition.rebalance();

		this.spreadFrequency = nbt.getInt("spreadFrequency");
		this.spreadTimer = nbt.getInt("spreadTimer");
		
		this.neighborUpdated = true;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), -1, this.getUpdateTag());
	}
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readToNBT(pkt.getNbtCompound());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		read(state, tag);
	}
	
	@Override
	public CompoundNBT getTileData() {
		return getUpdateTag();
	}
	/********************/
}
