package com.pluralsight;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Capstone skeleton – personal finance tracker.
 * ------------------------------------------------
 * File format  (pipe-delimited)
 *     yyyy-MM-dd|HH:mm:ss|description|vendor|amount
 * A deposit has a positive amount; a payment is stored
 * as a negative amount.
 */
public class FinancialTracker {

    /* ------------------------------------------------------------------
       Shared data and formatters
       ------------------------------------------------------------------ */
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    private static final String DATETIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    /* ------------------------------------------------------------------
       Main menu
       ------------------------------------------------------------------ */
    public static void main(String[] args) {
        loadTransactions(FILE_NAME);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("Welcome to TransactionApp");
            System.out.println("Choose an option:");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "D" -> addDeposit(scanner);
                case "P" -> addPayment(scanner);
                case "L" -> ledgerMenu(scanner);
                case "X" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
        scanner.close();
    }

    /* ------------------------------------------------------------------
       File I/O
       ------------------------------------------------------------------ */

    /**
     * Load transactions from FILE_NAME.
     * • If the file doesn’t exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {
            try {
                File file = new File(fileName);

                if (!file.exists()) {
                    file.createNewFile();
                }

                Scanner fileScanner = new Scanner(file);

                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();

                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split("\\|");

                    if (parts.length == 5) {
                        LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                        LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                        String description = parts[2];
                        String vendor = parts[3];
                        double amount = Double.parseDouble(parts[4]);

                        Transaction transaction = new Transaction(date, time, description, vendor, amount);
                        transactions.add(transaction);
                    }
                }

                fileScanner.close();

            } catch (Exception e) {
                System.out.println("Error loading transactions: " + e.getMessage());
            }
        }

    }

    /* ------------------------------------------------------------------
       Add new transactions
       ------------------------------------------------------------------ */

    /**
     * Prompt for ONE date+time string in the format
     * "yyyy-MM-dd HH:mm:ss", plus description, vendor, amount.
     * Validate that the amount entered is positive.
     * Store the amount as-is (positive) and append to the file.
     */
    private static void addDeposit(Scanner scanner) {
        addTransaction(scanner, false);

    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
        private static void addTransaction(Scanner scanner, boolean isDeposit) {
            try {
                System.out.print("Enter date and time yyyy-MM-dd HH:mm:ss: ");
                String dateTimeInput = scanner.nextLine();

                System.out.print("Enter description: ");
                String description = scanner.nextLine();

                System.out.print("Enter vendor: ");
                String vendor = scanner.nextLine();

                System.out.print("Enter amount: ");
                double amount = Double.parseDouble(scanner.nextLine());

                if (amount <= 0) {
                    System.out.println("Amount must be positive.");
                    return;
                }

                if (!isDeposit) {
                    amount *= -1;
                }

                String[] dateTimeParts = dateTimeInput.split(" ");
                LocalDate date = LocalDate.parse(dateTimeParts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(dateTimeParts[1], TIME_FMT);

                Transaction transaction = new Transaction(date, time, description, vendor, amount);
                transactions.add(transaction);

                FileWriter writer = new FileWriter(FILE_NAME, true);
                writer.write(transaction.toFileString() + "\n");
                writer.close();

                System.out.println("Transaction added.");

            } catch (Exception e) {
                System.out.println("Invalid input. Transaction not added.");
            }
        }

        /* ------------------------------------------------------------------
       Ledger menu
       ------------------------------------------------------------------ */
    private static void ledgerMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Ledger");
            System.out.println("Choose an option:");
            System.out.println("A) All");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");

            String input = scanner.nextLine().trim();

            switch (input.toUpperCase()) {
                case "A" -> displayLedger();
                case "D" -> displayDeposits();
                case "P" -> displayPayments();
                case "R" -> reportsMenu(scanner);
                case "H" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Display helpers: show data in neat columns
       ------------------------------------------------------------------ */
    private static void displayLedger() {
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }

        private static void displayDeposits() {
            for (Transaction t : transactions) {
                if (t.getAmount() > 0) {
                    System.out.println(t);
                }
            }
        }

        private static void displayPayments() {
            for (Transaction t : transactions) {
                if (t.getAmount() < 0) {
                    System.out.println(t);
                }
            }
        }

    /* ------------------------------------------------------------------
       Reports menu
       ------------------------------------------------------------------ */
    private static void reportsMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("Reports");
            System.out.println("Choose an option:");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("6) Custom Search");
            System.out.println("0) Back");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = today.withDayOfMonth(1);
                            filterTransactionsByDate(start, today);
                }
                case "2" -> {
                    LocalDate today = LocalDate.now();
                            LocalDate start = today.minusMonths(1).withDayOfMonth(1);
                    LocalDate end = today.withDayOfMonth(1).minusDays(1);
                    filterTransactionsByDate(start, today);
                }
                case "3" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = today.withDayOfYear(1);
                    filterTransactionsByDate();
                }
                case "4" -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = LocalDate.of(today.getYear(), -1,1,1);
                    LocalDate end = LocalDate.of(today.getYear(), -1,12,31);
                    filterTransactionsByDate(start,end);
                }
                case "5" -> {
                    System.out.print("Enter Vendor Name: ");
                    String vendor = scanner.nextLine();
                    filterTransactionsByVendor(vendor);
                }
                case "6" -> customSearch(scanner);
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------------------------------------------
       Reporting helpers
       ------------------------------------------------------------------ */
    private static void filterTransactionsByDate(LocalDate start, LocalDate end) {
        // TODO – iterate transactions, print those within the range
    }

    private static void filterTransactionsByVendor(String vendor) {
        // TODO – iterate transactions, print those with matching vendor
    }

    private static void customSearch(Scanner scanner) {
        // TODO – prompt for any combination of date range, description,
        //        vendor, and exact amount, then display matches
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        /* TODO – return LocalDate or null */
        return null;
    }

    private static Double parseDouble(String s) {
        /* TODO – return Double   or null */
        return null;
    }
}
