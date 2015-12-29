/**
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package gfxdperf.ycsb.core.generator;

import gfxdperf.ycsb.core.Utils;

/**
 * A generator of an exponential distribution. It produces a sequence
 * of time intervals (integers) according to an exponential
 * distribution.  Smaller intervals are more frequent than larger
 * ones, and there is no bound on the length of an interval.  When you
 * construct an instance of this class, you specify a parameter gamma,
 * which corresponds to the rate at which events occur.
 * Alternatively, 1/gamma is the average length of an interval.
 * See {@link gfxdperf.ycsb.core.generator.ExponentialGeneratorPrms} for
 * generator configuration.
 */
public class ExponentialGenerator extends IntegerGenerator
{
	/**
	 * The exponential constant to use.
	 */
	double _gamma;	

	/******************************* Constructors **************************************/

	/**
	 * Create an exponential generator with a mean arrival rate of
	 * gamma.  (And half life of 1/gamma).
	 */
	public ExponentialGenerator(double mean)
	{
		_gamma = 1.0/mean;
	}
	public ExponentialGenerator(int recordcount)
	{
          double percentile =
            ExponentialGeneratorPrms.getExponentialFraction();
          double fraction =
            ExponentialGeneratorPrms.getExponentialPercentile();
          double range = recordcount*fraction;
          _gamma = -Math.log(1.0-percentile/100.0) / range;  //1.0/mean;
	}

	/****************************************************************************************/
	
	/** 
	 * Generate the next item. this distribution will be skewed toward lower integers; e.g. 0 will
	 * be the most popular, 1 the next most popular, etc.
	 * @param itemcount The number of items in the distribution.
	 * @return The next item in the sequence.
	 */
	@Override
	public int nextInt()
	{
		return (int)nextLong();
	}

	/**
	 * Generate the next item as a long.
	 * 
	 * @param itemcount The number of items in the distribution.
	 * @return The next item in the sequence.
	 */
	public long nextLong()
	{
		return (long) (-Math.log(Utils.random().nextDouble()) / _gamma);
	}

	@Override
	public double mean() {
		return 1.0/_gamma;
	}
}
