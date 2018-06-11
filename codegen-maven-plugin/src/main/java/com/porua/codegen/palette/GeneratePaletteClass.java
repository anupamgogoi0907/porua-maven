package com.porua.codegen.palette;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.porua.codegen.GenerateCode;
import com.porua.core.tag.ConfigProperty;
import com.porua.core.tag.Connector;
import com.porua.core.tag.ConnectorConfig;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class GeneratePaletteClass {

	public static void generatePaletteAssets(List<Class<?>> listConnector) {
		listConnector.forEach(clazz -> {
			try {
				createConnectorPalette(clazz);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Generate Java source file for connector palette.
	 * 
	 * @param clazz
	 * @throws Exception
	 */
	private static void createConnectorPalette(Class<?> clazz) throws Exception {
		Connector connectAnnot = clazz.getAnnotation(Connector.class);

		// Class Annotation
		AnnotationSpec.Builder as = AnnotationSpec.builder(Connector.class);
		as.addMember("tagName", CodeBlock.of("$S", connectAnnot.tagName()));
		as.addMember("tagNamespace", CodeBlock.of("$S", connectAnnot.tagNamespace()));
		as.addMember("tagSchemaLocation", CodeBlock.of("$S", connectAnnot.tagSchemaLocation()));
		as.addMember("imageName", CodeBlock.of("$S", connectAnnot.imageName()));

		// Enum & Fields
		List<TypeSpec> listEnum = new ArrayList<>();
		List<FieldSpec> listFields = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			FieldSpec fs = null;
			ConfigProperty configPropAnnot = field.getAnnotation(ConfigProperty.class);
			ConnectorConfig configAnnot = field.getAnnotation(ConnectorConfig.class);

			// Simple property.
			if (configPropAnnot != null) {
				if (configPropAnnot.enumClass() == Void.class) {
					fs = FieldSpec.builder(field.getType(), field.getName()).addAnnotation(ConfigProperty.class).build();
				} else {
					Map<FieldSpec, TypeSpec> map = createEnum(field, configPropAnnot.enumClass());
					listFields.add(map.keySet().iterator().next());
					listEnum.add(map.values().iterator().next());
				}
			}
			// Separate configuration class.
			if (configAnnot != null) {
				AnnotationSpec.Builder asConfig = AnnotationSpec.builder(ConnectorConfig.class);
				asConfig.addMember("configName", CodeBlock.of("$S", configAnnot.configName()));
				asConfig.addMember("tagName", CodeBlock.of("$S", configAnnot.tagName()));

				TypeName tn = createConnectorConfigPalette(field.getType());
				fs = FieldSpec.builder(tn, field.getName()).addAnnotation(asConfig.build()).build();
			}

			if (fs != null) {
				listFields.add(fs);
			}
		}

		String className = clazz.getSimpleName() + "Palette";
		TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addAnnotation(as.build()).addTypes(listEnum).addFields(listFields).build();
		createFile(typeSpec);
	}

	/**
	 * Generate Java source file connector configuration.
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	private static TypeName createConnectorConfigPalette(Class<?> clazz) throws Exception {
		List<TypeSpec> listEnum = new ArrayList<>();
		List<FieldSpec> listField = new ArrayList<>();

		// Simple property.
		Arrays.asList(clazz.getDeclaredFields()).forEach(f -> {
			try {
				ConfigProperty configPropAnnot = f.getAnnotation(ConfigProperty.class);
				if (configPropAnnot != null) {
					if (configPropAnnot.enumClass() == Void.class) {
						FieldSpec fs = FieldSpec.builder(f.getType(), f.getName()).addAnnotation(ConfigProperty.class).build();
						listField.add(fs);
					} else {
						Map<FieldSpec, TypeSpec> map = createEnum(f, configPropAnnot.enumClass());
						listField.add(map.keySet().iterator().next());
						listEnum.add(map.values().iterator().next());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		});

		String className = clazz.getSimpleName() + "Palette";
		TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addTypes(listEnum).addFields(listField).build();
		createFile(typeSpec);
		ClassName cn = ClassName.get(GenerateCode.PALETTE_PACKAGE_NAME, className);
		return cn.box();
	}

	/**
	 * Generate Enum .
	 * 
	 * @param fieldEnum
	 * @param classEnum
	 * @return
	 * @throws Exception
	 */
	private static Map<FieldSpec, TypeSpec> createEnum(Field fieldEnum, Class<?> classEnum) throws Exception {
		String enumName = classEnum.getSimpleName();

		// Enum
		TypeSpec.Builder tsb = TypeSpec.enumBuilder(enumName);
		for (Object v : classEnum.getEnumConstants()) {
			Enum<?> e = (Enum<?>) v;
			tsb.addEnumConstant(e.name());
			tsb.addEnumConstant(e.name());
		}

		// Field
		ClassName cn = ClassName.get("", enumName);
		AnnotationSpec.Builder asFieldAttribute = AnnotationSpec.builder(ConfigProperty.class);
		asFieldAttribute.addMember("enumClass", CodeBlock.of("$T.class", cn.box()));
		FieldSpec.Builder fsb = FieldSpec.builder(fieldEnum.getType(), fieldEnum.getName(), Modifier.PRIVATE).addAnnotation(asFieldAttribute.build());

		Map<FieldSpec, TypeSpec> result = new HashMap<>();
		result.put(fsb.build(), tsb.build());
		return result;
	}

	/**
	 * Create file.
	 * 
	 * @param className
	 * @throws Exception
	 */
	private static void createFile(TypeSpec className) throws Exception {
		JavaFile javaFile = JavaFile.builder(GenerateCode.PALETTE_PACKAGE_NAME, className).build();
		File file = new File(GenerateCode.SRC_PATH);
		javaFile.writeTo(System.out);
		javaFile.writeTo(file);
	}
}
