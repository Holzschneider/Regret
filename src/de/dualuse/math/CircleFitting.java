package de.dualuse.math;
import static java.lang.Math.*;


public abstract class CircleFitting extends NonLinearOptimization {
	public CircleFitting() { super(3); }
	
	public abstract int n();
	public abstract double x(int i);
	public abstract double y(int i);
	
	
	@Override
	protected double e(double[] params) {
		double cx = params[0], cy = params[1], r = params[2];
		
		double error = 0; 
		for (int i=0,I=n();i<I;i++) {
			double x = x(i), y = y(i);
			
			//expr2c(e);
			error += pow(pow(pow(y-cy,2)+pow(x-cx,2),1.0/2.0)-r,2) ;
		}
		
		return error;
	}

	@Override
	protected double[] e_(double[] params, double[] result) {
		double cx = params[0], cy = params[1], r = params[2];
				
		for (int i=0,I=n();i<I;i++) {
			final double x = x(i), y = y(i);
			
			//expr2c( [e_cx, e_cy, e_r]);
			double  v1 = x-cx , v2 = y-cy , v3 = sqrt(pow(v2,2)+pow(v1,2)) , v4 = v3-r, contrib[] = { -2*v1*v4,-2*v2*v4,-2*v3*v4 };
			
			for (int j=0;j<contrib.length;j++)
				result[j]+=contrib[j];

		}
		
		return result;
	}
	
	protected double[][] e__(double[] parameters, double[][] values) {
		double cx = parameters[0], cy = parameters[1], r = parameters[2];

		for (int i=0,I=n();i<I;i++) {
			double x = x(i), y = y(i);

			//expr2c( [[e_cxcx, e_cxcy, e_cxr], [e_cxcy, e_cycy, e_cyr],[e_cxr, e_cyr, e_rr]]);
			double  v1 = x-cx , v2 = pow(v1,2) , v3 = y-cy , v4 = pow(v3,2) , v5 = sqrt(v4+v2) , v6 = 1/v5 , v7 = 2*(v5-r) , v8 = 2*v1*v3*v6 , v9 = 2*v1 , v10 = 2*v3 , result[][] = {{v7+2*v2*v6,v8,v9},{v8,v7+2*v4*v6,v10},{v9,v10,2*v5}};
			
			for (int j=0;j<9;j++)
				values[j/3][j%3] += result[j/3][j%3];
		}
		
		return values;
	}

}











