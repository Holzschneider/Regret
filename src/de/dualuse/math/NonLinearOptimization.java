package de.dualuse.math;


import java.util.Arrays;

public abstract class NonLinearOptimization {
	
	protected NonLinearOptimization(int dim) {
		p = new double[dim];
		q = new double[dim];
		s = new double[dim];
		
		d = new double[dim];
		b = new double[dim];
		J = new double[dim][dim];
		L = new double[dim][dim];
		U = new double[dim][dim];
	}
	
	private double p[] = new double[0];
	private double q[] = new double[0];
	private double s[] = new double[0];
	
	private double d[] = new double[0];
	private double b[] = new double[0];
	private double J[][] = new double[0][0];
	private double L[][] = new double[0][0];
	private double U[][] = new double[0][0];
	
	protected abstract double e(double[] params);
	protected abstract double[] e_(double[] params, double[] result);
	
	public double s() { throw new RuntimeException("either implement s or e__"); };
	public double[] s(double[] parameters, double[] sampleStepping) { Arrays.fill(sampleStepping, s()); return sampleStepping; };
	
//	public abstract double[][] e__(double[] params, double[][] result);
	protected double[][] e__(double[] parameters, double[][] result) {
		int n = p.length;
		
		s(parameters, s);
		for (int j=0;j<n;j++) {
			final double original = parameters[j];
			final double samplingRadius = s[j];
			
			parameters[j]=original+samplingRadius;
			e_(parameters, p);
			
			parameters[j]=original-samplingRadius;
			e_(parameters, q);
			
			parameters[j]=original;
			
			for (int k=0;k<n;k++)
				J[j][k] = (p[k]-q[k])/(2*samplingRadius);
		}
		return result; 
	}
	
	protected void sanitize(final double[] parameters) { }

	protected int iterations() { throw new RuntimeException(); }
	protected double epsilon() { throw new RuntimeException(); }
	
	protected double optimize(final double[] parameters) {
		return optimize(parameters,epsilon(), iterations());
	}
	
	protected double optimize(final double[] parameters, final double convergenceError, final int maxIterations ) {
		final int n = p.length;
		
		Arrays.fill(p,0);
		Arrays.fill(q,0);
		Arrays.fill(s,0);
		
		Arrays.fill(d,0);
		
		//http://de.wikipedia.org/wiki/Newton-Verfahren#Das_Newton-Verfahren_im_Mehrdimensionalen
		
		double error = e(parameters);
		for (int i=0;i<maxIterations && error > convergenceError;i++) {
		
			for (int j=0,J=b.length;j<J;j++)
				b[j]=0;
			
			e_(parameters, b);
			
			for (int r=0;r<J.length;r++)
				for (int c=0;c<J[r].length;c++)
					J[r][c] = L[r][c] = U[r][c] = 0;

			e__(parameters, J);

			for (int j=0;j<n;j++) b[j] = -b[j];

			decompose(n, J, L, U);
			solve(n, L, U, b, d);
			
			for (int j=0;j<n;j++) parameters[j] += d[j];
			
			sanitize(parameters);
			
			error = e(parameters);	
		}

		return error;
//		return e(parameters);
	}

	protected double optimize(final double[] parameters, final double[] parameterWeighting, final double[] constantDamping, final double convergenceError, final int maxIterations ) {
		final int n = p.length;
				
//		double error = e(parameters);
		double error = Double.POSITIVE_INFINITY;
		for (int i=0;i<maxIterations && error > convergenceError;i++) {
			
			e_(parameters, b);
			for (int j=0;j<n;j++) b[j] = -b[j]*parameterWeighting[j];
			
			e__(parameters, J);
			for (int j=0;j<n;j++)
				for (int k=0;k<n;k++ )
					J[j][k] *= parameterWeighting[k]*parameterWeighting[j] ;

			decompose(n, J, L, U);
			solve(n, L, U, b, d);
			for (int j=0;j<n;j++) parameters[j] += constantDamping[j]*d[j];

			sanitize(parameters);
			error = e(parameters);	
		}
		
		return error; //e(parameters);
	}
	
	
	

//	 decompose
	protected static void decompose(int n, double[][] M, double[][] L, double[][] U){
		
	    // Code: Cormen et al., page 756
	    int i, j, k;
	    for ( k = 0; k < n; ++k) {
	        U[ k][ k] = M[ k][ k];
	        for ( i = k+1; i < n; ++i) {
	            L[ i][ k] = M[ i][ k] / U[ k][ k];
	            U[ k][ i] = M[ k][ i];
	        }
	        for( i = k+1; i < n; ++i) {
	            for( j = k+1; j < n; ++j) {
	                M[ i][ j] = M[ i][ j] - L[ i][ k]*U[ k][ j];
	            }
	        }
	    }
	}

	// solve
	protected static void solve(int n, double[][] L, double[][] U, double[] y, double[] x) {
		
	    // Code: Cormen et al., page 756
//	    double[] y = b;//new double[n];
	    int i, j;

	    // forward substitution
	    for ( i = 0; i < n; ++i) {
//	        y[ i] = b[ i];
	        for ( j = 0; j < i; ++j) {
	            y[ i] -= L[ i][ j] * y[ j];
	        }
	    }

	    // back substitution
	    for ( i = n-1; i >= 0; --i) {
	        x[ i] = y[ i];
	        for ( j = i+1; j < n; ++j) {
	            x[ i] -= U[ i][ j] * x[ j];
	        }
	        x[ i] /= U[ i][ i];
	    }
	}
	

	
//	public static int factor(double A[][], int pivot[]) {
//		int N = A.length, M = A[0].length, minMN = N < M ? N : M;
//
//		for (int j = 0; j < minMN; j++) {
//
//			int jp = j;
//
//			double t = Math.abs(A[j][j]);
//			for (int i = j + 1; i < M; i++) {
//				double ab = Math.abs(A[i][j]);
//				if (ab > t) {
//					jp = i;
//					t = ab;
//				}
//			}
//
//			pivot[j] = jp;
//
//			if (A[jp][j] == 0)
//				return 1; // factorization failed because of zero pivot
//
//			if (jp != j) {
//				// swap rows j and jp
//				double tA[] = A[j];
//				A[j] = A[jp];
//				A[jp] = tA;
//			}
//
//			if (j < M - 1) {
//				double recp = 1.0 / A[j][j];
//
//				for (int k = j + 1; k < M; k++)
//					A[k][j] *= recp;
//			}
//
//			if (j < minMN - 1) {
//
//				for (int ii = j + 1; ii < M; ii++) {
//					double Aii[] = A[ii];
//					double Aj[] = A[j];
//					double AiiJ = Aii[j];
//					for (int jj = j + 1; jj < N; jj++)
//						Aii[jj] -= AiiJ * Aj[jj];
//
//				}
//			}
//		}
//
//		return 0;
//	}
//
//	public static void solve(double LU[][], int pvt[], double b[]) {
//		int M = LU.length;
//		int N = LU[0].length;
//		int ii = 0;
//
//		for (int i = 0; i < M; i++) {
//			int ip = pvt[i];
//			double sum = b[ip];
//
//			b[ip] = b[i];
//			if (ii == 0)
//				for (int j = ii; j < i; j++)
//					sum -= LU[i][j] * b[j];
//			else if (sum == 0.0)
//				ii = i;
//			b[i] = sum;
//		}
//
//		for (int i = N - 1; i >= 0; i--) {
//			double sum = b[i];
//			for (int j = i + 1; j < N; j++)
//				sum -= LU[i][j] * b[j];
//			b[i] = sum / LU[i][i];
//		}
//	}


}
