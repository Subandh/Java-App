import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

class Book {
    private static int idCounter = 1;
    int id;
    String name, author, genre, dateTimeAdded;

    public Book(int id, String name, String author, String genre, String dateTimeAdded) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.dateTimeAdded = dateTimeAdded;
        idCounter = Math.max(idCounter, id + 1);
    }

    public Book(String name, String author, String genre, String dateTimeAdded) {
        this(idCounter++, name, author, genre, dateTimeAdded);
    }

    public static Book fromString(String line) {
        String[] p = line.split("\\|");
        return p.length == 5 ? new Book(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]) : null;
    }

    public String toDataString() {
        return id + "|" + name + "|" + author + "|" + genre + "|" + dateTimeAdded;
    }

    public String toString() {
        return "ID: " + id + " | " + name + " by " + author + " (Genre: " + genre + ") - Added on: " + dateTimeAdded;
    }
}

class Library {
    private ArrayList<Book> books = new ArrayList<>();
    private final String FILE_NAME = "library_books.txt";

    public Library() { load(); }

    public void addBook(Book b) { books.add(b); save(); }

    public boolean removeBook(Book b) {
        boolean removed = books.remove(b);
        if (removed) save();
        return removed;
    }

    public ArrayList<Book> getBooks() { return books; }

    public ArrayList<Book> search(String query) {
        ArrayList<Book> res = new ArrayList<>();
        for (Book b : books)
            if (b.name.toLowerCase().contains(query.toLowerCase()) ||
                b.genre.toLowerCase().contains(query.toLowerCase()))
                res.add(b);
        return res;
    }

    public void sortByAuthor() { books.sort(Comparator.comparing(b -> b.author)); }

    public void sortByDate() { books.sort(Comparator.comparing(b -> b.dateTimeAdded)); }

    public void sortById() { books.sort(Comparator.comparingInt(b -> b.id)); }

    private void load() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                Book b = Book.fromString(line);
                if (b != null) books.add(b);
            }
        } catch (IOException ignored) {}
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Book b : books)
                bw.write(b.toDataString() + "\n");
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }
}

public class LibraryBookManager {
    private JFrame frame;
    private JTextField searchField, bookField, authorField, idField;
    private JComboBox<String> genreBox;
    private JLabel genreLabel;
    private Set<String> selectedGenres = new LinkedHashSet<>();
    private DefaultListModel<Book> listModel = new DefaultListModel<>();
    private JList<Book> bookList = new JList<>(listModel);
    private Library library = new Library();

    public LibraryBookManager() {
        frame = new JFrame("Library Book Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        Color beige = new Color(210, 210, 190), grey = new Color(175, 175, 175);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(beige);
        frame.setContentPane(mainPanel);

        bookField = new JTextField(15);
        authorField = new JTextField(15);
        idField = new JTextField(5);
        bookField.setFont(font); authorField.setFont(font); idField.setFont(font);
        genreBox = new JComboBox<>(new String[]{"Fiction", "Non-Fiction", "Sci-Fi", "Fantasy", "Mystery", "Biography", "History", "Finance",
                "Autobiography", "Self Help", "Psychology", "Horror", "Romance", "Gothic", "Adventure", "Coming-of-Age", "Other"});
        genreBox.setFont(font);
        genreLabel = new JLabel("Selected Genres: None");

        JScrollPane scrollPane = new JScrollPane(bookList);
        bookList.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JPanel inputPanel = new JPanel(new GridLayout(2, 1)); inputPanel.setBackground(grey);
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT)); row1.setBackground(grey);
        row1.add(new JLabel("Book Name:")); row1.add(bookField);
        row1.add(new JLabel("Author:")); row1.add(authorField);
        row1.add(new JLabel("ID (Optional):")); row1.add(idField);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT)); row2.setBackground(grey);
        JButton addGenreBtn = new JButton("+"); addGenreBtn.setFont(font);
        JButton addBtn = new JButton("Add Book"), removeBtn = new JButton("Remove Selected Book");
        addBtn.setFont(font); removeBtn.setFont(font);
        addBtn.setBackground(new Color(144, 238, 144)); removeBtn.setBackground(new Color(255, 99, 71));
        row2.add(new JLabel("Genre:")); row2.add(genreBox); row2.add(addGenreBtn); row2.add(genreLabel);
        row2.add(addBtn); row2.add(removeBtn);

        inputPanel.add(row1); inputPanel.add(row2);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); controlPanel.setBackground(beige);
        searchField = new JTextField(15); searchField.setFont(font);
        String[] btnNames = {"Sort by Author", "Sort by Date", "Sort by ID", "Search", "Clear"};
        Color[] colors = {new Color(173, 216, 230), new Color(173, 216, 230), new Color(173, 216, 230), new Color(135, 206, 250), new Color(255, 182, 193)};
        JButton[] buttons = new JButton[btnNames.length];

        Runnable updateList = () -> {
            listModel.clear();
            String q = searchField.getText().trim();
            (q.isEmpty() ? library.getBooks() : library.search(q)).forEach(listModel::addElement);
        };

        for (int i = 0; i < btnNames.length; i++) {
            buttons[i] = new JButton(btnNames[i]);
            buttons[i].setFont(font); buttons[i].setBackground(colors[i]);
            controlPanel.add(buttons[i]);
            if (btnNames[i].equals("Sort by ID")) {
                controlPanel.add(new JLabel("Search:"));
                controlPanel.add(searchField);
            }
        }

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { updateList.run(); }
        });

        Runnable[] actions = {
            () -> { library.sortByAuthor(); updateList.run(); },
            () -> { library.sortByDate(); updateList.run(); },
            () -> { library.sortById(); updateList.run(); },
            updateList,
            () -> { searchField.setText(""); updateList.run(); }
        };

        for (int i = 0; i < buttons.length; i++) {
            int idx = i;
            buttons[i].addActionListener(e -> actions[idx].run());
        }

        addGenreBtn.addActionListener(e -> {
            String sel = (String) genreBox.getSelectedItem();
            if (sel != null && selectedGenres.add(sel))
                genreLabel.setText("Selected Genres: " + String.join(", ", selectedGenres));
        });

        addBtn.addActionListener(e -> {
            String idTxt = idField.getText().trim(), name = bookField.getText().trim(), author = authorField.getText().trim();
            String genre = String.join(", ", selectedGenres);
            if (name.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter book name, author, and at least one genre.");
                return;
            }

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                Book book = idTxt.isEmpty() ? new Book(name, author, genre, now) :
                        new Book(Integer.parseInt(idTxt), name, author, genre, now);
                library.addBook(book);
                updateList.run();
                JOptionPane.showMessageDialog(frame, "Book added: " + book.name);
                bookField.setText(""); authorField.setText(""); idField.setText("");
                genreBox.setSelectedIndex(0); selectedGenres.clear(); genreLabel.setText("Selected Genres: None");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid ID. Enter numeric.");
            }
        });

        removeBtn.addActionListener(e -> {
            Book sel = bookList.getSelectedValue();
            if (sel != null && library.removeBook(sel)) {
                updateList.run();
                JOptionPane.showMessageDialog(frame, "Book removed: " + sel.name);
            } else JOptionPane.showMessageDialog(frame, "Please select a book to remove.");
        });

        updateList.run();
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryBookManager::new);
    }
}
