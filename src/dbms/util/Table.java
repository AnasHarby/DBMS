package dbms.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dbms.datatypes.DatatypeDBMS;
import dbms.exception.DatabaseNotFoundException;
import dbms.exception.IncorrectDataEntryException;
import dbms.exception.SyntaxErrorException;
import dbms.exception.TableNotFoundException;
import dbms.sqlparser.sqlInterpreter.Condition;

public class Table {
	private Database database = null;
	private String name = null;
	private List<Column> columns = null;
	private int size;

	public Table(String name) {
		this.name = name;
		columns = new ArrayList<Column>();
		size = 0;
	}

	public Table(String name, Database database) {
		this.name = name;
		this.database = database;
		columns = new ArrayList<Column>();
		size = 0;
	}

	public void attachToDatabase(Database database) {
		this.database = database;
	}

	public void addColumn(Column col) {
		columns.add(col);
	}

	public String getName() {
		return name;
	}

	public Database getDatabase() {
		return database;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public Column getColumn(int index) {
		return columns.get(index);
	}

	public Column getColumn(String name) {
		for (Column col : columns) {
			if (col.getName().equals(name)) {
				return col;
			}
		}
		return null;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void insertRow(Map<String, Object> entryMap)
			throws IncorrectDataEntryException,
			TableNotFoundException, DatabaseNotFoundException {
		if (entryMap == null) {
			return;
		}
		validateValues(entryMap);
		for (Column col : columns) {
			col.addEntry(entryMap.get(col.getName()));
		}
		size++;
	}

	public void delete(Condition condition)
			throws IncorrectDataEntryException, SyntaxErrorException,
			TableNotFoundException, DatabaseNotFoundException {
		Map<String, String> cols =
				mapColumns();
		for (int i = 0; i < size; i++) {
			Map<String, Object> row =
					getRow(i, null);
			if (condition == null || Evaluator.getInstance().evaluate(row,
					condition.getPostfix(), cols)) {
				deleteRow(i);
				i--;
			}
		}
	}

	public ResultSet select(Collection<String> columns, Condition condition)
			throws IncorrectDataEntryException, SyntaxErrorException,
			TableNotFoundException, DatabaseNotFoundException {
		if (columns != null) {
			for (String col : columns) {
				if (!hasColumn(col)) {
					throw new IncorrectDataEntryException("Column not found!");
				}
			}
		}
		ResultSet res = new ResultSet();
		for (int i = 0; i < size; i++) {
			Map<String, Object> row =
					getRow(i, columns);
			if (!row.isEmpty() && (condition == null || Evaluator.getInstance()
					.evaluate(row, condition.getPostfix(), mapColumns()))) {
				res.add(new Result(row));
			}
		}
		return res;
	}

	public void update(Map<String, Object> values, Map<String, String> columns, Condition condition)
			throws IncorrectDataEntryException, SyntaxErrorException,
			TableNotFoundException, DatabaseNotFoundException {
		validateColumns(columns);
		validateValues(values);
		for (int i = 0; i < size; i++) {
			Map<String, Object> row =
					getRow(i, null);
			if (!row.isEmpty() && (condition == null || Evaluator.getInstance()
					.evaluate(row, condition.getPostfix(), mapColumns()))) {
				updateRow(values, columns, i);
			}
		}
	}

	public void alterAdd(String colName, Class<? extends DatatypeDBMS> datatype)
			throws IncorrectDataEntryException {
		Column col = new Column(colName, datatype);
		for (int i = 0; i < size; i++) {
			col.addEntry(null);
		}
		addColumn(col);
	}

	public void alterDrop(String colName)
			throws IncorrectDataEntryException {
		Column col = getColumn(colName);
		if (col == null) {
			throw new IncorrectDataEntryException("Column not found!");
		}
		columns.remove(col);
	}

	private void updateRow(Map<String, Object> values, Map<String, String> columns, int index) {
		if (values != null) {
			for (Map.Entry<String, Object> entry : values.entrySet()) {
				Column col = getColumn(entry.getKey());
				col.set(index, entry.getValue());
			}
		}
		if (columns != null) {
			for (Map.Entry<String, String> entry : columns.entrySet()) {
				Column col1 = getColumn(entry.getKey());
				Column col2 = getColumn(entry.getValue());
				col1.set(index, col2.get(index));
			}
		}
	}

	private Map<String, Object> getRow(int index, Collection<String> columns) {
		Map<String, Object> ret =
				new LinkedHashMap<String, Object>();
		for (Column col : this.columns) {
			if (columns == null
					|| columns.contains(col.getName())) {
				ret.put(col.getName(), col.get(index));
			}
		}
		return ret;
	}

	private void deleteRow(int index) {
		for (Column col : columns) {
			col.removeEntry(index);
		}
		size--;
	}

	public boolean hasColumn(String column) {
		for (Column col : columns) {
			if (col.getName().equals(column)) {
				return true;
			}
		}
		return false;
	}

	private Map<String, String> mapColumns() {
		Map<String, String> cols =
				new HashMap<String, String>();
		for (Column col : columns) {
			String type = null;
			try {
				type = (String) col.getType().getField("KEY").get(col.getType().newInstance());
			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException
					| IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			cols.put(col.getName(), type);
		}
		return cols;
	}

	private void validateColumns(Map<String, String> columns)
			throws IncorrectDataEntryException {
		if (columns == null) {
			return;
		}
		for (Map.Entry<String, String> entry : columns.entrySet()) {
			Column col1 = getColumn(entry.getKey());
			Column col2 = getColumn(entry.getValue());
			if (col1 == null || col2 == null) {
				throw new IncorrectDataEntryException("Column not found!");
			} else if (!col1.getType().equals(col2.getType())) {
				throw new IncorrectDataEntryException("Datatype conflict!");
			}
		}
	}

	private void validateValues(Map<String, Object> values) throws IncorrectDataEntryException {
		if (values == null) {
			return;
		}
		Map<String, String> cols = mapColumns();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			String type = cols.get(entry.getKey());
			if (type == null) {
				throw new IncorrectDataEntryException("Column not found!");
			}
			if (!type.equals(entry.getValue().getClass().getSimpleName())) {
				throw new IncorrectDataEntryException("Datatype conflict!");
			}
		}
	}

	public void clear() {
		for (Column col : columns) {
			col.clear();
		}
		columns.clear();
		size = 0;
	}
}