package dbms.datatypes;

import java.sql.Date;

public class DBDate implements DatatypeDBMS {
    public static final String KEY = "Date";
    private Date value;

    static {
        DatatypeFactory.getFactory().register(KEY, DBDate.class);
    }

    public DBDate() {
    }

    public DBDate(Date value) {
        this.value = value;
    }

    @Override
    public Object toObj(String s) {
        try {
            return Date.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int compareTo(DatatypeDBMS data) {
        return value.compareTo((Date) data.getValue());
    }

    @Override
    public Date getValue() {
        return null;
    }
}