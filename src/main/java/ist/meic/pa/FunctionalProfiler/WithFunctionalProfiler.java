package ist.meic.pa.FunctionalProfiler;

import javassist.*;
import java.util.Arrays;
import java.util.TreeSet;

public class WithFunctionalProfiler {

	public static void main(String[] args) {
		try {
			Translator translator = new MyTranslator();
			ClassPool pool = ClassPool.getDefault();
			Loader classLoader = new Loader(pool);

			classLoader.delegateLoadingOf("ist.meic.pa.FunctionalProfiler.MyTranslator"); //Translator delegado ao parent Loader para possibilitar acessos estáticos.

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
			
		} catch (Throwable throwable) {
			System.out.println("Failed to load Class " + throwable);
		}
	}
}

