package dbms.util;

import dbms.datatypes.DBDatatype;
import javafx.util.Pair;

import java.util.*;

/**
 * Data holder for a collection of records.
 */
public class RecordSet implements Iterable<Record>, Cloneable {
	private List<Record> records;
	private List<String> columns;
	private int i;

	/**
	 * Constructor for a {@link RecordSet}, initiates an empty
	 * set.
	 */
    public RecordSet() {
		records = new ArrayList<>();
		columns = new ArrayList<>();
		i = 0;
	}

	/**
	 * Constructor for a {@link RecordSet} that takes in an {@link ArrayList}
	 * if records and copies data from it.
	 * @param records {@link ArrayList} of records.
	 */
    public RecordSet(Collection<Record> records) {
		this.records = new ArrayList<>();
		columns = new ArrayList<>();
		for (Record res : records) {
			if (res == null) {
				continue;
			}
			this.records.add(res);
		}
	}

	public void setColumnList(List<String> columnList) {
    	columns = columnList;
	}

	public List<String> getColumnList() {
    	return columns;
	}

	/**
	 * Gets a {@link List} representation of the set.
	 * @return {@link List} if records.
	 */
    public List<Map<String, DBDatatype>> getMapList() {
		List<Map<String, DBDatatype>> mapRepresent = new LinkedList<Map<String, DBDatatype>>();
		for (int j = 0; j < records.size(); j++) {
			mapRepresent.add(records.get(j).getRecord());
		}

		return mapRepresent;
	}

	/**
	 * Adds a new {@link Record} to the set of records.
	 * @param record {@link Record} to be added.
	 */
	public void add(Record record) {
		records.add(record);
	}


    /**
     * Add all elements of {@link Record} in given Collection to records.
     * @param records {@link Collection<Record>}.
     */
    public void addAll(Collection<Record> records) {
        for (Record record : records) {
            records.add(record);
        }
    }

	/**
	 * Gets number of records in set.
	 * @return size.
	 */
	public int size() {
		return records.size();
	}

	/**
	 * Checks if a set has a next result when using an iterator.
	 * @return boolean value.
	 */
	public boolean hasNext() {
		if (i < records.size() - 1) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the next result in line when using an iterator.
	 * @return next {@link Record}.
	 */
	public Record next() {
		if (hasNext()) {
			i++;
			Record record = records.get(i);
			return record;
		}
		return null;
	}

	public boolean hasPrev() {
		if (i > 0) {
			return true;
		}
		return false;
	}

	public Record prev() {
		if (hasPrev()) {
			i--;
			Record record = records.get(i);
			return record;
		}
		return null;
	}

	public Record curr() {
		if (isEmpty()) {
			return null;
		}
		return records.get(i);
	}

	@Override
	public Iterator<Record> iterator() {
		Iterator<Record> it = records.iterator();
		return it;
	}

	/**
	 * Checks if the result set is empty.
	 * @return boolean value.
	 */
	public boolean isEmpty() {
		return records.isEmpty();
	}


	/**
	 *
	 * @param columns
     */
	public void orderBy(final List<Pair<String, Boolean>> columns) {
        DBComparatorChain<Record> comparatorChain = new DBComparatorChain<>();
        for (Pair<String, Boolean> pair : columns) {
            Comparator<Record> recordComparator = new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    DBDatatype value1 = o1.get(pair.getKey());
                    DBDatatype value2 = o2.get(pair.getKey());
                    if (value1 == null || value2 == null) {
                        return -1;
                    } else {
                        return value1.compareTo(value2);
                    }
                }
            };
            comparatorChain.addComparator(recordComparator, pair.getValue());
        }
        records.sort(comparatorChain);
	}

	public List<Record> getRecords() {
		return records;
	}

    /**
     * remove duplicates from records.
     */
    public void distinct() {
        Set<Record> distinctRecords = new LinkedHashSet<>();
        for (Record record : records) {
            distinctRecords.add(record);
        }
        records.clear();
        records.addAll(distinctRecords);
    }

    public RecordSet union(RecordSet recordSet, Boolean removeDuplicates) {
        Collection<Record> result;
        if (removeDuplicates) {
            result = new LinkedHashSet<>(records);
        } else {
            result = new ArrayList<>(records);
        }
        result.add(recordSet.curr());
        while(recordSet.hasNext()) {
            result.add(recordSet.next());
        }
        return new RecordSet(result);
    }

	@Override
	public RecordSet clone() {
		RecordSet newRecSet = new RecordSet();
		for (Record record : records) {
			newRecSet.add(record.clone());
		}
    	return newRecSet;
	}
}