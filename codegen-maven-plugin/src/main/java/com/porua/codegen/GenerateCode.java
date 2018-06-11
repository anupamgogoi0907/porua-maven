package com.porua.codegen;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.porua.codegen.jaxb.GenerateComponentXsd;
import com.porua.codegen.jaxb.GenerateJaxbClass;
import com.porua.codegen.palette.GeneratePaletteClass;
import com.porua.codegen.palette.GeneratePaletteJar;
import com.porua.codegen.spring.GenerateSpringAssets;
import com.porua.mojo.GenJarMojo;
import com.porua.mojo.GenJavaMojo;
import com.porua.mojo.GenPaletteMojo;
import com.porua.mojo.GenXsdMojo;

public class GenerateCode {
	public static String SRC_PATH = "./src/main/java/";
	public static String TARGET_PATH = "./target/classes/";
	public static String META_INF_DIR = "./src/main/resources/META-INF/";
	public static String PALETTE_JAR_PATH = "./palette/";

	public static String SPRING_ASSET_PACKAGE_NAME;
	public static String SPRING_ASSET_PACKAGE_PATH;
	public static String SPRING_CONNECTOR_SCHEMA_NAME = "connector-schema.xsd";
	public static String SPRING_HANDLERS_FILE = META_INF_DIR + "spring.handlers";
	public static String SPRING_SCHEMAS_FILE = META_INF_DIR + "spring.schemas";

	public static String PALETTE_PACKAGE_NAME;
	public static String PALETTE_PACKAGE_PATH;
	public static String JAXB_PACKAGE_NAME;
	public static String JAXB_PACKAGE_PATH;

	/**
	 * Generate all assets. Java sources,spring.handlers,spring.schemas.
	 * {@link GenJavaMojo}
	 * 
	 * @param pkg
	 * @param connectors
	 * @param loader
	 * @throws Exception
	 */
	public static void generateJavaAssets(String pkg, List<String> connectors, ClassLoader loader) throws Exception {
		configure(pkg);
		List<Class<?>> listConnector = loadConnectorClasses(connectors, loader);
		GenerateSpringAssets.generateSpringAssets(listConnector);
		GeneratePaletteClass.generatePaletteAssets(listConnector);
		GenerateJaxbClass.generateJaxbAssets(listConnector);
	}

	/**
	 * Generate Palette Java sources. {@link GenPaletteMojo}
	 * 
	 * @param pkg
	 * @param connectors
	 * @param loader
	 * @throws Exception
	 */
	public static void generatePaletteAssets(String pkg, List<String> connectors, ClassLoader loader) throws Exception {
		configure(pkg);
		List<Class<?>> listConnector = loadConnectorClasses(connectors, loader);
		GeneratePaletteClass.generatePaletteAssets(listConnector);
	}

	/**
	 * Generate XSD. {@link GenXsdMojo}
	 * 
	 * @param pkg
	 * @param connectors
	 * @param loader
	 * @throws Exception
	 */
	public static void generateXsdAssets(String pkg, List<String> connectors, ClassLoader loader) throws Exception {
		configure(pkg);
		GenerateComponentXsd.generateXsdAssets(loader);

	}

	/**
	 * Generate palette jar. {@link GenJarMojo}
	 * 
	 * @param pkg
	 * @param jarName
	 * @param connectors
	 * @param loader
	 * @throws Exception
	 */
	public static void generatePaletteAssetsJar(String pkg, String jarName, List<String> connectors, ClassLoader loader) throws Exception {
		configure(pkg);
		GeneratePaletteJar.generatePaletteAssetsJar(jarName);
	}

	/**
	 * Configure names, paths etc.
	 * 
	 * @param pkg
	 * @throws Exception
	 */
	public static void configure(String pkg) throws Exception {
		Files.createDirectories(Paths.get(META_INF_DIR));
		SPRING_ASSET_PACKAGE_NAME = pkg.concat(".").concat("spring");
		SPRING_ASSET_PACKAGE_PATH = SPRING_ASSET_PACKAGE_NAME.replaceAll("\\.", "/") + "/";

		PALETTE_PACKAGE_NAME = pkg.concat(".").concat("palette");
		PALETTE_PACKAGE_PATH = PALETTE_PACKAGE_NAME.replaceAll("\\.", "/").concat("/");

		JAXB_PACKAGE_NAME = pkg.concat(".").concat("jaxb");
		JAXB_PACKAGE_PATH = JAXB_PACKAGE_NAME.replaceAll("\\.", "/").concat("/");

	}

	/**
	 * Load connector classes based on their names.
	 * 
	 * @param connectors
	 * @param loader
	 * @return
	 * @throws Exception
	 */
	private static List<Class<?>> loadConnectorClasses(List<String> connectors, ClassLoader loader) throws Exception {
		List<Class<?>> listConnector = new ArrayList<>();
		for (String className : connectors) {
			Class<?> clazz = loader.loadClass(className);
			listConnector.add(clazz);
		}
		return listConnector;
	}
}
