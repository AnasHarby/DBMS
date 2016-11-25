package dbms.xml;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dbms.exception.DatabaseNotFoundException;
import dbms.exception.SyntaxErrorException;
import dbms.exception.TableAlreadyCreatedException;
import dbms.exception.TableNotFoundException;
import dbms.sqlparser.sqlInterpreter.Condition;
import dbms.util.Result;
import dbms.util.ResultSet;

public class TableParser {
	private static TableParser instance  = null;
	private static Transformer transformer = null;
	private static DocumentBuilder docBuilder = null;
	private static final String WORKSPACE_DIR =
			System.getProperty("user.home") + "\\databases";
	private static final ResourceBundle CONSTANTS =
			ResourceBundle.getBundle("dbms.xml.Constants");

	private TableParser() {
		try {
			docBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			transformer = TransformerFactory
					.newInstance().newTransformer();
		} catch (TransformerConfigurationException
				| TransformerFactoryConfigurationError
				| ParserConfigurationException e) {
			e.printStackTrace();
		}
		transformer.setOutputProperty(OutputKeys.INDENT,
				"yes");
		transformer.setOutputProperty(
				CONSTANTS.getString("indentation"),
				CONSTANTS.getString("indentation.val"));
	}

	public static TableParser getInstance() {
		if (instance == null) {
			instance = new TableParser();
		}
		return instance;
	}

	public void createTable(String dbName, String tableName,
			Map<String, Class> columns)
			throws DatabaseNotFoundException,
			TableAlreadyCreatedException, SyntaxErrorException {
		File tableFile = new File(openDB(dbName), tableName
				+ CONSTANTS.getString("extension.xml"));
		if (tableFile.exists()) {
			throw new TableAlreadyCreatedException();
		}
		Document doc = docBuilder.newDocument();
		//Table element
		Element table = doc.createElement(CONSTANTS.getString("table.element"));
		table.setAttribute(CONSTANTS.getString("name.attr"), tableName);
		table.setAttribute(CONSTANTS.getString("db.attr"), dbName);
		table.setAttribute(CONSTANTS.getString("rows.attr"), "0");
		doc.appendChild(table);
		addColumns(doc, table, columns);
		transform(doc, tableFile);
	}

	public ResultSet select(String dbName,
			String tableName, Condition condition)
					throws TableNotFoundException, DatabaseNotFoundException {
		File tableFile = openTable(dbName, tableName);
		Document doc = null;
		try {
			doc = docBuilder.parse(tableFile);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		int size = Integer.parseInt(doc.getFirstChild().
				getAttributes().getNamedItem(
						CONSTANTS.getString("rows.attr")).getTextContent());
		NodeList rowList = doc.getElementsByTagName(
				CONSTANTS.getString("row.element"));
		Result[] rows = new Result[size];
		Boolean[] conditionMet = new Boolean[size];
		Arrays.fill(conditionMet, true);
		for (int i = 0; i < rowList.getLength(); i++) {
			Node row = rowList.item(i);
			if (row.getTextContent() == "") {
				continue;
			}
			Node col = row.getParentNode();
			int index = Integer.parseInt(((Element) row).getAttribute(
					CONSTANTS.getString("index.val")));
			if (!conditionMet[index]) {
				continue;
			}
			if (rows[index] == null) {
				rows[index] = new Result();
			}
			String name = col.getAttributes()
					.getNamedItem(CONSTANTS
							.getString("name.attr")).getTextContent();
			String type = col.getAttributes()
					.getNamedItem(CONSTANTS
							.getString("type.attr")).getTextContent();
			if (type.equals("Integer")) {
				rows[index].add(name, Integer.parseInt(
						row.getTextContent()));
			} else if (type.equals("String")) {
				rows[index].add(name, row.getTextContent());
			}
		}
		ResultSet results =
				new ResultSet();
		for (int i = 0; i < size; i++) {
			results.add(rows[i]);
		}
		return results;
	}

	private File openTable(String dbName, String tableName)
			throws DatabaseNotFoundException,
			TableNotFoundException {
		File tableFile = new File(openDB(dbName), tableName
				+ CONSTANTS.getString("extension.xml"));
		if (!tableFile.exists()) {
			throw new TableNotFoundException();
		}
		return tableFile;
	}

	private void addColumns(Document doc, Node table,
			Map<String, Class> columns) throws SyntaxErrorException {
		for (Map.Entry<String, Class> col : columns.entrySet()) {
			String name = col.getKey();
			String type = ParserUtil.getClassName(col.getValue());
			if (type == null) {
				throw new SyntaxErrorException();
			}
			Element column = doc.createElement(
					CONSTANTS.getString("column.element"));
			column.setAttribute(
					CONSTANTS.getString("name.attr"), name);
			column.setAttribute(
					CONSTANTS.getString("type.attr"), type);
			table.appendChild(column);
		}
	}

	public void insertIntoTable(String dbName, String tableName,
			Map<String, Object> entryMap)
			throws DatabaseNotFoundException,
			TableNotFoundException, SyntaxErrorException {
		File tableFile = openTable(dbName, tableName);
		Document doc = null;
		try {
			doc = docBuilder.parse(tableFile);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		addRow(doc, entryMap);
		transform(doc, tableFile);
	}

	private void addRow(Document doc,
			Map<String, Object> entryMap) throws SyntaxErrorException {
		Node rowsAttr = doc.getFirstChild()
				.getAttributes().getNamedItem(
						CONSTANTS.getString("rows.attr"));
		int index = Integer.parseInt(
				rowsAttr.getTextContent());
		rowsAttr.setTextContent(Integer.toString(index + 1));
		NodeList cols = doc.getElementsByTagName(
				CONSTANTS.getString("column.element"));
		insertRowData(doc, index, cols, entryMap);

	}

	private void insertRowData(Document doc, int index,
			NodeList cols, Map<String, Object> entryMap)
					throws SyntaxErrorException {
		if (!ParserUtil.validateColumnEntries(entryMap, cols)) {
			throw new SyntaxErrorException();
		}
		Map<Node, Boolean> inserted = new HashMap<Node, Boolean>();
		for (Map.Entry<String, Object> entry : entryMap.entrySet()) {
			Node col = ParserUtil
					.getColumnFromNodeList(entry.getKey(), cols);
			inserted.put(col, true);
			Node newRow =
					getNewRowContent(doc, index, ParserUtil
							.getObjectClassName(entry.getValue()), entry.getValue());
			col.appendChild(newRow);
		}
		for (int i = 0; i < cols.getLength(); i++) {
			Node col = cols.item(i);
			if (inserted.get(col) == null) {
				String type = col.getAttributes()
						.getNamedItem(CONSTANTS.getString("type.attr")).getTextContent();
				Node newRow =
						getNewRowContent(doc, index, type, null);
				col.appendChild(newRow);
			}
		}
	}

	private Node getNewRowContent(Document doc, int index,
			String type, Object value) throws SyntaxErrorException {
		Element row =
				doc.createElement(CONSTANTS.getString("row.element"));
		row.setAttribute(CONSTANTS.getString("index.val"), Integer.toString(index));
		String content = ParserUtil.getObjectStringValue(value, type);
		if (content == null) {
			throw new SyntaxErrorException();
		}
		row.setTextContent(content);
		return row;
	}

	private File openDB(String dbName)
			throws DatabaseNotFoundException {
		File database = new File(WORKSPACE_DIR + "\\" + dbName);
		if (!database.exists()) {
			throw new DatabaseNotFoundException();
		}
		return database;
	}


	private void transform(Document doc, File tableFile) {
		DOMSource source = new DOMSource(doc);
		StreamResult result =
				new StreamResult(tableFile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	protected Document getDocument(String dbName, String tableName)
			throws DatabaseNotFoundException {
		File database = new File(WORKSPACE_DIR + "\\" + dbName);
		if (!database.exists()) {
			throw new DatabaseNotFoundException();
		}
		File schema = new File(database, tableName
				+ CONSTANTS.getString("extension.schema"));
		if (!schema.exists()) {
			return null;
		}
		Document doc = docBuilder.newDocument();
		return doc;
	}

	private NodeList getColumnsNodeList(String dbName, String tableName)
			throws DatabaseNotFoundException {
		Document doc = getDocument(dbName, tableName);
		if (doc == null) {
			return null;
		}
		NodeList cols = doc.getElementsByTagName(
				CONSTANTS.getString("column.element"));
		return cols;
	}

	protected Map<String, String> getColumns(String dbName, String tableName)
			throws DatabaseNotFoundException {
		NodeList colsList = getColumnsNodeList(dbName, tableName);
		return getColumns(colsList);
	}

	private Map<String, String> getColumns(NodeList colsList) {
		if (colsList == null) {
			return null;
		}
		Map<String, String> cols = new HashMap<String, String>();
		for (int i = 0; i < colsList.getLength(); i++) {
			Node col = colsList.item(i);
			String name = col.getAttributes()
					.getNamedItem(CONSTANTS.getString("name.attr"))
					.getTextContent();
			String type = col.getAttributes()
					.getNamedItem(CONSTANTS.getString("type.attr"))
					.getTextContent();
			cols.put(name, type);
		}
		return cols;
	}
}