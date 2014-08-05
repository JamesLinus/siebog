package radigost.samples.jade.predators;

public final class Const
{
	public static final int DIM 				= 8;
	public static final double EPSILON 			= 0.0001;
	public static final double REWARD_WIN 		= 15;
	public static final double REWARD_STEP 		= -0.1;
	public static final double LEARNING_RATE 	= 0.8;
	public static final double DISCOUNT_RATE 	= 0.9;
	public static final double LEARNING_RATE_1 	= 1.0 - LEARNING_RATE;
	public static final int NUM_PREDATORS 		= 4;
	public static final int EP_COUNT 			= 20;
	public static final int TRI_COUNT 			= 1000;
}
