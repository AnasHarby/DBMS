package dbms.connection;

import java.util.Collection;
import java.util.Map;

import dbms.exception.DatabaseNotFoundException;
import dbms.exception.SyntaxErrorException;
import dbms.exception.TableNotFoundException;
import dbms.sqlparser.sqlInterpreter.Condition;
import dbms.util.ResultSet;
import dbms.xml.XMLParser;

public class XMLConnection implements Connection {

	private static XMLConnection instance = null;

	private String dbName;

	private XMLConnection() {

	}

	public static XMLConnection getInstance() {
		if (instance == null)
			instance = new XMLConnection();
		return instance;
	}

	@Override
	public void createDatabase(String dbName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropDatabase(String dbName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createTable(String tableName, Map<String, Class> columns) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropTable(String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertIntoTable(String tableName, Map<String, Object> entryMap) {
		// TODO Auto-generated method stub

	}

	@Override
	public ResultSet selectAll(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet selectAll(String tableName, Condition condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet select(String tableName, Collection<String> columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet select(String tableName,
			Collection<String> columns, Condition condition) throws DatabaseNotFoundException, TableNotFoundException, SyntaxErrorException {
		return XMLParser.getInstance().select(dbName, tableName, condition, columns);
	}

	@Override
	public void delete(String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String tableName, Condition condition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(String tableName, Map<String, Object> values,
					   Map<String, String> columns) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(String tableName, Map<String, Object> values,
					   Map<String, String> columns, Condition condition) {
		// TODO Auto-generated method stub
	}

	@Override
	public void useDatabase(String dbName) {
		this.dbName = dbName;
	}

	@Override
	public String getDatabaseName() throws DatabaseNotFoundException {
		if (dbName == null) {
			throw new DatabaseNotFoundException();
		}
		return dbName;
	}
}