package dbms.datatypes;

public class DBFloat implements DBDatatype {

    /**
     * Key identifier to DBString.
     */
    public static final String KEY = "Float";

    static {
        DatatypeFactory.getFactory().register(KEY, DBFloat.class);
    }

    private Float value;

    public DBFloat() {

    }

    /**
     * @param value {@link Float} sets the local value to the given value.
     */
    public DBFloat(final Float value) {
        this.value = value;
    }

    @Override
    public Object toObj(final String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int compareTo(final DBDatatype data) {
        return value.compareTo((Float) data.getValue());
    }

    @Override
    public Float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBFloat dbFloat = (DBFloat) o;

        return value != null ? value.equals(dbFloat.value)
                : dbFloat.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
