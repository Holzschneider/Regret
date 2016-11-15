package de.dualuse.math;
import static java.lang.Math.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.dualuse.commons.swing.JKnob;
import de.dualuse.commons.swing.JStickyKnob;


public class CircleFittingTest {
	
	public static interface PointDefinition {
		void define(double x, double y);
	}
	
	public static interface DataPoints {
		public void get(int i, PointDefinition p);
	}
	
	double cx=0, cy=0, r=1;
	
	double error = 0;
	double diffE_cx = 0;
	double diffE_cy = 0;
	double diffE_r = 0;
	
	double diffE_cxcx = 0;
	double diffE_cxcy = 0;
	double diffE_cycy = 0;
	double diffE_cxr = 0;
	double diffE_cyr = 0;
	double diffE_rr = 0;
	
	public CircleFittingTest set(double cx, double cy, double r) {
		this.cx = cx;
		this.cy = cy;
		this.r = r;
		
		return this;
	}
	
	private double sqr(double x) { return x*x; }
	
	static public interface CircleDefinition {
		void defineCircle( double cx, double cy, double r);
	}
	
	public void fit( int n, DataPoints p, double convergenceError, int maxIterations, CircleDefinition steps) {
		for (int it=0;it<maxIterations;it++) {

			diffE_cx = diffE_cy = diffE_r = error = 0;
			diffE_cxcx = diffE_cxcy = diffE_cxr = 0;
			diffE_cycy = diffE_cyr = diffE_rr = 0;
			for (int i=0;i<n;i++)
				p.get(i, (x,y) ->  {
					double Dsq = sqr(y-cy)+sqr(x-cx), D = sqrt(Dsq);
					diffE_cx += -(2*(x-cx)*(D-r));
					diffE_cy += -(2*(y-cy)*(D-r));
					diffE_r  += -2*(D-r);
					
					diffE_cxcx += 2*(D-r)+sqr(2*(x-cx))/D;
					diffE_cxcy += (2*(x-cx)*(y-cy))/D;
					diffE_cxr += 2*(x-cx);
					
					diffE_cycy += 2*(D-r)+(2*sqr(y-cy))/D;
					diffE_cyr += 2*(y-cy);
					diffE_rr += 2*D;
					
					error += Dsq; 
				});
			
			
			if (error<convergenceError)
				break;
			
			final double a = diffE_cxcx, b = diffE_cxcy, c = diffE_cxr;
			final double d = b, e = diffE_cycy, f = diffE_cyr;
			final double g = c, h = f, i = diffE_rr;
		
			final double A = (e * i - f * h), D = -(b * i - c * h), G = (b * f - c * e);
			final double B = -(d * i - f * g), E = (a * i - c * g), H = -(a * f - c * d);
			final double C = (d * h - e * g), F = -(a * h - b * g), I = (a * e - b * d);
		
			final double ooDetA = 1. / (a * A - b * (i * d - f * g) + c * (d * h - e * g));
		
			final double i00 = A * ooDetA, i01 = D * ooDetA, i02 = G * ooDetA;
			final double i10 = B * ooDetA, i11 = E * ooDetA, i12 = H * ooDetA;
			final double i20 = C * ooDetA, i21 = F * ooDetA, i22 = I * ooDetA;
		
			final double dx = i00 * cx + i01 * cy + i02 * r;
			final double dy = i10 * cx + i11 * cy + i12 * r;
			final double dr = i20 * cx + i21 * cy + i22 * r;
			
			cx-= dx*1000;
			cy-= dy*1000;
			r -= dr*1000;
			
			steps.defineCircle(cx, cy, r);
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		
		JFrame f = new JFrame();
		
		f.setContentPane(new JComponent() {
			int N = 20;
			double R = 200;
			double SIGMA = 10;
			
			Point2D.Double C = new Point2D.Double(300,200); 
			
//			JKnob A = new JKnob(220,130);
//			JKnob B = new JKnob(300,240);
			JKnob A = new JStickyKnob(101,99);
			JKnob B = new JStickyKnob(401,400);
			
			{
				add(A);
				add(B);
			}
			
			
			Random r = new Random(1337);
			ArrayList<Point2D> points = new ArrayList<Point2D>();
			{
				for (int i=0;i<N;i++) {
					
					double theta = r.nextDouble()*PI*2;
					double x = sin(theta), y = cos(theta);
					
					double d = r.nextGaussian()*SIGMA;
					
					points.add( new Point2D.Double(x*(R+d)+C.x,y*(R+d)+C.y) );
					
				}
			}
			
			MouseAdapter ma = new MouseAdapter() {
				{
					addMouseWheelListener(this);
				};
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					cycles +=e.getWheelRotation();
					
					cycles = Math.min(20, cycles);
					cycles = Math.max(1, cycles);
					System.out.println(cycles);
					repaint();
						
				}
				
			};
			
			int cycles = 2;
			
			@Override
			protected void paintComponent(Graphics g) {
				
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				
				double r = 3;
				for (Point2D p: points) 
					g2.fill(new Ellipse2D.Double(p.getX()-r, p.getY()-r, 2*r, 2*r));
				
				
				double dx = B.getCenterX()-A.getCenterX(), dy = B.getCenterY()-A.getCenterY(), R = sqrt(dx*dx+dy*dy); 
				g2.draw(new Ellipse2D.Double( A.getCenterX()-R, A.getCenterY()-R, 2*R, 2*R));

				g2.setColor(new Color(0,0,1,0.3f));
				g2.setStroke(new BasicStroke(1));

				CircleFittingDemo cf = new CircleFittingDemo() {
					public int n() { return N; }
					public double x(int i) { return points.get(i).getX(); }
					public double y(int i) { return points.get(i).getY(); }
				};
				System.out.println();
				for (int i=0;i<cycles;i++) {
					
					
					if (i==cycles-1)
						g2.setStroke(new BasicStroke(5));
						
					double[] parameters = { A.getCenterX(), A.getCenterY(), R }; 
					double error = cf.optimize(parameters, 0, i);
					g2.draw(new Ellipse2D.Double(parameters[0]-parameters[2], parameters[1]-parameters[2], 2*parameters[2], 2*parameters[2]));
					System.out.println( sqrt(error/N)+" "+Arrays.toString(parameters) );
				}
				
				g2.setStroke(new BasicStroke(5));
				
//				new LaneSaC().set(A.getCenterX(),A.getCenterY(),R).fit(
//						points.size(), (i,p)-> p.define(points.get(i).getX(), points.get(i).getY()), 
//						0, 100, 
//						(cx,cy,radius) -> { 
//							g2.draw(new Ellipse2D.Double(cx-radius, cy-radius, 2*radius, 2*radius));
//							System.out.println( cx+", "+cy+", "+radius );
//						});
				
				
				g2.dispose();
						
			}
			
		});
		
		
		
		f.setBounds(400, 100, 800, 800);
		f.setVisible(true);
		
		
	}
}




























