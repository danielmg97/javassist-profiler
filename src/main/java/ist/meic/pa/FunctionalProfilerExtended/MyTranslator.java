package ist.meic.pa.FunctionalProfilerExtended;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.HashMap;
import java.util.HashSet;

public class MyTranslator implements Translator {

	//O mapa guarda os fully qualified names das classes que terão como value um objeto que encapsula os contadores.
	public static HashMap<String, MyCountRegister> hashMap = new HashMap<String, MyCountRegister>();
	
	//Estes mapas guardam o nome dos fields e o nr de writes ou counts dependendo
	public static HashMap<String, Integer> readFieldStatistics = new HashMap<String, Integer>();
	public static HashMap<String, Integer> writeFieldStatistics = new HashMap<String, Integer>();
	
	//Array para guardar os fields que são lidos/escritos a partir de classes que não os declaram (guardo já o output para printar field+classe_que_acede+classe_que_declara)
	public static HashSet<String> fieldsAccessedRemotely = new HashSet<String>();
	
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException { }

	public static void incrementReads(String nome) {
		hashMap.get(nome).incrRead();
	}

	public static void incrementWrites(String nome) {
		hashMap.get(nome).incrWrite();
	}
	
	public static void incrementFieldReadCount(String nome) {
		readFieldStatistics.put(nome,readFieldStatistics.get(nome)+1);
	}
	
	public static void incrementFieldWriteCount(String nome) {
		writeFieldStatistics.put(nome,writeFieldStatistics.get(nome)+1);
	}

	public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {

		CtClass ctClass = pool.get(classname);

		hashMap.put(classname, new MyCountRegister());

		CtMethod[] ctms = ctClass.getDeclaredMethods();

		//Instrumentação ao nível dos métodos, alterações em loadTime.
		for (int i = 0; i < ctms.length; i++) {

			ctms[i].instrument(new ExprEditor() {

				@Override
				public void edit(FieldAccess f) {
					if (f.isReader() && !f.getFieldName().equals("out")) {	//Se fieldAccess é leitura, não contamos o out (é próprio e incontornável do Java)
						if(!readFieldStatistics.containsKey(f.getFieldName()))
							readFieldStatistics.put(f.getFieldName(), 0);
						try {
							f.replace("{ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementReads( $0.getClass().getName());"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementFieldReadCount(\""+f.getFieldName()+"\");"
									+ "if(!$0.getClass().getName().equals(\""+ctClass.getName()+"\"))"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.fieldsAccessedRemotely.add( \"Field \'"+f.getFieldName()+"\' declared in class <\"+$0.getClass().getName()+\"> is read from the class <"+ctClass.getName()+">\");"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					if (f.isWriter()) {
						if(!writeFieldStatistics.containsKey(f.getFieldName()))
							writeFieldStatistics.put(f.getFieldName(), 0);
						try {
							f.replace("{ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementWrites( $0.getClass().getName());"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementFieldWriteCount(\""+f.getFieldName()+"\");"
									+ "if(!$0.getClass().getName().equals(\""+ctClass.getName()+"\"))"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.fieldsAccessedRemotely.add( \"Field \'"+f.getFieldName()+"\' declared in class <\"+$0.getClass().getName()+\"> is written from the class <"+ctClass.getName()+">\");"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		CtConstructor[] ctcs = ctClass.getDeclaredConstructors();
		
		//Instrumentação ao nível dos Construtores, alterações em loadTime.
		for (int i = 0; i < ctcs.length; i++) {
			ctcs[i].instrument(new ExprEditor() {
				@Override
				public void edit(FieldAccess f) {
					if (f.isReader() && !f.getFieldName().equals("out")) {
						if(!readFieldStatistics.containsKey(f.getFieldName()))
							readFieldStatistics.put(f.getFieldName(), 0);
						try {
							f.replace("{ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementReads( $0.getClass().getName());"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementFieldReadCount(\""+f.getFieldName()+"\");"
									+ "if(!$0.getClass().getName().equals(\""+ctClass.getName()+"\"))"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.fieldsAccessedRemotely.add( \"Field \'"+f.getFieldName()+"\' declared in class <\"+$0.getClass().getName()+\"> is read from the class <"+ctClass.getName()+">\");"
									+ " $_ = $proceed($$);}");
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
					if (f.isWriter()) {
						if(!writeFieldStatistics.containsKey(f.getFieldName()))
							writeFieldStatistics.put(f.getFieldName(), 0);
						try {
							f.replace("{if(!($0.equals(this))){"	//Escritas a fields da própria classe não contam na inicialização.
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementWrites( $0.getClass().getName());"
									+ "if(!$0.getClass().getName().equals(\""+ctClass.getName()+"\"))"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.fieldsAccessedRemotely.add( \"Field \'"+f.getFieldName()+"\' declared in class <\"+$0.getClass().getName()+\"> is written from the class <"+ctClass.getName()+">\");"
									+ "ist.meic.pa.FunctionalProfilerExtended.MyTranslator.incrementFieldWriteCount(\""+f.getFieldName()+"\");}"
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
