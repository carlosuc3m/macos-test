/*-
 * #%L
 * Use deep learning frameworks from Java in an agnostic and isolated way.
 * %%
 * Copyright (C) 2022 - 2023 Institut Pasteur and BioImage.IO developers.
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the BioImage.io nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package io.bioimage.modelrunner.transformations;

import io.bioimage.modelrunner.tensor.Tensor;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class ClipTransformation extends AbstractTensorPixelTransformation
{

	private static final class ClipFunction implements FloatUnaryOperator
	{

		private final float min;

		private final float max;

		private ClipFunction( final double min, final double max )
		{
			this.min = (float) min;
			this.max = (float) max;
		}

		@Override
		public final float applyAsFloat( final float in )
		{
			return ( in > max )
					? max
					: ( in < min )
							? min
							: in;
		}
	}
	
	private static String name = "clip";
	private Double min;
	private Double max;

	public ClipTransformation()
	{
		super(name);
	}
	
	public void setMin(Object min) {
		if (min instanceof Integer) {
			this.min = Double.valueOf((int) min);
		} else if (min instanceof Double) {
			this.min = (double) min;
		} else if (min instanceof String) {
			this.min = Double.valueOf((String) min);
		} else {
			throw new IllegalArgumentException("'min' parameter has to be either and instance of "
					+ Integer.class + " or " + Double.class
					+ ". The provided argument is an instance of: " + min.getClass());
		}
	}
	
	public void setMax(Object max) {
		if (max instanceof Integer) {
			this.max = Double.valueOf((int) max);
		} else if (max instanceof Double) {
			this.max = (double) max;
		} else if (max instanceof String) {
			this.max = Double.valueOf((String) max);
		} else {
			throw new IllegalArgumentException("'max' parameter has to be either and instance of "
					+ Integer.class + " or " + Double.class
					+ ". The provided argument is an instance of: " + max.getClass());
		}
	}
	
	public void checkRequiredArgs() {
		if (min == null) {
			throw new IllegalArgumentException(String.format(DEFAULT_MISSING_ARG_ERR, "min"));
		} else if (max == null) {
			throw new IllegalArgumentException(String.format(DEFAULT_MISSING_ARG_ERR, "max"));
		}
	}

	public < R extends RealType< R > & NativeType< R > > Tensor< FloatType > apply( final Tensor< R > input )
	{
		checkRequiredArgs();
		super.setFloatUnitaryOperator(new ClipFunction( min, max ) );
		return super.apply(input);
	}

	public void applyInPlace( final Tensor< FloatType > input )
	{
		checkRequiredArgs();
		super.setFloatUnitaryOperator(new ClipFunction( min, max ) );
		super.apply(input);
	}
}
