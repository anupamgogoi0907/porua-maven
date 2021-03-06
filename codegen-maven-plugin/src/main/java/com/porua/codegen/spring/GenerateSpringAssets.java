package com.porua.codegen.spring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.w3c.dom.Element;

import com.porua.codegen.GenerateCode;
import com.porua.core.PoruaBeanDefinitionParser;
import com.porua.core.tag.ConfigProperty;
import com.porua.core.tag.Connector;
import com.porua.core.tag.ConnectorConfig;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public class GenerateSpringAssets {

	public static void generateSpringAssets(List<Class<?>> listConnector) throws Exception {
		for (Class<?> clazz : listConnector) {
			createBeanDefinitionParser(clazz);
		}
		createNamespaceHandler(listConnector);
		createSpringFiles(listConnector.get(0));
	}

	/**
	 * Generate bean definition java source for connector.
	 * 
	 * @param clazz
	 * @throws Exception
	 */
	private static void createBeanDefinitionParser(Class<?> clazz) throws Exception {
		CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("builder.setScope($T.SCOPE_PROTOTYPE);", BeanDefinition.class);
		for (Field field : clazz.getDeclaredFields()) {
			ConfigProperty configPropAnnot = field.getAnnotation(ConfigProperty.class);
			ConnectorConfig configAnnot = field.getAnnotation(ConnectorConfig.class);

			// Simple property.
			if (configPropAnnot != null) {
				builder.add(CodeBlock.of("String " + field.getName() + "= element.getAttribute($S);", field.getName()));
				builder.add(CodeBlock.of("builder.addPropertyValue(" + "$S" + "," + field.getName() + ");", field.getName()));
			}
			// Separate configuration class.
			if (configAnnot != null) {
				builder.add(CodeBlock.of("String configRef=element.getAttribute($S);", configAnnot.configName()));
				builder.add(CodeBlock.of("$1T configRefBean = new $2T(configRef);", RuntimeBeanReference.class, RuntimeBeanReference.class));
				builder.add(CodeBlock.of("builder.addPropertyValue(" + "$S" + "," + "configRefBean" + ");", field.getName()));
				createConnectorConfigBeanDefinitionParser(field.getType());
			}

		}

		// Constructor
		MethodSpec methodConstructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addStatement("super($T.class)", clazz).build();

		// doParse()
		ParameterSpec p1 = ParameterSpec.builder(Element.class, "element").build();
		ParameterSpec p2 = ParameterSpec.builder(BeanDefinitionBuilder.class, "builder").build();

		MethodSpec methgodDoParse = MethodSpec.methodBuilder("doParse").addModifiers(Modifier.PROTECTED).returns(void.class).addParameters(Arrays.asList(p1, p2)).addCode(builder.build()).build();

		// Create java file.
		TypeSpec className = TypeSpec.classBuilder(clazz.getSimpleName() + "DefinitionParser").addModifiers(Modifier.PUBLIC).superclass(PoruaBeanDefinitionParser.class).addMethods(Arrays.asList(methodConstructor, methgodDoParse)).build();
		createFile(className);
	}

	/**
	 * Generate bean definition java source for connector configuration.
	 * 
	 * @param clazz
	 * @throws Exception
	 */
	private static void createConnectorConfigBeanDefinitionParser(Class<?> clazz) throws Exception {
		CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("builder.setScope($T.SCOPE_PROTOTYPE);", BeanDefinition.class);
		for (Field field : clazz.getDeclaredFields()) {
			ConfigProperty configPropAnnot = field.getAnnotation(ConfigProperty.class);

			// Simple property.
			if (configPropAnnot != null) {
				builder.add(CodeBlock.of("String " + field.getName() + "= element.getAttribute($S);", field.getName()));
				builder.add(CodeBlock.of("builder.addPropertyValue(" + "$S" + "," + field.getName() + ");", field.getName()));
			}

		}

		// Constructor
		MethodSpec methodConstructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addStatement("super($T.class)", clazz).build();

		// doParse()
		ParameterSpec p1 = ParameterSpec.builder(Element.class, "element").build();
		ParameterSpec p2 = ParameterSpec.builder(BeanDefinitionBuilder.class, "builder").build();

		MethodSpec methgodDoParse = MethodSpec.methodBuilder("doParse").addModifiers(Modifier.PROTECTED).returns(void.class).addParameters(Arrays.asList(p1, p2)).addCode(builder.build()).build();

		// Create java file.
		TypeSpec className = TypeSpec.classBuilder(clazz.getSimpleName() + "DefinitionParser").addModifiers(Modifier.PUBLIC).superclass(PoruaBeanDefinitionParser.class).addMethods(Arrays.asList(methodConstructor, methgodDoParse)).build();
		createFile(className);

	}

	/**
	 * All connectors inside the same project must have the same namespace.
	 * 
	 * @param listConnector
	 * @throws Exception
	 */
	private static void createNamespaceHandler(List<Class<?>> listConnector) throws Exception {
		CodeBlock.Builder builder = CodeBlock.builder();
		for (Class<?> clazz : listConnector) {
			Connector annotation = clazz.getAnnotation(Connector.class);

			// Connector
			String tag = annotation.tagName();
			builder.add("registerBeanDefinitionParser($S," + "new " + clazz.getSimpleName() + "DefinitionParser()" + ");", tag);

			// Connector Configuration
			Field configField = Arrays.asList(clazz.getDeclaredFields()).stream().filter(f -> f.getAnnotation(ConnectorConfig.class) != null).findFirst().orElse(null);
			if (configField != null) {
				ConnectorConfig config = configField.getAnnotation(ConnectorConfig.class);
				builder.add("registerBeanDefinitionParser($S," + "new " + configField.getType().getSimpleName() + "DefinitionParser()" + ");", config.tagName());
			}
		}
		// init()
		MethodSpec methodInit = MethodSpec.methodBuilder("init").addModifiers(Modifier.PUBLIC).returns(void.class).addStatement(builder.build()).build();

		// Create java file.
		TypeSpec className = TypeSpec.classBuilder("NamespaceHandler").addModifiers(Modifier.PUBLIC).superclass(NamespaceHandlerSupport.class).addMethods(Arrays.asList(methodInit)).build();
		createFile(className);
	}

	/**
	 * Generate assets under META-INF for spring custom tags.
	 * 
	 * @param clazz
	 * @throws Exception
	 */
	private static void createSpringFiles(Class<?> clazz) throws Exception {
		Connector annotation = clazz.getAnnotation(Connector.class);

		// spring.handlers
		File handlersFile = new File(GenerateCode.SPRING_HANDLERS_FILE);
		String ns = annotation.tagNamespace();
		ns = "http\\:" + ns.substring(5) + "=" + GenerateCode.SPRING_ASSET_PACKAGE_NAME + "." + "NamespaceHandler";
		createFile(handlersFile, ns);

		// spring.schemas
		File schemasFile = new File(GenerateCode.SPRING_SCHEMAS_FILE);
		String tagSchema = annotation.tagSchemaLocation();
		tagSchema = "http\\:" + tagSchema.substring(5) + "=" + GenerateCode.SPRING_ASSET_PACKAGE_PATH + GenerateCode.SPRING_CONNECTOR_SCHEMA_NAME;
		createFile(schemasFile, tagSchema);
	}

	/**
	 * Create spring asset files e.g spring.handlers & spring.schemas
	 * 
	 * @param file
	 * @param content
	 * @throws Exception
	 */
	private static void createFile(File file, String content) throws Exception {
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
		writer.close();
	}

	/**
	 * Create source java file.
	 * 
	 * @param className
	 * @throws Exception
	 */
	private static void createFile(TypeSpec className) throws Exception {
		JavaFile javaFile = JavaFile.builder(GenerateCode.SPRING_ASSET_PACKAGE_NAME, className).build();
		File file = new File(GenerateCode.SRC_PATH);
		javaFile.writeTo(System.out);
		javaFile.writeTo(file);
	}

}
