
package com.library.dao;

import com.library.model.*;
import com.library.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LibraryDAO {

    public int count(String table) throws Exception {
        if (!Set.of("books", "members").contains(table)) {
            throw new IllegalArgumentException("Invalid table");
        }

        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {

            r.next();
            return r.getInt(1);
        }
    }

    public int borrowedCount() throws Exception {
        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(
                     "SELECT COUNT(*) FROM borrowings WHERE return_date IS NULL")) {

            r.next();
            return r.getInt(1);
        }
    }

    public int overdueCount() throws Exception {
        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(
                     "SELECT COUNT(*) FROM borrowings " +
                             "WHERE return_date IS NULL AND due_date < CURDATE()")) {

            r.next();
            return r.getInt(1);
        }
    }

    public List<Book> books() throws Exception {
        List<Book> out = new ArrayList<>();

        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM books ORDER BY title")) {

            while (r.next()) {
                out.add(new Book(
                        r.getInt("id"),
                        r.getString("isbn"),
                        r.getString("title"),
                        r.getString("author"),
                        r.getString("category"),
                        r.getInt("published_year"),
                        r.getInt("quantity"),
                        r.getInt("available_quantity")
                ));
            }
        }

        return out;
    }

    public List<Member> members(String search) throws Exception {
        List<Member> out = new ArrayList<>();

        String q = "SELECT * FROM members " +
                "WHERE member_code LIKE ? OR full_name LIKE ? OR email LIKE ? " +
                "ORDER BY full_name";

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {

            String x = "%" + (search == null ? "" : search) + "%";

            p.setString(1, x);
            p.setString(2, x);
            p.setString(3, x);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    out.add(new Member(
                            r.getInt("id"),
                            r.getString("member_code"),
                            r.getString("full_name"),
                            r.getString("email"),
                            r.getString("phone"),
                            r.getString("address")
                    ));
                }
            }
        }

        return out;
    }

    public void addBook(
            String isbn,
            String title,
            String author,
            String category,
            int year,
            int qty
    ) throws Exception {

        String q = "INSERT INTO books " +
                "(isbn, title, author, category, published_year, quantity, available_quantity) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {

            p.setString(1, isbn);
            p.setString(2, title);
            p.setString(3, author);
            p.setString(4, category);
            p.setInt(5, year);
            p.setInt(6, qty);
            p.setInt(7, qty);

            p.executeUpdate();
        }
    }

    public void addMember(
            String code,
            String name,
            String email,
            String phone,
            String address
    ) throws Exception {

        String q = "INSERT INTO members " +
                "(member_code, full_name, email, phone, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {

            p.setString(1, code);
            p.setString(2, name);
            p.setString(3, email);
            p.setString(4, phone);
            p.setString(5, address);

            p.executeUpdate();
        }
    }

    public void updateMember(
            int id,
            String code,
            String name,
            String email,
            String phone,
            String address
    ) throws Exception {

        String q = "UPDATE members SET member_code=?, full_name=?, " +
                "email=?, phone=?, address=? WHERE id=?";

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {

            p.setString(1, code);
            p.setString(2, name);
            p.setString(3, email);
            p.setString(4, phone);
            p.setString(5, address);
            p.setInt(6, id);

            p.executeUpdate();
        }
    }

    public void deleteMember(int id) throws Exception {

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT COUNT(*) FROM borrowings WHERE member_id=?")) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {
                r.next();

                if (r.getInt(1) > 0) {
                    throw new SQLException(
                            "Member has borrowing history and cannot be deleted."
                    );
                }
            }
        }

        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "DELETE FROM members WHERE id=?")) {

            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    public List<Borrowing> borrowings() throws Exception {
        List<Borrowing> out = new ArrayList<>();

        String q = "SELECT br.*, m.member_code, m.full_name, b.isbn, b.title " +
                "FROM borrowings br " +
                "JOIN members m ON br.member_id=m.id " +
                "JOIN books b ON br.book_id=b.id " +
                "ORDER BY br.issue_date DESC, br.id DESC";

        try (Connection c = DB.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(q)) {

            while (r.next()) {
                out.add(new Borrowing(
                        r.getInt("id"),
                        r.getInt("member_id"),
                        r.getInt("book_id"),
                        r.getString("member_code"),
                        r.getString("full_name"),
                        r.getString("isbn"),
                        r.getString("title"),
                        r.getDate("issue_date").toLocalDate(),
                        r.getDate("due_date").toLocalDate(),
                        r.getDate("return_date") == null
                                ? null
                                : r.getDate("return_date").toLocalDate(),
                        r.getString("status")
                ));
            }
        }

        return out;
    }

    public void issue(
            int memberId,
            int bookId,
            LocalDate issue,
            LocalDate due
    ) throws Exception {

        if (due.isBefore(issue)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before issue date."
            );
        }

        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);

            try {
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT available_quantity FROM books WHERE id=? FOR UPDATE")) {

                    p.setInt(1, bookId);

                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            throw new SQLException("Book not found.");
                        }

                        if (r.getInt(1) < 1) {
                            throw new SQLException(
                                    "No available copies of this book."
                            );
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO borrowings " +
                                "(member_id, book_id, issue_date, due_date, status) " +
                                "VALUES (?, ?, ?, ?, 'BORROWED')")) {

                    p.setInt(1, memberId);
                    p.setInt(2, bookId);
                    p.setDate(3, java.sql.Date.valueOf(issue));
                    p.setDate(4, java.sql.Date.valueOf(due));

                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE books SET available_quantity = " +
                                "available_quantity - 1 WHERE id=?")) {

                    p.setInt(1, bookId);
                    p.executeUpdate();
                }

                c.commit();

            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void returnBook(int borrowingId, LocalDate date) throws Exception {

        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);

            try {
                int bookId;

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT book_id, return_date FROM borrowings " +
                                "WHERE id=? FOR UPDATE")) {

                    p.setInt(1, borrowingId);

                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) {
                            throw new SQLException(
                                    "Borrowing record not found."
                            );
                        }

                        if (r.getDate("return_date") != null) {
                            throw new SQLException(
                                    "This book has already been returned."
                            );
                        }

                        bookId = r.getInt("book_id");
                    }
                }

                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE borrowings SET return_date=?, status='RETURNED' " +
                                "WHERE id=?")) {

                    p.setDate(1, java.sql.Date.valueOf(date));
                    p.setInt(2, borrowingId);

                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE books SET available_quantity = " +
                                "available_quantity + 1 WHERE id=?")) {

                    p.setInt(1, bookId);
                    p.executeUpdate();
                }

                c.commit();

            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }
}
