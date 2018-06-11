package com.porua.codegen.jaxb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd;
import org.apache.xmlbeans.impl.inst2xsd.Inst2XsdOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.porua.codegen.GenerateCode;

@SuppressWarnings("unused")
public class GenerateComponentXsd {

	public static void generateXsdAssets(ClassLoader loader) throws Exception {
		Class<?>[] classes = getJaxbClasses(loader);

		JAXBContext jaxbContext = JAXBContext.newInstance(classes);
		CustomSchemaOutputResolver resolver = new CustomSchemaOutputResolver();
		jaxbContext.generateSchema(resolver);
		List<DOMResult> listResult = resolver.getResult();
		createUnifiedXsd(listResult);
	}

	/**
	 * Get classes from {@link GenerateCode.JAXB_PACKAGE_PATH}
	 * 
	 * @param loader
	 * @return
	 * @throws Exception
	 */
	private static Class<?>[] getJaxbClasses(ClassLoader loader) throws Exception {
		String path = GenerateCode.TARGET_PATH.concat(GenerateCode.JAXB_PACKAGE_PATH);
		File[] files = new File(path).listFiles();
		List<Class<?>> listClass = new ArrayList<>();
		for (File file : files) {
			String fileName = file.getPath().substring(GenerateCode.TARGET_PATH.length()).replaceAll("/", "\\.");
			fileName = fileName.substring(0, fileName.length() - ".class".length());
			Class<?> clazz = loader.loadClass(fileName);
			listClass.add(clazz);
		}

		Class<?>[] classes = listClass.toArray(new Class<?>[listClass.size()]);
		return classes;
	}

	/**
	 * Create the unified xsd.
	 * 
	 * @param listResult
	 * @throws Exception
	 */
	private static void createUnifiedXsd(List<DOMResult> listResult) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();

		// Xsd document.
		Document docFinal = builder.newDocument();

		// Add root element.
		Element elmRoot = docFinal.createElement("xs:schema");
		docFinal.appendChild(elmRoot);

		// Iterate.
		for (DOMResult domResult : listResult) {
			Document doc = (Document) domResult.getNode();
			Node root = doc.getDocumentElement();
			traverse(root, elmRoot, docFinal);
			for (int i = 0; i < root.getAttributes().getLength(); i++) {
				Attr attribue = (Attr) root.getAttributes().item(i);
				if (attribue.getName().equals("targetNamespace")) {
					elmRoot.setAttribute("xmlns:tns", attribue.getValue());
				}
				elmRoot.setAttribute(attribue.getName(), attribue.getValue());
			}
		}
	}

	/**
	 * Traverse recursively a Node and add it to the document.
	 * 
	 * @param root
	 * @param elmRoot
	 * @param docFinal
	 * @throws Exception
	 */
	private static void traverse(Node root, Element elmRoot, Document docFinal) throws Exception {
		NodeList nList = root.getChildNodes();
		for (int i = 0; i < nList.getLength(); i++) {
			Node nextRoot = nList.item(i);

			if (nextRoot.getNodeType() == Node.ELEMENT_NODE && !nextRoot.getNodeName().equals("xs:import")) {
				// Node Element.
				Element elmNextRoot = docFinal.createElement(nextRoot.getNodeName());

				// Node Attributes
				for (int j = 0; j < nextRoot.getAttributes().getLength(); j++) {
					Attr attribue = (Attr) nextRoot.getAttributes().item(j);
					if (attribue.getName().equals("type")) {
						String value = attribue.getValue().startsWith("xs:") == true ? attribue.getValue() : "tns:".concat(attribue.getValue());
						elmNextRoot.setAttribute(attribue.getName(), value);
					} else {
						elmNextRoot.setAttribute(attribue.getName(), attribue.getValue());
					}
				}
				elmRoot.appendChild(elmNextRoot);

				// Recursion.
				traverse(nextRoot, elmNextRoot, docFinal);
			}
		}
		prettyPrint(docFinal);
	}

	/**
	 * 
	 * @param xml
	 * @throws Exception
	 */
	private static final void prettyPrint(Document xml) throws Exception {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Writer out = new StringWriter();
		String filePath = GenerateCode.SRC_PATH.concat(GenerateCode.SPRING_ASSET_PACKAGE_PATH).concat(GenerateCode.SPRING_CONNECTOR_SCHEMA_NAME);
		Files.createDirectories(Paths.get(filePath).getParent());
		FileWriter fout = new FileWriter(new File(filePath));
		tf.transform(new DOMSource(xml), new StreamResult(fout));
	}

	/**
	 * Generate DOM of the xsd and store in list.
	 * 
	 * @author ac-agogoi
	 *
	 */
	static class CustomSchemaOutputResolver extends SchemaOutputResolver {

		List<DOMResult> list = new ArrayList<>();

		@Override
		public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
			DOMResult result = new DOMResult();
			result.setSystemId(suggestedFileName);
			list.add(result);
			return result;
		}

		public List<DOMResult> getResult() {
			return list;
		}
	}

	/**
	 * Generate XML.
	 * 
	 * @param classes
	 * @throws Exception
	 */
	@Deprecated
	private static void xsdGen(Class<?>... classes) throws Exception {
		JAXBContext context = JAXBContext.newInstance(classes);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		List<String> listXml = new ArrayList<>();
		for (Class<?> clazz : classes) {
			StringWriter writer = new StringWriter();
			m.marshal(clazz.newInstance(), writer);
			String xml = writer.toString();
			listXml.add(xml);
		}
		parseXsd(listXml);
	}

	/**
	 * Generate XSD from XML.
	 * 
	 * @param listXml
	 * @throws Exception
	 */
	@Deprecated
	private static void parseXsd(List<String> listXml) throws Exception {

		XmlObject[] xmlInsances = new XmlObject[listXml.size()];
		for (int i = 0; i < listXml.size(); i++) {
			xmlInsances[i] = XmlObject.Factory.parse(listXml.get(i));
		}

		Inst2XsdOptions inst2XsdOptions = new Inst2XsdOptions();
		inst2XsdOptions.setDesign(Inst2XsdOptions.DESIGN_VENETIAN_BLIND);
		SchemaDocument[] docArr = Inst2Xsd.inst2xsd(xmlInsances, inst2XsdOptions);

		if (docArr != null) {
			String filePath = GenerateCode.SRC_PATH.concat(GenerateCode.SPRING_ASSET_PACKAGE_PATH).concat(GenerateCode.SPRING_CONNECTOR_SCHEMA_NAME);
			Files.createDirectories(Paths.get(filePath).getParent());
			File file = new File(filePath);
			docArr[0].save(file);
		}

	}
}
