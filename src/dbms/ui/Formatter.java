package dbms.ui;

import dbms.util.Result;
import dbms.util.ResultSet;

import java.util.*;


public class Formatter {
    private static Formatter instance;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static Formatter getInstance() {
        if (instance == null) {
            instance = new Formatter();
        }
        return instance;
    }

    private int getColumnWidth(Object key, ResultSet resultSet) {
        int max = key.toString().length();
        for(Result result : resultSet) {
            Object value = result.getResult().get(key);
            int currentLength = value == null ? 0 : value.toString().length();
            if (currentLength > max) {
                max = currentLength;
            }
        }
        return max;
    }

    private void printTableLine(Result firstResult, List<Integer> widthOfColumns) {
        Iterator it = firstResult.getResult().entrySet().iterator();
        int currentColumn = 0;
        while (it.hasNext()) {
            it.next();
            System.out.print("+");
            for (int i = 0; i < widthOfColumns.get(currentColumn); i++) {
                System.out.print("-");
            }
            currentColumn++;
        }
        System.out.println("+");
    }

    private List<Integer> getAllColumnsWidth(ResultSet resultSet, Result firstResult) {
        Iterator it = firstResult.getResult().entrySet().iterator();
        List<Integer> widthOfColumns = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            widthOfColumns.add(getColumnWidth(pair.getKey(), resultSet));
        }
        return widthOfColumns;
    }

    private void printFirstRow(Result firstResult, List<Integer> widthOfColumns) {
        Iterator it = firstResult.getResult().entrySet().iterator();
        int currentColumn = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Object value = pair.getValue();
            System.out.print("|");
            if (value == null) {
                System.out.print("");
                for (int i = 0; i < widthOfColumns.get(currentColumn); i++)
                    System.out.print(" ");
            } else {
                System.out.print(value);
                for (int i = 0; i < widthOfColumns.get(currentColumn) - value.toString().length(); i++)
                    System.out.print(" ");
            }
            currentColumn++;
        }
        System.out.println("|");
    }

    private void printAllRows(ResultSet resultSet, Result firstResult, List<Integer> widthOfColumns) {
        int currentColumn = 0;
        while(resultSet.hasNext()) {
            Iterator it4 = resultSet.next().getResult().entrySet().iterator();
            while (it4.hasNext()) {
                Map.Entry pair = (Map.Entry)it4.next();
                System.out.print("|");
                Object value = pair.getValue();
                if (value == null) {
                    System.out.print("");
                    for (int i = 0; i < widthOfColumns.get(currentColumn); i++)
                        System.out.print(" ");
                } else {
                    System.out.print(value);
                    for (int i = 0; i < widthOfColumns.get(currentColumn) - value.toString().length(); i++)
                        System.out.print(" ");
                }
                currentColumn++;
            }
            System.out.println("|");
            currentColumn = 0;
            printTableLine(firstResult, widthOfColumns);
        }
    }

    private void printHeaders(Result firstResult, List<Integer> widthOfColumns) {
        int currentColumn = 0;
        Iterator it = firstResult.getResult().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.print("|");
            System.out.print(ANSI_GREEN + pair.getKey() + ANSI_RESET);
            for (int i = 0; i < widthOfColumns.get(currentColumn) - pair.getKey().toString().length(); i++) {
                System.out.print(" ");
            }
            currentColumn++;
        }
        System.out.println("|");
    }

    public void printTable(ResultSet resultSet) {
        Result firstResult = resultSet.next();
        if (resultSet.isEmpty()) {
        	return;
        }
        List<Integer> widthOfColumns = getAllColumnsWidth(resultSet, firstResult);

        printTableLine(firstResult, widthOfColumns);

        printHeaders(firstResult, widthOfColumns);
        printTableLine(firstResult, widthOfColumns);

        printFirstRow(firstResult, widthOfColumns);
        printTableLine(firstResult, widthOfColumns);

        printAllRows(resultSet, firstResult, widthOfColumns);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ID", 1);
        map.put("Name", "Khaled");
        map.put("Part", "SQL Parser");

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("ID", 5);
        map2.put("Name", "Tolba");
        map2.put("Part", "SQL Parser");

        Map<String, Object> map3 = new LinkedHashMap<>();
        map3.put("ID", 4);
        map3.put("Name", "Anas");
        map3.put("Part", "XML Parser");

        Map<String, Object> map4 = new LinkedHashMap<>();
        map4.put("ID", 9);
        map4.put("Name", null);
        map4.put("Part", "XML Parser                          ");

        Result result = new Result(map);
        Result result2 = new Result(map2);
        Result result3 = new Result(map3);
        Result result4 = new Result(map4);

        ArrayList<Result> results = new ArrayList<>();
        results.add(result);
        results.add(result2);
        results.add(result3);
        results.add(result4);

        ResultSet resultSet = new ResultSet(results);
        resultSet.orderBy(true, "ID");
        new Formatter().printTable(resultSet);

    }
}
