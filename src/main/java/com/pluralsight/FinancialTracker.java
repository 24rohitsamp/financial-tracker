package com.pluralsight;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
     * • If the file doesn't exist, create an empty one so that future writes succeed.
     * • Each line looks like: date|time|description|vendor|amount
     */
    public static void loadTransactions(String fileName) {
        File file = new File(fileName);

        // Create the file if it does not exist
        try {
            if (!file.exists()) {
                file.createNewFile();
                return; // nothing to load from a brand-new file
            }

            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length != 5) continue;

                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                LocalTime time = LocalTime.parse(parts[1], TIME_FMT);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            fileScanner.close();
        } catch (Exception e) {
            System.out.println("Error loading transactions: " + e.getMessage());
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
        try {
            System.out.print("Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTimeStr = scanner.nextLine().trim();
            String[] dateTimeParts = dateTimeStr.split(" ");
            if (dateTimeParts.length != 2) {
                System.out.println("Invalid date/time format.");
                return;
            }
            LocalDate date = LocalDate.parse(dateTimeParts[0], DATE_FMT);
            LocalTime time = LocalTime.parse(dateTimeParts[1], TIME_FMT);

            System.out.print("Enter description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter vendor: ");
            String vendor = scanner.nextLine().trim();

            System.out.print("Enter amount (positive): ");
            Double amount = parseDouble(scanner.nextLine().trim());
            if (amount == null || amount <= 0) {
                System.out.println("Invalid amount. Must be a positive number.");
                return;
            }

            Transaction t = new Transaction(date, time, description, vendor, amount);
            transactions.add(t);
            saveTransaction(t);
            System.out.println("Deposit added successfully.");
        } catch (Exception e) {
            System.out.println("Error adding deposit: " + e.getMessage());
        }
    }

    /**
     * Same prompts as addDeposit.
     * Amount must be entered as a positive number,
     * then converted to a negative amount before storing.
     */
    private static void addPayment(Scanner scanner) {
        try {
            System.out.print("Enter date and time (yyyy-MM-dd HH:mm:ss): ");
            String dateTimeStr = scanner.nextLine().trim();
            String[] dateTimeParts = dateTimeStr.split(" ");
            if (dateTimeParts.length != 2) {
                System.out.println("Invalid date/time format.");
                return;
            }
            LocalDate date = LocalDate.parse(dateTimeParts[0], DATE_FMT);
            LocalTime time = LocalTime.parse(dateTimeParts[1], TIME_FMT);

            System.out.print("Enter description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Enter vendor: ");
            String vendor = scanner.nextLine().trim();

            System.out.print("Enter amount (positive): ");
            Double amount = parseDouble(scanner.nextLine().trim());
            if (amount == null || amount <= 0) {
                System.out.println("Invalid amount. Must be a positive number.");
                return;
            }

            // Store payments as negative
            double negativeAmount = -amount;
            Transaction t = new Transaction(date, time, description, vendor, negativeAmount);
            transactions.add(t);
            saveTransaction(t);
            System.out.println("Payment added successfully.");
        } catch (Exception e) {
            System.out.println("Error adding payment: " + e.getMessage());
        }
    }

    /** Appends a single transaction to the CSV file. */
    private static void saveTransaction(Transaction t) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(
                    t.getDate().format(DATE_FMT) + "|" +
                            t.getTime().format(TIME_FMT) + "|" +
                            t.getDescription() + "|" +
                            t.getVendor() + "|" +
                            t.getAmount()
            );
        } catch (Exception e) {
            System.out.println("Error saving transaction: " + e.getMessage());
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
    private static void printHeader() {
        System.out.printf("%-12s %-10s %-25s %-20s %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-".repeat(80));
    }

    private static void printTransaction(Transaction t) {
        System.out.printf("%-12s %-10s %-25s %-20s %10.2f%n",
                t.getDate().format(DATE_FMT),
                t.getTime().format(TIME_FMT),
                t.getDescription(),
                t.getVendor(),
                t.getAmount());
    }

    private static void displayLedger() {
        System.out.println("\n--- All Transactions ---");
        printHeader();
        for (int i = transactions.size() - 1; i >= 0; i--) {
            printTransaction(transactions.get(i));
        }
        System.out.println();
    }

    private static void displayDeposits() {
        System.out.println("\n--- Deposits ---");
        printHeader();
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            if (t.getAmount() > 0) {
                printTransaction(t);
            }
        }
        System.out.println();
    }

    private static void displayPayments() {
        System.out.println("\n--- Payments ---");
        printHeader();
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            if (t.getAmount() < 0) {
                printTransaction(t);
            }
        }
        System.out.println();
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
                    // Month-to-date: from the first of the current month to today
                    LocalDate now = LocalDate.now();
                    LocalDate start = now.withDayOfMonth(1);
                    System.out.println("\n--- Month To Date ---");
                    filterTransactionsByDate(start, now);
                }
                case "2" -> {
                    // Previous month: first to last day of last month
                    LocalDate now = LocalDate.now();
                    LocalDate start = now.minusMonths(1).withDayOfMonth(1);
                    LocalDate end = now.withDayOfMonth(1).minusDays(1);
                    System.out.println("\n--- Previous Month ---");
                    filterTransactionsByDate(start, end);
                }
                case "3" -> {
                    // Year-to-date: Jan 1 of current year to today
                    LocalDate now = LocalDate.now();
                    LocalDate start = now.withDayOfYear(1);
                    System.out.println("\n--- Year To Date ---");
                    filterTransactionsByDate(start, now);
                }
                case "4" -> {
                    // Previous year: Jan 1 to Dec 31 of last year
                    LocalDate now = LocalDate.now();
                    LocalDate start = now.minusYears(1).withDayOfYear(1);
                    LocalDate end = now.withDayOfYear(1).minusDays(1);
                    System.out.println("\n--- Previous Year ---");
                    filterTransactionsByDate(start, end);
                }
                case "5" -> {
                    System.out.print("Enter vendor name: ");
                    String vendor = scanner.nextLine().trim();
                    System.out.println("\n--- Transactions for Vendor: " + vendor + " ---");
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
        printHeader();
        boolean found = false;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            LocalDate date = t.getDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                printTransaction(t);
                found = true;
            }
        }
        if (!found) System.out.println("No transactions found in that date range.");
        System.out.println();
    }

    private static void filterTransactionsByVendor(String vendor) {
        printHeader();
        boolean found = false;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            if (t.getVendor().equalsIgnoreCase(vendor)) {
                printTransaction(t);
                found = true;
            }
        }
        if (!found) System.out.println("No transactions found for vendor: " + vendor);
        System.out.println();
    }

    private static void customSearch(Scanner scanner) {
        System.out.println("\n--- Custom Search (press Enter to skip any field) ---");

        System.out.print("Start date (yyyy-MM-dd): ");
        LocalDate startDate = parseDate(scanner.nextLine().trim());

        System.out.print("End date (yyyy-MM-dd): ");
        LocalDate endDate = parseDate(scanner.nextLine().trim());

        System.out.print("Description (partial match): ");
        String description = scanner.nextLine().trim();

        System.out.print("Vendor: ");
        String vendor = scanner.nextLine().trim();

        System.out.print("Amount (exact, e.g. -28.00): ");
        Double amount = parseDouble(scanner.nextLine().trim());

        printHeader();
        boolean found = false;
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);

            if (startDate != null && t.getDate().isBefore(startDate)) continue;
            if (endDate != null && t.getDate().isAfter(endDate)) continue;
            if (!description.isEmpty() &&
                    !t.getDescription().toLowerCase().contains(description.toLowerCase())) continue;
            if (!vendor.isEmpty() &&
                    !t.getVendor().equalsIgnoreCase(vendor)) continue;
            if (amount != null && t.getAmount() != amount) continue;

            printTransaction(t);
            found = true;
        }
        if (!found) System.out.println("No transactions matched your search.");
        System.out.println();
    }

    /* ------------------------------------------------------------------
       Utility parsers (you can reuse in many places)
       ------------------------------------------------------------------ */
    private static LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, DATE_FMT);
        } catch (Exception e) {
            System.out.println("Invalid date format: " + s);
            return null;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            System.out.println("Invalid number: " + s);
            return null;
        }
    }
}