package de.dualuse.math;

//import static java.lang.Math.*;

public abstract class NonLinearRegressionSolverTemplate extends NonLinearOptimization {
	public static int DIM = 0; //DIMENSIONS
	
	public NonLinearRegressionSolverTemplate() { super(DIM); }
	
	public abstract int n();
	//public abstract double DATAELEMENT-INPUT(int i);
	
	
	private double e = 0, e_[] = {}, e__[][] = {};
	
	@Override
	protected double e(double[] params) {
		//final double PARAMETER = params[INDEX];
		
		double error = 0; 
		for (int i=0,I=n();i<I;i++) {
			//final double DATAELEMENT = DATAELEMENT(i);
					
			//ERROR-FUNCTION
//			error += pow(pow(pow(y-cy,2)+pow(x-cx,2),1.0/2.0)-r,2) ;
			error += e;
		}
		
		return error;
	}
	
	@Override
	protected double[] e_(double[] params, double[] values) {
		//final double PARAMETER = params[INDEX];
				
		for (int i=0,I=n();i<I;i++) {
			//final double DATAELEMENT = DATAELEMENT(i);
			
			//FIRST-ORDER-DERIVATE
//			double  v1 = x-cx , v2 = y-cy , v3 = sqrt(pow(v2,2)+pow(v1,2)) , v4 = v3-r, contrib[] = { -2*v1*v4,-2*v2*v4,-2*v3*v4 };
			
			for (int j=0;j<DIM;j++)
				values[j]+=e_[j];
			
		}
		
		return values;
	}
	
	@Override
	protected double[][] e__(double[] params, double[][] values) {
		//final double PARAMETER = params[INDEX];
		
		for (int i=0,I=n();i<I;i++) {
			//final double DATAELEMENT = DATAELEMENT(i);
			
			//SECOND-ORDER-DERIVATE
//			double  v1 = x-cx , v2 = pow(v1,2) , v3 = y-cy , v4 = pow(v3,2) , v5 = sqrt(v4+v2) , v6 = 1/v5 , v7 = 2*(v5-r) , v8 = 2*v1*v3*v6 , v9 = 2*v1 , v10 = 2*v3 , result[][] = {{v7+2*v2*v6,v8,v9},{v8,v7+2*v4*v6,v10},{v9,v10,2*v5}};
			
			for (int j=0,J=DIM*DIM;j<J;j++)
				values[j/DIM][j%DIM] += e__[j/DIM][j%DIM];
		}
		
		return values;
	}

}











