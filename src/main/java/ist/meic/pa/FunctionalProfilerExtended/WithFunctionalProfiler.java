package ist.meic.pa.FunctionalProfilerExtended;

import javassist.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WithFunctionalProfiler {

	public static void main(String[] args) {
		try {
			Translator translator = new MyTranslator();
			ClassPool pool = ClassPool.getDefault();
			Loader classLoader = new Loader(pool);

			classLoader.delegateLoadingOf("ist.meic.pa.FunctionalProfilerExtended.MyTranslator"); //Translator delegado ao parent Loader para possibilitar acessos estáticos.

			classLoader.addTranslator(pool, translator);
			classLoader.run(args[0], Arrays.copyOfRange(args, 1, args.length));

			int totalReads = 0;
			int totalWrites = 0;

			for(String s : MyTranslator.hashMap.keySet()) {
				totalReads+=MyTranslator.hashMap.get(s).getReads();
				totalWrites+=MyTranslator.hashMap.get(s).getWrites();
			}

			System.out.println("Total reads: "+totalReads+" Total writes: "+totalWrites);

			for(String s : new TreeSet<>(MyTranslator.hashMap.keySet())) {
				if(MyTranslator.hashMap.get(s).getReads()!=0 || MyTranslator.hashMap.get(s).getWrites()!=0)
					System.out.println("class "+s+" -> reads: "+MyTranslator.hashMap.get(s).getReads()+" writes: "+MyTranslator.hashMap.get(s).getWrites());
			}
			
			//////////////////// Extended - Version ///////////////////////////
			
			final Map<String, Integer> sortedByReadCount = MyTranslator.readFieldStatistics.entrySet()
	                .stream()
	                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			System.out.println("\nFields by Read Access Rank:");
			for(String s : sortedByReadCount.keySet()) {
				if(sortedByReadCount.get(s)!=0)
					System.out.println("\t"+s +" : "+sortedByReadCount.get(s)+ " read(s)");
			}
			
			final Map<String, Integer> sortedByWriteCount = MyTranslator.writeFieldStatistics.entrySet()
	                .stream()
	                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			System.out.println("\nFields by Write Access Rank:");
			for(String s : sortedByWriteCount.keySet()) {
				if(sortedByWriteCount.get(s)!=0)
					System.out.println("\t"+s +" : "+sortedByWriteCount.get(s)+ " write(s)");
			}
			
			if(totalReads!=0) {
				float fraction = (float) totalWrites/totalReads;
				System.out.println("\nThis program is more inclined to "+ ((fraction<=0.5) ? "functional!" : "imperative!"));
				System.out.println("Fraction (totalWrites/totalReads) equals "+ fraction);
			}
				
			System.out.println("\nFields that are read/written outside of the methods of their classes:");
			for(String x : MyTranslator.fieldsAccessedRemotely) {
				System.out.println("\t-"+x);
			}	
			
		} catch (Throwable throwable) {
			System.out.println("Failed to load Class " + throwable);
		}
	}
}

