package com.porua.codegen.jaxb;

import java.io.File;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.porua.codegen.GenerateCode;
import com.porua.core.tag.ConfigProperty;
import com.porua.core.tag.Connector;
import com.porua.core.tag.ConnectorConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

@SuppressWarnings("unused")
public class GenerateJaxbClass {

	public static List<String> generateJaxbAssets(List<Class<?>> listConnector) throws Exception {
		List<String> list = new ArrayList<>();
		for (Class<?> clazz : listConnector) {
			List<String> listTemp = createConnectorJaxbClass(clazz, new ArrayList<>());
			list.addAll(listTemp);
		}
		return list;
	}

	/**
	 * Genrate Jaxb class for the Connector. The connector class must be annonated
	 * with @Connector
	 * 
	 * @param clazz
	 * @param listClassName
	 * @return
	 * @throws Exception
	 */
	private static List<String> createConnectorJaxbClass(Class<?> clazz, List<String> listClassName) throws Exception {
		Connector connectAnnot = clazz.getAnnotation(Connector.class);

		AnnotationSpec.Builder as = AnnotationSpec.builder(XmlRootElement.class);
		as.addMember("name", CodeBlock.of("$S", connectAnnot.tagName()));
		as.addMember("namespace", CodeBlock.of("$S", connectAnnot.tagNamespace()));

		List<TypeSpec> listEnum = new ArrayList<>();
		List<FieldSpec> listFieldSpec = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			FieldSpec.Builder fs = null;
			AnnotationSpec.Builder asAttribute = null;

			// Annotations.
			ConfigProperty configPropAnnot = field.getAnnotation(ConfigProperty.class);
			ConnectorConfig configAnnot = field.getAnnotation(ConnectorConfig.class);

			// Simple property.
			if (configPropAnnot != null) {
				if (configPropAnnot.enumClass() == Void.class) {
					asAttribute = AnnotationSpec.builder(XmlAttribute.class);
					asAttribute.addMember("name", CodeBlock.of("$S", field.getName()));
					fs = FieldSpec.builder(field.getType(), field.getName(), Modifier.PRIVATE).addAnnotation(asAttribute.build());
				} else {
					Map<FieldSpec, TypeSpec> map = createEnum(field, configPropAnnot.enumClass());
					listFieldSpec.add(map.keySet().iterator().next());
					listEnum.add(map.values().iterator().next());
				}
			}
			// Separate configuration class.
			if (configAnnot != null) {
				asAttribute = AnnotationSpec.builder(XmlAttribute.class);
				asAttribute.addMember("name", CodeBlock.of("$S", configAnnot.configName()));
				fs = FieldSpec.builder(String.class, configAnnot.configName().replaceAll("[^a-zA-Z0-9]", "")).addAnnotation(asAttribute.build());
				createConnectorConfigJaxbClass(field.getType(), configAnnot.tagName(), connectAnnot.tagNamespace(), listClassName);
			}
			if (fs != null) {
				listFieldSpec.add(fs.build());
			}
		}
		String className = clazz.getSimpleName() + "Jaxb";
		TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addAnnotation(as.build()).addTypes(listEnum).addFields(listFieldSpec).build();
		createFile(typeSpec);

		ClassName cn = ClassName.get(GenerateCode.JAXB_PACKAGE_NAME, className);
		listClassName.add(cn.reflectionName());
		return listClassName;
	}

	/**
	 * Generate Jaxb class for connector configuration class.
	 * 
	 * @param clazz
	 * @param name
	 * @param namespace
	 * @param listClassName
	 * @throws Exception
	 */
	private static void createConnectorConfigJaxbClass(Class<?> clazz, String name, String namespace, List<String> listClassName) throws Exception {
		AnnotationSpec.Builder asClass = AnnotationSpec.builder(XmlRootElement.class);
		asClass.addMember("name", CodeBlock.of("$S", name));
		asClass.addMember("namespace", CodeBlock.of("$S", namespace));

		List<TypeSpec> listEnum = new ArrayList<>();
		List<FieldSpec> listFieldSpec = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			// Simple property.
			ConfigProperty configPropAnnot = field.getAnnotation(ConfigProperty.class);
			if (configPropAnnot != null) {
				if (configPropAnnot.enumClass() == Void.class) {
					AnnotationSpec.Builder asAttribute = AnnotationSpec.builder(XmlAttribute.class);
					asAttribute.addMember("name", CodeBlock.of("$S", field.getName()));
					FieldSpec.Builder fs = FieldSpec.builder(field.getType(), field.getName()).addAnnotation(asAttribute.build());
					listFieldSpec.add(fs.build());
				} else {
					Map<FieldSpec, TypeSpec> map = createEnum(field, configPropAnnot.enumClass());
					listFieldSpec.add(map.keySet().iterator().next());
					listEnum.add(map.values().iterator().next());
				}
			}
		}
		String className = clazz.getSimpleName() + "Jaxb";
		TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addAnnotation(asClass.build()).addTypes(listEnum).addFields(listFieldSpec).build();
		createFile(typeSpec);

		ClassName cn = ClassName.get(GenerateCode.JAXB_PACKAGE_NAME, className);
		listClassName.add(cn.simpleName());
	}

	/**
	 * Generate @XmlEnum annotation.
	 * 
	 * @param fieldEnum
	 * @param classEnum
	 * @return
	 * @throws Exception
	 */
	private static Map<FieldSpec, TypeSpec> createEnum(Field fieldEnum, Class<?> classEnum) throws Exception {
		String enumName = classEnum.getSimpleName();

		// @XmlEnum
		TypeSpec.Builder tsb = TypeSpec.enumBuilder(enumName).addAnnotation(XmlEnum.class);
		for (Object v : classEnum.getEnumConstants()) {
			Enum<?> e = (Enum<?>) v;
			tsb.addEnumConstant(e.name());
			tsb.addEnumConstant(e.name());
		}

		// @XmlAttribute
		ClassName cn = ClassName.get("", enumName);
		AnnotationSpec.Builder asFieldAttribute = AnnotationSpec.builder(XmlAttribute.class);
		asFieldAttribute.addMember("name", CodeBlock.of("$S", fieldEnum.getName()));
		FieldSpec.Builder fsb = FieldSpec.builder(cn.box(), fieldEnum.getName(), Modifier.PRIVATE).addAnnotation(asFieldAttribute.build());

		Map<FieldSpec, TypeSpec> result = new HashMap<>();
		result.put(fsb.build(), tsb.build());
		return result;
	}

	/**
	 * Create source java file.
	 * 
	 * @param className
	 * @throws Exception
	 */
	private static void createFile(TypeSpec className) throws Exception {
		JavaFile javaFile = JavaFile.builder(GenerateCode.JAXB_PACKAGE_NAME, className).build();
		File file = new File(GenerateCode.SRC_PATH);
		javaFile.writeTo(System.out);
		javaFile.writeTo(file);
	}

	/**
	 * Default value generator. TODO: Improve it.
	 * 
	 * @param fs
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private static FieldSpec.Builder getDefaultValue(FieldSpec.Builder fs, String type) throws Exception {
		if (type.contains("int") || type.contains("Integer")) {
			fs.initializer(CodeBlock.of("$L", Integer.MAX_VALUE));
			return fs;
		} else if (type.contains("String")) {
			fs.initializer(CodeBlock.of("$S", "somevalue"));
			return fs;
		} else if (type.contains("float") || type.equals("Float")) {
			fs.initializer(CodeBlock.of("$L", Float.MAX_VALUE));
			return fs;
		} else if (type.contains("double") || type.equals("Double")) {
			fs.initializer(CodeBlock.of("$L", Double.MAX_VALUE));
			return fs;
		} else {
			return fs;
		}
	}

}
