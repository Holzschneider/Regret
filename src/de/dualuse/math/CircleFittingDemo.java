package de.dualuse.math;
//import static java.lang.Math.*;

public abstract class CircleFittingDemo extends NonLinearOptimization {
	public static int DIM = 3; //DIMENSIONS
	
	public CircleFittingDemo() { super(DIM); }
	
	public abstract int n();
	public abstract double x(int i); /// GENERATED
	public abstract double y(int i); /// GENERATED
	
	
	private double e = 0, e_[] = {}, e__[][] = {};
	
	@Override
	protected double e(double[] params) {
		final double cx = params[0]; /// GENERATED
		final double cy = params[1]; /// GENERATED
		final double r = params[2]; /// GENERATED
		
		double error = 0; 
		for (int i=0,I=n();i<I;i++) {
			final double x = x(i); /// GENERATED
			final double y = y(i); /// GENERATED
					
			double e = Math.pow(Math.pow(Math.pow(y-cy,2d)+Math.pow(x-cx,2d),1d/2d)-r,2d) ; 
//			error += pow(pow(pow(y-cy,2)+pow(x-cx,2),1.0/2.0)-r,2) ;
			error += e;
		}
		
		return error;
	}
	
	@Override
	protected double[] e_(double[] params, double[] values) {
		final double cx = params[0]; /// GENERATED
		final double cy = params[1]; /// GENERATED
		final double r = params[2]; /// GENERATED
				
		for (int i=0,I=n();i<I;i++) {
			final double x = x(i); /// GENERATED
			final double y = y(i); /// GENERATED
			
			double  v1 = x-cx , v2 = y-cy , v3 = Math.sqrt(Math.pow(v2,2d)+Math.pow(v1,2d)) , v4 = 1d/v3 , v5 = v3-r , e_[] = {-2d*v1*v4*v5,-2d*v2*v4*v5,-2d*v5}; 
//			double  v1 = x-cx , v2 = y-cy , v3 = sqrt(pow(v2,2)+pow(v1,2)) , v4 = v3-r, contrib[] = { -2*v1*v4,-2*v2*v4,-2*v3*v4 };
			
			for (int j=0;j<DIM;j++)
				values[j]+=e_[j];

		}
		
		return values;
	}

	@Override
	protected double[][] e__(double[] params, double[][] values) {
		final double cx = params[0]; /// GENERATED
		final double cy = params[1]; /// GENERATED
		final double r = params[2]; /// GENERATED
		
		for (int i=0,I=n();i<I;i++) {
			final double x = x(i); /// GENERATED
			final double y = y(i); /// GENERATED
			
			double  v1 = x-cx , v2 = Math.pow(v1,2d) , v3 = y-cy , v4 = Math.pow(v3,2d) , v5 = v4+v2 , v6 = 1d/v5 , v7 = Math.sqrt(v5) , v8 = 1d/Math.pow(v7,3d) , v9 = v7-r , v10 = 1d/v7 , v11 = 2d*v10*v9 , v12 = 2d*v1*v3*v6-2d*v1*v3*v8*v9 , v13 = 2d*v1*v10 , v14 = 2d*v3*v10 , e__[][] = {{v11-2d*v2*v8*v9+2d*v2*v6,v12,v13},{v12,v11-2d*v4*v8*v9+2d*v4*v6,v14},{v13,v14,2d}}; 
//			double  v1 = x-cx , v2 = pow(v1,2) , v3 = y-cy , v4 = pow(v3,2) , v5 = sqrt(v4+v2) , v6 = 1/v5 , v7 = 2*(v5-r) , v8 = 2*v1*v3*v6 , v9 = 2*v1 , v10 = 2*v3 , result[][] = {{v7+2*v2*v6,v8,v9},{v8,v7+2*v4*v6,v10},{v9,v10,2*v5}};
			
			for (int j=0,J=DIM*DIM;j<J;j++)
				values[j/DIM][j%DIM] += e__[j/DIM][j%DIM];
		}
		
		return values;
	}

}











