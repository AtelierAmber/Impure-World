package com.github.atelieramber.impureworld.blocks.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.github.atelieramber.impureworld.lists.TileEntityTypes;
import com.github.atelieramber.impureworld.materials.ModMaterials;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityPollutedAir extends TileEntity implements ITickableTileEntity {
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
	}
	
	public void setComposition(double d, double e, double f) {
		airComposition.setComposition(d, e, f);
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
			Spreadability.REPLACE);

	public void onReplaced(World worldIn, BlockState oldState, BlockState newState) {
		if (newState.getMaterial() != Material.AIR || newState.getMaterial() != Material.GLASS) {

		}
	}

	@Override
	public void tick() {
		if (!world.isAreaLoaded(pos, 2))
			return;
		++spreadTimer;
		if (spreadTimer >= getSpreadFrequency()) {
			if (neighborUpdated) {
				List<Pair<Direction, Spreadability>> spreadDirections = getSpreadDirs();
				if (spreadDirections.size() > 0) {
					this.airComposition.scale(spreadToSides(spreadDirections));
					markDirty();
					world.notifyBlockUpdate(pos, getBlockState(), world.getBlockState(pos), 3);
				}

				neighborUpdated = false;
			}
			/* Composition update */
			updateComposition();
			spreadTimer = 0;
		}
	}

	private int getSpreadFrequency() {
		if (spreadFrequency == 0) {
			if (world != null) {
				if (world.rand != null) {
					spreadFrequency = 20 + world.rand.nextInt(64);
				} else {
					Random random = new Random(world.getSeed());
					spreadFrequency = 20 + random.nextInt(64);
				}
			} else {
				spreadFrequency = 20;
			}
		}
		return spreadFrequency;
	}

	private void updateComposition() {
		List<Direction> mergeDirs = getMergeDirs();
		double carbonTotal = 0.0f;
		double sulfurTotal = 0.0f;
		double particulateTotal = 0.0f;

		if (mergeDirs.size() > 1) {
			for (int i = 0; i < mergeDirs.size(); ++i) {
				BlockPos offset = pos.offset(mergeDirs.get(i));
				BlockState state = world.getBlockState(offset);
				if (state.getMaterial() == ModMaterials.POLLUTED_AIR) {
					TileEntityPollutedAir te = (TileEntityPollutedAir) world.getTileEntity(offset);

					carbonTotal += te.airComposition.carbon;
					sulfurTotal += te.airComposition.sulfur;
					particulateTotal += te.airComposition.particulate;
				}
			}

			airComposition.polluteDxDt(carbonTotal / (double) mergeDirs.size(), sulfurTotal / (double) mergeDirs.size(),
					particulateTotal / (double) mergeDirs.size(), .25);
		} else if (mergeDirs.size() > 0) {
			BlockPos offset = pos.offset(mergeDirs.get(0));
			BlockState state = world.getBlockState(offset);
			if (state.getMaterial() == ModMaterials.POLLUTED_AIR) {
				TileEntityPollutedAir te = (TileEntityPollutedAir) world.getTileEntity(offset);

				carbonTotal = airComposition.carbon + te.airComposition.carbon;
				sulfurTotal = airComposition.sulfur + te.airComposition.sulfur;
				particulateTotal = airComposition.particulate + te.airComposition.particulate;

				airComposition.setComposition(carbonTotal / 2.0, sulfurTotal / 2.0, particulateTotal / 2.0);
				te.airComposition.setComposition(airComposition);
			}
		}
	}

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

	private List<Direction> getMergeDirs() {
		List<Direction> mergeDirections = new ArrayList<Direction>();
		for (int i = 0; i < SIDES.size(); ++i) {
			BlockPos offset = pos.offset(SIDES.get(i));
			if (world.getBlockState(offset).getMaterial() == ModMaterials.POLLUTED_AIR) {
				mergeDirections.add(SIDES.get(i));
			}
		}
		return mergeDirections;
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

	public void onNeighborStateUpdated(BlockPos neighbor) {
		neighborUpdated = true;
	}

	public float getPurity() {
		return airComposition.purity();
	}

	private Spreadability canSpreadTo(BlockPos blockPos) {
		Block blockAt = world.getBlockState(blockPos).getBlock();
		return SPREADABLE_BLOCKS.getOrDefault(blockAt, Spreadability.NONE);
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
		nbt.putFloat("clean", this.airComposition.clean);
		nbt.putDouble("carbon", this.airComposition.carbon);
		nbt.putDouble("sulfur", this.airComposition.sulfur);
		nbt.putDouble("particulate", this.airComposition.particulate);

		nbt.putInt("spreadFrequency", this.spreadFrequency);
		nbt.putInt("spreadTimer", this.spreadTimer);

		nbt.putBoolean("neighborUpdated", this.neighborUpdated);
		return super.write(nbt);
	}

	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);

		this.airComposition.clean = nbt.getFloat("clean");
		this.airComposition.carbon = nbt.getDouble("carbon");
		this.airComposition.sulfur = nbt.getDouble("sulfur");
		this.airComposition.particulate = nbt.getDouble("particulate");

		this.spreadFrequency = nbt.getInt("spreadFrequency");
		this.spreadTimer = nbt.getInt("spreadTimer");

		this.neighborUpdated = nbt.getBoolean("neighborUpdated");
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), -1, this.getUpdateTag());
	}
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(pkt.getNbtCompound());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		deserializeNBT(tag);
	}

	@Override
	public CompoundNBT getTileData() {
		return getUpdateTag();
	}
	/********************/
}
