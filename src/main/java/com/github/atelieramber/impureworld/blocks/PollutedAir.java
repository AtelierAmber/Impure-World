package com.github.atelieramber.impureworld.blocks;

import com.github.atelieramber.impureworld.blocks.tileentities.TileEntityPollutedAir;
import com.github.atelieramber.impureworld.lists.TileEntityTypes;
import com.github.atelieramber.impureworld.materials.ModMaterials;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PollutedAir extends Block {

	public static final IntegerProperty IMPURITY = IntegerProperty.create("impurity", 0, 10);

	public PollutedAir(Properties properties) {
		super(properties);
		this.setDefaultState(getDefaultState().with(getImpurityProperty(), Integer.valueOf(5)));
	}

	public IntegerProperty getImpurityProperty() {
		return IMPURITY;

	}
	/* Reference For Later *//*
								 * 
								 * worldIn.getBiomeProvider().findBiomePosition(x, z, range, biomes, random)
								 * 
								 * world.getBiome(blockPos).getBiomeName().toLowerCase() == "hell";
								 * 
								 * hardness cap pressure to prevent filling an area of pollution
								 */

	protected int getImpurity(BlockState state) {
		return state.get(this.getImpurityProperty());
	}

	protected BlockState withImpurity(int impurity) {
		return this.getDefaultState().with(this.getImpurityProperty(), Integer.valueOf(impurity));
	}

	public void updateImpurity(World world, BlockPos pos, BlockState state, float impurity) {
		int oldImpurity = getImpurity(state);
		int newImpurity = Math.round(impurity * 10.0f);
		if (oldImpurity != newImpurity && newImpurity < 11) {
			System.out.println("Updated Impurity: " + impurity + ":" + newImpurity);
			world.setBlockState(pos, this.withImpurity(newImpurity), 2);
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity()) {
			TileEntityPollutedAir TE = (TileEntityPollutedAir) worldIn.getTileEntity(pos);
			TE.onReplaced(worldIn, state, newState);
			if (state.getBlock() != newState.getBlock() || !newState.hasTileEntity()) {
				worldIn.removeTileEntity(pos);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileEntityPollutedAir) {
			TileEntityPollutedAir tepa = (TileEntityPollutedAir) te;
			tepa.onNeighborStateUpdated(fromPos);
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation axis) {
		return super.rotate(state, world, pos, axis);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entityIn) {
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = ((PlayerEntity) entityIn);
			double eyePos = player.getPosYEye();
			world.destroyBlock(pos.offset(player.getHorizontalFacing().getOpposite(), 1), false, player);
			BlockPos eyePosBlock = new BlockPos(player.getPosX(), eyePos, player.getPosZ());
			if (world.getBlockState(eyePosBlock).getMaterial() == material) {
				TileEntityPollutedAir tileEntity = (TileEntityPollutedAir) world.getTileEntity(pos);
				// player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10, 0));
			}
		}
	}

	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
		TileEntityPollutedAir newTE = TileEntityTypes.POLLUTED_AIR.get().create();
		newTE.setComposition(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0);
		return newTE;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(IMPURITY);
	}

	public static Properties properties = Block.Properties.create(ModMaterials.POLLUTED_AIR).sound(SoundType.SNOW)
			.notSolid().variableOpacity();
}
