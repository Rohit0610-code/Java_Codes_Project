package java_filehandling_project;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

// ─────────────────────────────────────────────
//  Simple Student data model (OOP)
// ─────────────────────────────────────────────
class Student {
    private int id;
    private String name;
    private String branch;
    private double cgpa;

    public Student(int id, String name, String branch, double cgpa) {
        this.id     = id;
        this.name   = name;
        this.branch = branch;
        this.cgpa   = cgpa;
    }

    public int    getId()     { return id;     }
    public String getName()   { return name;   }
    public String getBranch() { return branch; }
    public double getCgpa()   { return cgpa;   }

    @Override
    public String toString() {
        return id + "," + name + "," + branch + "," + cgpa;
    }
}

// ─────────────────────────────────────────────
//  Main Application class
// ─────────────────────────────────────────────
public class StudentDataViewer extends JFrame {

    // ── Colour / Font constants ──────────────
    private static final Color  HEADER_BG    = new Color(25, 84, 153);
    private static final Color  HEADER_FG    = Color.WHITE;
    private static final Color  BTN_COLOR    = new Color(41, 128, 185);
    private static final Color  BTN_HOVER    = new Color(21, 100, 160);
    private static final Color  TABLE_HDR    = new Color(52, 73, 94);
    private static final Color  ROW_EVEN     = new Color(240, 248, 255);
    private static final Color  ROW_ODD      = Color.WHITE;
    private static final Color  STATUS_BG    = new Color(44, 62, 80);
    private static final Font   TITLE_FONT   = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font   SUBTITLE_FONT= new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font   BTN_FONT     = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font   TABLE_FONT   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font   STATUS_FONT  = new Font("Segoe UI", Font.PLAIN, 12);

    // ── Swing components ────────────────────
    private JLabel    fileNameLabel;
    private JButton   browseBtn, loadBtn, searchBtn, exportBtn, clearBtn;
    private JTextField searchField;
    private JTable    table;
    private DefaultTableModel tableModel;
    private JLabel    statusLabel, recordCountLabel;

    // ── Data storage ────────────────────────
    private ArrayList<Student> studentList = new ArrayList<>();
    private File selectedFile = null;

    // ────────────────────────────────────────
    //  Constructor – build the whole GUI
    // ────────────────────────────────────────
    public StudentDataViewer() {
        setTitle("Structured File Data Viewer - Student Records");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        add(buildHeaderPanel(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);

        setVisible(true);
    }

    // ─────────────────────────────────────────
    //  HEADER  (title + toolbar)
    // ─────────────────────────────────────────
    private JPanel buildHeaderPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // ── Title strip ──
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(HEADER_BG);
        titlePanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("  Structured File Data Viewer");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(HEADER_FG);
        titleLabel.setIcon(createTextIcon("📋", 20)); // decorative emoji icon

        JLabel subLabel = new JLabel("Load · Search · Export Student Records");
        subLabel.setFont(SUBTITLE_FONT);
        subLabel.setForeground(new Color(189, 215, 238));

        JPanel titleText = new JPanel(new GridLayout(2, 1, 0, 2));
        titleText.setOpaque(false);
        titleText.add(titleLabel);
        titleText.add(subLabel);
        titlePanel.add(titleText, BorderLayout.WEST);

        // ── Toolbar strip ──
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(new Color(36, 100, 170));
        toolbar.setBorder(new EmptyBorder(4, 10, 4, 10));

        browseBtn  = makeButton("📂 Browse",  "Browse for a .txt file");
        loadBtn    = makeButton("📥 Load Data","Load records from selected file");
        exportBtn  = makeButton("💾 Export",  "Export records to report.txt");
        clearBtn   = makeButton("🗑 Clear",   "Clear table and reset");

        searchField = new JTextField(16);
        searchField.setFont(BTN_FONT);
        searchField.setToolTipText("Enter student name to search");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 190, 230), 1),
                new EmptyBorder(4, 8, 4, 8)));
        searchField.setPreferredSize(new Dimension(160, 32));

        searchBtn = makeButton("🔍 Search", "Search by student name");

        fileNameLabel = new JLabel("  No file selected");
        fileNameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        fileNameLabel.setForeground(new Color(200, 230, 255));

        toolbar.add(browseBtn);
        toolbar.add(loadBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(new JLabel("  Search:") {{
            setForeground(Color.WHITE); setFont(BTN_FONT); }});
        toolbar.add(searchField);
        toolbar.add(searchBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(exportBtn);
        toolbar.add(clearBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(fileNameLabel);

        // ── Wire button actions ──
        browseBtn.addActionListener(e -> browseFile());
        loadBtn  .addActionListener(e -> loadData());
        searchBtn.addActionListener(e -> searchData());
        exportBtn.addActionListener(e -> exportData());
        clearBtn .addActionListener(e -> clearAll());

        // allow pressing Enter in search field
        searchField.addActionListener(e -> searchData());

        wrapper.add(titlePanel, BorderLayout.NORTH);
        wrapper.add(toolbar,    BorderLayout.SOUTH);
        return wrapper;
    }

    // ─────────────────────────────────────────
    //  CENTER  (JTable inside JScrollPane)
    // ─────────────────────────────────────────
    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.setBackground(Color.WHITE);

        // Table model with non-editable cells
        String[] columns = {"ID", "Name", "Branch", "CGPA"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(TABLE_FONT);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 230, 240));
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoCreateRowSorter(true); // enable column sorting

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);

        // Center-align ID and CGPA columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Styled header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HDR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        // Alternating row colours via custom renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    setForeground(new Color(30, 30, 30));
                }
                if (col == 0 || col == 3) setHorizontalAlignment(CENTER);
                else                      setHorizontalAlignment(LEFT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ─────────────────────────────────────────
    //  STATUS BAR  (bottom)
    // ─────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BG);
        bar.setBorder(new EmptyBorder(5, 15, 5, 15));
        bar.setPreferredSize(new Dimension(0, 30));

        statusLabel = new JLabel("Ready – Please browse and load a student data file.");
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(new Color(189, 215, 238));

        recordCountLabel = new JLabel("Total Records: 0");
        recordCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recordCountLabel.setForeground(new Color(144, 238, 144));

        bar.add(statusLabel,      BorderLayout.WEST);
        bar.add(recordCountLabel, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────
    //  ACTION: Browse for file
    // ─────────────────────────────────────────
    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Student Data File (.txt)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text Files (*.txt)", "txt"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileNameLabel.setText("  " + selectedFile.getName());
            setStatus("✔ File Selected: " + selectedFile.getName());
        }
    }

    // ─────────────────────────────────────────
    //  ACTION: Load data from file using FileInputStream
    // ─────────────────────────────────────────
    private void loadData() {
        if (selectedFile == null) {
            showError("Please browse and select a file first.");
            return;
        }

        studentList.clear();
        tableModel.setRowCount(0); // clear existing table rows

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(selectedFile);

            // Read all bytes then convert to String
            byte[] data = new byte[(int) selectedFile.length()];
            fis.read(data);
            String content = new String(data).trim();

            if (content.isEmpty()) {
                setStatus("⚠ File is empty. Please choose a valid data file.");
                return;
            }

            // Split by newline (handle Windows \r\n and Unix \n)
            String[] lines = content.split("\\r?\\n");
            int loaded = 0, skipped = 0;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    skipped++;
                    continue; // skip malformed lines
                }

                try {
                    int    id     = Integer.parseInt(parts[0].trim());
                    String name   = parts[1].trim();
                    String branch = parts[2].trim();
                    double cgpa   = Double.parseDouble(parts[3].trim());

                    Student s = new Student(id, name, branch, cgpa);
                    studentList.add(s);
                    tableModel.addRow(new Object[]{id, name, branch, cgpa});
                    loaded++;

                } catch (NumberFormatException nfe) {
                    skipped++; // skip lines with bad number format
                }
            }

            updateRecordCount(loaded);

            if (skipped > 0) {
                setStatus("✔ Data Loaded Successfully – " + loaded +
                          " records loaded, " + skipped + " line(s) skipped.");
            } else {
                setStatus("✔ Data Loaded Successfully – " + loaded + " records loaded.");
            }

        } catch (FileNotFoundException fnfe) {
            showError("File not found: " + selectedFile.getName());
        } catch (IOException ioe) {
            showError("Error reading file: " + ioe.getMessage());
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────
    //  ACTION: Search by name
    // ─────────────────────────────────────────
    private void searchData() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            // If search is empty, restore all records
            tableModel.setRowCount(0);
            for (Student s : studentList) {
                tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getBranch(), s.getCgpa()});
            }
            updateRecordCount(studentList.size());
            setStatus("ℹ Search cleared – showing all records.");
            return;
        }

        tableModel.setRowCount(0);
        int found = 0;

        for (Student s : studentList) {
            if (s.getName().toLowerCase().contains(query)) {
                tableModel.addRow(new Object[]{
                    s.getId(), s.getName(), s.getBranch(), s.getCgpa()});
                found++;
            }
        }

        updateRecordCount(found);
        if (found == 0) {
            setStatus("⚠ No records found matching: \"" + searchField.getText().trim() + "\"");
        } else {
            setStatus("✔ Search Completed – " + found + " record(s) found for \"" +
                      searchField.getText().trim() + "\"");
        }
    }

    // ─────────────────────────────────────────
    //  ACTION: Export displayed table to report.txt using FileOutputStream
    // ─────────────────────────────────────────
    private void exportData() {
        if (tableModel.getRowCount() == 0) {
            showError("No data to export. Please load data first.");
            return;
        }

        File reportFile = new File("report.txt");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(reportFile);

            // ── Build a nicely formatted report ──
            StringBuilder sb = new StringBuilder();
            sb.append("========================================\n");
            sb.append("     STUDENT DATA REPORT\n");
            sb.append("     Generated by Structured File Data Viewer\n");
            sb.append("========================================\n\n");

            // Header row
            sb.append(String.format("%-8s %-20s %-12s %-8s%n",
                    "ID", "Name", "Branch", "CGPA"));
            sb.append("----------------------------------------\n");

            // Data rows from current table view
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                sb.append(String.format("%-8s %-20s %-12s %-8s%n",
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2),
                        tableModel.getValueAt(i, 3)));
            }

            sb.append("----------------------------------------\n");
            sb.append("Total Records Exported: ").append(tableModel.getRowCount()).append("\n");
            sb.append("========================================\n");

            byte[] bytes = sb.toString().getBytes();
            fos.write(bytes);
            fos.flush();

            setStatus("✔ Data Exported Successfully → " + reportFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Report saved to:\n" + reportFile.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ioe) {
            showError("Export failed: " + ioe.getMessage());
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────
    //  ACTION: Clear everything
    // ─────────────────────────────────────────
    private void clearAll() {
        tableModel.setRowCount(0);
        studentList.clear();
        searchField.setText("");
        fileNameLabel.setText("  No file selected");
        selectedFile = null;
        updateRecordCount(0);
        setStatus("🗑 Cleared – ready for a new file.");
    }

    // ─────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────

    /** Update status bar message */
    private void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    /** Update total record count label */
    private void updateRecordCount(int count) {
        recordCountLabel.setText("Total Records: " + count + "  ");
    }

    /** Show an error dialog and update status */
    private void showError(String msg) {
        setStatus("✖ " + msg);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Factory: create a styled JButton with hover effect.
     */
    private JButton makeButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(BTN_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(BTN_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(120, 32));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BTN_HOVER); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(BTN_COLOR);  }
        });

        return btn;
    }

    /**
     * Creates a tiny ImageIcon from a Unicode character / emoji for decorative use.
     * Falls back gracefully if the font can't render it.
     */
    private Icon createTextIcon(String text, int size) {
        return new Icon() {
            @Override public int getIconWidth()  { return size + 4; }
            @Override public int getIconHeight() { return size + 4; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
                g.setColor(HEADER_FG);
                g.drawString(text, x, y + size);
            }
        };
    }

    // ─────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────
    public static void main(String[] args) {
        // Use the system look-and-feel for crisp widgets, then override with our colours
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(StudentDataViewer::new);
    }
}
