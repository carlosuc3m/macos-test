package org.bioimageanalysis.icy.deeplearning.transformations;

import org.bioimageanalysis.icy.deeplearning.tensor.Tensor;

import net.imglib2.converter.RealTypeConverters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;

public abstract class AbstractTensorTransformation implements TensorTransformation
{

	private final String name;

	private Mode mode = Mode.FIXED;
	
	protected static String DEFAULT_MISSING_ARG_ERR = "Cannot execute Clip BioImage.io transformation because '%s' "
			+ "parameter was not set.";

	protected AbstractTensorTransformation( final String name )
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setMode( final Mode mode )
	{
		this.mode = mode;
	}

	@Override
	public Mode getMode()
	{
		return mode;
	}

	protected < R extends RealType< R > & NativeType< R > > Tensor< FloatType > makeOutput( final Tensor< R > input )
	{
		final ImgFactory< FloatType > factory = Util.getArrayOrCellImgFactory( input.getData(), new FloatType() );
		final Img< FloatType > outputImg = factory.create( input.getData() );
		// TODO improve, do not copy here. Do it directly in the method to avoid looping twice over the images
		RealTypeConverters.copyFromTo(input.getData(), outputImg);
		final Tensor< FloatType > output = Tensor.build( getName() + '_' + input.getName(), input.getAxesOrderString(), outputImg );
		return output;
	}
}
