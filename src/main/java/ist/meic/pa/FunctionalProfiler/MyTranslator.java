package ist.meic.pa.FunctionalProfiler;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.HashMap;

public class MyTranslator implements Translator {

	//O mapa guarda os fully qualified names das classes que ter�o como value um objeto que encapsula os contadores.
	public static HashMap<String, MyCountRegister> hashMap = new HashMap<String, MyCountRegister>();

	public void start(ClassPool pool) throws NotFoundException, CannotCompileException { }

	public static void incrementReads(String nome) {
		hashMap.get(nome).incrRead();
	}

	public static void incrementWrites(String nome) {
		hashMap.get(nome).incrWrite();
	}

	public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {

		CtClass ctClass = pool.get(classname);

		hashMap.put(classname, new MyCountRegister());

		CtMethod[] ctms = ctClass.getDeclaredMethods();

		//Instrumenta��o ao n�vel dos m�todos, altera��es em loadTime.
		for (int i = 0; i < ctms.length; i++) {

			ctms[i].instrument(new ExprEditor() {

				@Override
				public void edit(FieldAccess f) {
					if (f.isReader() && !f.getFieldName().equals("out")) {	//Se fieldAccess � leitura, n�o contamos o out (� pr�prio e incontorn�vel do Java)
						try {
							f.replace("{ist.meic.pa.FunctionalProfiler.MyTranslator.incrementReads( $0.getClass().getName());"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					if (f.isWriter()) {
						try {
							f.replace("{ist.meic.pa.FunctionalProfiler.MyTranslator.incrementWrites( $0.getClass().getName());"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		CtConstructor[] ctcs = ctClass.getDeclaredConstructors();
		
		//Instrumenta��o ao n�vel dos Construtores, altera��es em loadTime.
		for (int i = 0; i < ctcs.length; i++) {
			ctcs[i].instrument(new ExprEditor() {
				@Override
				public void edit(FieldAccess f) {
					if (f.isReader() && !f.getFieldName().equals("out")) {
						try {
							f.replace("{ist.meic.pa.FunctionalProfiler.MyTranslator.incrementReads( $0.getClass().getName());"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					if (f.isWriter()) {
						try {
							f.replace("{if(!($0.equals(this))){"	//Escritas a fields da pr�pria classe n�o contam na inicializa��o.
									+ "ist.meic.pa.FunctionalProfiler.MyTranslator.incrementWrites( $0.getClass().getName());}"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
}
