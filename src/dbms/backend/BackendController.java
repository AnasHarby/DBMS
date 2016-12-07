package dbms.backend;

import java.util.Collection;
import java.util.Map;

import dbms.datatypes.DatatypeDBMS;
import dbms.datatypes.DatatypeFactory;
import dbms.exception.DatabaseAlreadyCreatedException;
import dbms.exception.DatabaseNotFoundException;
import dbms.exception.IncorrectDataEntryException;
import dbms.exception.SyntaxErrorException;
import dbms.exception.TableAlreadyCreatedException;
import dbms.exception.TableNotFoundException;
import dbms.exception.TypeNotSupportedException;
import dbms.sqlparser.SQLParser;
import dbms.sqlparser.sqlInterpreter.Condition;
import dbms.util.Column;
import dbms.util.Database;
import dbms.util.ResultSet;
import dbms.util.Table;

/**
 * Controller that controls data entry and queries between
 * {@link SQLParser} and the actual back-end writing.
 *
 */
public class BackendController {
	private static BackendController instance = null;
	private String dbName;

	private BackendController() {

	}

	/**
	 * Gets the static instance of Controller.
	 * @return instance
	 */
	public static BackendController getInstance() {
		if (instance == null) {
			instance = new BackendController();
		}
		return instance;
	}

	/**
	 * Creates database inside system-dependent user's
	 * home path.
	 * @param dbName Name of database.
	 * @throws DatabaseAlreadyCreatedException
	 */
	public void createDatabase(String dbName)
			throws DatabaseAlreadyCreatedException {
		this.dbName = dbName;
		BackendParser.createDatabase(new Database(dbName));
	}

	/**
	 * Drops database from path.
	 * @param dbName Name of database.
	 * @throws DatabaseNotFoundException
	 */
	public void dropDatabase(String dbName)
			throws DatabaseNotFoundException {
		BackendParser.dropDatabase(new Database(dbName));
	}

	/**
	 * Creates table inside used database.
	 * @param tableName Name of database.
	 * @param columns {@link Map} between names of given columns and
	 * their data type.
	 * @throws TableAlreadyCreatedException
	 * @throws DatabaseNotFoundException
	 * @throws TypeNotSupportedException
	 * @throws IncorrectDataEntryException
	 */
	public void createTable(String tableName, Map<String, Class> columns)
			throws DatabaseNotFoundException,
			TableAlreadyCreatedException, IncorrectDataEntryException {
		Table table = new Database(dbName).createTable(tableName);
		for (Map.Entry<String, Class> col : columns.entrySet()) {
			Class<? extends DatatypeDBMS> type =
					DatatypeFactory.getFactory().getRegisteredDatatype(
							col.getValue().getSimpleName());
			if (type == null) {
				throw new IncorrectDataEntryException("Datatype not supported!");
			}
			table.addColumn(new Column(col.getKey(), type));
		}
		BackendParserFactory.getFactory().getCurrentParser().create(table);
		table.clear();
	}

	/**
	 * Deletes table inside used database.
	 * @param tableName Name of table.
	 * @throws TableNotFoundException
	 * @throws DatabaseNotFoundException
	 */
	public void dropTable(String tableName) throws DatabaseNotFoundException {
		BackendParserFactory.getFactory().getCurrentParser().dropTable(
				dbName, tableName);
	}

	/**
	 * Inserts new data into table
	 * @param tableName Name of table.
	 * @param entryMap {@link Map} between column names
	 * and objects to be inserted.
	 * @throws TableNotFoundException
	 * @throws DatabaseNotFoundException
	 * @throws TypeNotSupportedException
	 * @throws IncorrectDataEntryException
	 */
	public void insertIntoTable(String tableName, Map<String, Object> entryMap)
			throws DatabaseNotFoundException,
			TableNotFoundException, IncorrectDataEntryException {
		Table table = new Database(dbName).createTable(tableName);
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		table.insertRow(entryMap);
		BackendParserFactory.getFactory().getCurrentParser().writeTo(table);
		table.clear();
	}

	/**
	 * Selects data from database given a certain condition,
	 * the result is stored after in a {@link ResultSet}.
	 * @param tableName Name of table inside database.
	 * @param condition {@link Condition} condition for data selection,
	 * can be null.
	 * @param columns {@link Collection<String>} columns to select from.
	 * @return {@link ResultSet} Set of returned data.
	 * @throws TableNotFoundException
	 * @throws DatabaseNotFoundException
	 * @throws IncorrectDataEntryException
	 * @throws SyntaxErrorException
	 */
	public ResultSet select(String tableName,
			Collection<String> columns, Condition condition)
					throws DatabaseNotFoundException,TableNotFoundException,
					SyntaxErrorException, IncorrectDataEntryException {
		Table table = new Database(dbName).createTable(tableName);
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		ResultSet ret = table.select(columns, condition);
		table.clear();
		return ret;
	}

	/**
	 * Deletes data from database given a certain condition.
	 * @param tableName Name of table.
	 * @param condition {@link Condition} condition for data deletion,
	 * can be null.
	 * @throws DatabaseNotFoundException
	 * @throws TableNotFoundException
	 * @throws SyntaxErrorException
	 * @throws IncorrectDataEntryException
	 */
	public void delete(String tableName, Condition condition)
			throws DatabaseNotFoundException, TableNotFoundException,
			SyntaxErrorException, IncorrectDataEntryException {
		Table table = new Database(dbName).createTable(tableName);
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		table.delete(condition);
		BackendParserFactory.getFactory().getCurrentParser().writeTo(table);
		table.clear();
	}

	/**
	 * Updates data inside database given a certain condition.
	 * @param tableName Name of table.
	 * @param values {@link Map} between column names and
	 * objects to be updated inside database.
	 * @param columns {@link Map} between columns to be updated
	 * with values of other columns.
	 * @param condition {@link Condition} condition for data updating,
	 * can be null.
	 * @throws DatabaseNotFoundException
	 * @throws TableNotFoundException
	 * @throws SyntaxErrorException
	 * @throws IncorrectDataEntryException
	 */
	public void update(String tableName, Map<String, Object> values,
			Map<String, String> columns, Condition condition)
					throws DatabaseNotFoundException, TableNotFoundException,
					SyntaxErrorException, IncorrectDataEntryException {
		Table table = new Database(dbName).createTable(tableName);
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		table.update(values, columns, condition);
		BackendParserFactory.getFactory().getCurrentParser().writeTo(table);
		table.clear();
	}

	/**
	 * Uses a given database to operate on tables inside of it.
	 * @param dbName Name of database.
	 * @throws DatabaseNotFoundException
	 */
	public void useDatabase(String dbName) throws DatabaseNotFoundException {
		try {
			this.createDatabase(dbName);
			this.dropDatabase(dbName);
			throw new DatabaseNotFoundException();
		} catch (DatabaseAlreadyCreatedException e) {
			this.dbName = dbName;
		}
	}

	/**
	 * Gets name of the currently used database.
	 * @return Name of the currently used database.
	 * @throws DatabaseNotFoundException
	 */
	public String getDatabaseName() throws DatabaseNotFoundException {
		if (dbName == null) {
			throw new DatabaseNotFoundException();
		}
		return dbName;
	}

	/**
	 * adds a new column in table.
	 * @param tableName table name.
	 * @param columnName column name.
	 * @throws TableNotFoundException
	 * @throws DatabaseNotFoundException
	 * @throws IncorrectDataEntryException
     */
	public void alterAdd(String tableName, String columnName, Class datatype)
			throws DatabaseNotFoundException, TableNotFoundException
			, IncorrectDataEntryException {
		Class<? extends DatatypeDBMS> type =
				DatatypeFactory.getFactory().getRegisteredDatatype(
						datatype.getSimpleName());
		if (type == null) {
			throw new IncorrectDataEntryException("Datatype not supported!");
		}
		Table table = new Table(tableName);
		table.attachToDatabase(new Database(dbName));
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		table.alterAdd(columnName, type);
		BackendParserFactory.getFactory().getCurrentParser().writeTo(table);
		table.clear();
	}

	/**
	 * Deletes an existing column from table.
	 * @param tableName table name.
	 * @param columnName column name.
	 * @throws TableNotFoundException
	 * @throws DatabaseNotFoundException
	 * @throws IncorrectDataEntryException
     */
	public void alterDrop(String tableName, String columnName)
			throws DatabaseNotFoundException, TableNotFoundException
			, IncorrectDataEntryException {
		Table table = new Table(tableName);
		table.attachToDatabase(new Database(dbName));
		BackendParserFactory.getFactory().getCurrentParser().load(table);
		table.alterDrop(columnName);
		BackendParserFactory.getFactory().getCurrentParser().writeTo(table);
		table.clear();
	}
}