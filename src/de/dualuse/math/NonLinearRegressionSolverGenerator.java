package de.dualuse.math;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NonLinearRegressionSolverGenerator {
	
	public static void main(String[] args) throws Exception {
//		String source = "src/de/dualuse/math/CircleFittingDemo.mx";
		String source = "src/de/dualuse/math/RayBundling.mx";
		String template = "src/de/dualuse/math/NonLinearRegressionSolverTemplate.java";

		
		String replacements[][] = { 
//				{"pow\\((.+),\\s*1/2\\s*\\)", "sqrt($1)" },
				{"sqrt","Math.sqrt"}, //{"sqrt\\((.+)\\)", "Math.sqrt($1)" },
				{"pow", "Math.pow"}, //{"pow\\((.+),(.+)\\)", "Math.pow($1,$2)" },
				{"(([^\\w])(\\d+(\\.\\d*)?)|(([^\\w])(\\d*\\.\\d+)))", "$1d"},
			};
		
		String expr2c[] = { 
				"printdef(expr) := sprint(first(expr), \"=\", second(expr), \",\")$",
				"block2c(expr) := if ?equal(op(expr), block) then ( sprint(\"double\",\"\"), for d in reverse(rest(reverse(rest(expr)))) do printdef(d), sprint(\"result =\", string(last(expr)), \";\")) else sprint(\"double result =\", string(expr), \";\")$",
				"expr2c(expr) := ( block2c(subst(pow, \"^\", optimize(expr))) )$"
		};

//		final Pattern datapoint = Pattern.compile("datapoint:\\s*\\[\\s*(\\w+)\\s*((,)|(]\\s*;\\s*$))");
		final Pattern datapointPattern = Pattern.compile("^\\s*datapoint\\s*:\\s*\\[((?:\\s*(?:\\w+)\\s*,)*\\s*(?:\\w+)\\s*)\\]\\s*;\\s*$");
		final Pattern parametersPattern = Pattern.compile("^\\s*parameters\\s*:\\s*\\[((?:\\s*(?:\\w+)\\s*,)*\\s*(?:\\w+)\\s*)\\]\\s*;\\s*$");
		final Pattern datapointErrorExpressionPattern = Pattern.compile("^\\s*(e)\\s*:\\s*(.+);\\s*$");
		
		final Pattern listElementsPattern = Pattern.compile("\\s*(\\w+)\\s*,?"); 
		
		/////
		
		ArrayList<String> datapointElements = new ArrayList<String>();
		ArrayList<String> parameterNames = new ArrayList<String>();
		String errorExpression = "", errorExpressionName = "";


		Process mp = new ProcessBuilder("/Applications/Maxima.app/Contents/Resources/maxima.sh").start();
		
		try (BufferedReader br = new BufferedReader(new FileReader(new File(source))); PrintWriter out = new PrintWriter(mp.getOutputStream())) {
			
			//Configuration
			out.println("display2d:false;");
			for (String s: expr2c)
				out.println(s);
			
			//Filling
			for (String line ="";line!=null;line = br.readLine()) {
				for (Matcher m = datapointPattern.matcher(line), n;m.find(); out.println(m.group(0)))
					for (n = listElementsPattern.matcher(m.group(1)), datapointElements.clear();n.find();)
						datapointElements.add(n.group(1));

				for (Matcher m = parametersPattern.matcher(line), n;m.find(); out.println(m.group(0)))
					for (n = listElementsPattern.matcher(m.group(1)), parameterNames.clear();n.find();)
						parameterNames.add(n.group(1));

				for (Matcher m = datapointErrorExpressionPattern.matcher(line);m.find(); out.println(m.group(0))) {
					errorExpression = m.group(0);
					errorExpressionName = m.group(1);
				}
			}

//			System.out.println();
			ArrayList<String> firstOrderDerivativeNames = new ArrayList<String>();
			for (String p: parameterNames) {
				String derivativeName = "d_d"+p;
				firstOrderDerivativeNames.add(derivativeName);
				out.println(derivativeName+": diff("+errorExpressionName+","+p+",1);");
			}
			
//			System.out.println();
			ArrayList<ArrayList<String>> secondOrderDerivativeNamesList = new ArrayList<ArrayList<String>>();
			for (String q: firstOrderDerivativeNames) {
				ArrayList<String> secondOrderDerivativeNames = new ArrayList<String>();
				for (String p: parameterNames) {
					String secondOrderDerivativeName = "d"+q+"d"+p;
					out.println(secondOrderDerivativeName+": diff("+q+","+p+",1);");
					secondOrderDerivativeNames.add(secondOrderDerivativeName);
				}
				secondOrderDerivativeNamesList.add(secondOrderDerivativeNames);
			}
			

			out.println("expr2c("+errorExpressionName+");");
			out.println("expr2c("+firstOrderDerivativeNames+");");
			out.println("expr2c("+secondOrderDerivativeNamesList+");");
		}
		

		final Pattern inputPattern = Pattern.compile("^\\(\\%i(\\d+)\\) (.+)$");
	
		ArrayList<String> derivatives = new ArrayList<String>();
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(mp.getInputStream()))) {
			
			for (String line=br.readLine();line!=null;line=br.readLine())
				for (Matcher m=inputPattern.matcher(line);m.find();)
					derivatives.add(m.group(2));
//					System.out.println(m.group(1)+": "+m.group(2));
//				System.out.println(line);
		}
		
		final String templateName = template.replaceAll("(:?.*/)?(\\w+)\\W.*", "$2");
		final String sourceName = source.replaceAll("(:?.*/)?(\\w+)\\W.*", "$2");
		
		
		final Pattern classNamePattern = Pattern.compile("^(.*)("+templateName+")(.*)$");
		
		final Pattern dataPointElementNamePattern = Pattern.compile("^(\\s*)//(.*)DATAELEMENT-INPUT(.*)$");
		final Pattern parameterCountPattern = Pattern.compile("^(.*DIM\\s*=\\s*)\\d+(\\s*;.*)$");
		final Pattern parameterDefinitionPattern = Pattern.compile("^(\\s*)//(.*)PARAMETER(.*\\[\\s*)INDEX(\\s*\\]\\s*;.*)$");
		final Pattern dataElementDefinitionPattern = Pattern.compile("^(\\s*)//(.*)DATAELEMENT(.*)DATAELEMENT(\\s*\\(\\s*i\\s*\\)\\s*;.*)$");
		

		final Pattern errorFunctionPattern = Pattern.compile("^(\\s*)//(.*)ERROR-FUNCTION(.*)$");
		final Pattern firstOrderDerivatePattern = Pattern.compile("^(\\s*)//(.*)FIRST-ORDER-DERIVATE(.*)$");
		final Pattern secondOrderDerivatePattern = Pattern.compile("^(\\s*)//(.*)SECOND-ORDER-DERIVATE(.*)$");
		
		
		
		try (BufferedReader br = new BufferedReader(new FileReader(template))) {
			
			reader:
			for (String line=br.readLine();line!=null;line=br.readLine()) {

				for (Matcher m=classNamePattern.matcher(line);m.find();) {
					System.out.println(m.group(1)+sourceName+m.group(3));
					continue reader;
				}

				
				for (Matcher m=parameterCountPattern.matcher(line);m.find();) {
					System.out.println(m.group(1)+parameterNames.size()+m.group(2));
					continue reader;
				}
				
				for (Matcher m=dataPointElementNamePattern.matcher(line);m.find();) {
					for (String element: datapointElements)
						System.out.println(m.group(1)+m.group(2)+element+m.group(3)+" /// GENERATED");
					
					continue reader;
				}

				for (Matcher m=parameterDefinitionPattern.matcher(line);m.find();) {
					for (int i=0,I=parameterNames.size();i<I;i++)
						System.out.println(m.group(1)+m.group(2)+parameterNames.get(i)+m.group(3)+i+m.group(4)+" /// GENERATED");
					
					continue reader;
				}

				for (Matcher m=dataElementDefinitionPattern.matcher(line);m.find();) {
					for (String element: datapointElements)
						System.out.println(m.group(1)+m.group(2)+element+m.group(3)+element+m.group(4)+" /// GENERATED");
					
					continue reader;
				}
				
				for (Matcher m=errorFunctionPattern.matcher(line);m.find();) {
					String code = derivatives.get(0)
							.replace("%", "v")
							.replace("result","e")
							.replaceAll(" , (v|e)", ",\n"+m.group(1)+"\t$1");
					
					for (String[] replacement: replacements)
						code = code.replaceAll(replacement[0], replacement[1]);
					
					System.out.println(m.group(1)+code+m.group(2));
					continue reader;
				}
				
				for (Matcher m=firstOrderDerivatePattern.matcher(line);m.find();) {
					String code = derivatives.get(1)
							.replace("%", "v")
							.replace("result = [", "e_[] = {")
							.replace("] ;", "};")
							.replaceAll(" , (v|e)", ",\n"+m.group(1)+"\t$1");
					
					for (String[] replacement: replacements)
						code = code.replaceAll(replacement[0], replacement[1]);

					System.out.println(m.group(1)+code+m.group(2));
					continue reader;
				}

				for (Matcher m=secondOrderDerivatePattern.matcher(line);m.find();) {
					String code = derivatives.get(2)
							.replace("%", "v")
							.replace("result = [[", "e__[][] = {{")
							.replace("],[","},{")
							.replace("]] ;", "}};")
							.replaceAll(" , (v|e)", ",\n"+m.group(1)+"\t$1")
//							.replaceAll("\\{\\{", "\n"+m.group(1)+"\t\t{{")
							.replaceAll("\\{\\{", "\t{{")
							.replaceAll(",\\{", ",\n"+m.group(1)+"\t\t\t {");
//							.replaceAll("(\\{|,)\\{", "$1\n"+m.group(1)+"\t\t{")
//							.replaceAll("\\}\\}", "}\n"+m.group(1)+"\t}");
					
					for (String[] replacement: replacements)
						code = code.replaceAll(replacement[0], replacement[1]);
					
					System.out.println(m.group(1)+code+m.group(2));
					continue reader;
				}

				
				
				
				System.out.println(line);
			}
			
		}
		
		
		
		
		
//		
//		
//		
//		Process p= new ProcessBuilder("/Applications/Maxima.app/Contents/Resources/maxima.sh").start();
//
//		InputStream is = p.getInputStream(), es = p.getErrorStream();
//		PrintStream ps = new PrintStream (p.getOutputStream());
//		
//		new Thread() {
//			public void run() {
//				try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
//					for (String line=br.readLine();line!=null;line=br.readLine())
//						System.out.println(line);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			};
//		}.start();
//		
//		new Thread() {
//			public void run() {
//				try (BufferedReader br = new BufferedReader(new InputStreamReader(es))) {
//					for (String line=br.readLine();line!=null;line=br.readLine())
//						System.err.println(line);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			};
//		}.start();
//
//	
//		
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
//			for (String line=br.readLine();line!=null;line=br.readLine()) {
//				ps.println(line);
//				ps.flush();
//			}
//		}
		
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream())); PrintWriter pw = new PrintWriter(); ) {
//		
//		while (p.isAlive()) {
//			
//		}
		
	}

}
