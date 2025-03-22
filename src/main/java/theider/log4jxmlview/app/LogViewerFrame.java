package theider.log4jxmlview.app;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import theider.log4jxmlview.Log4jXmlReader;
import theider.log4jxmlview.LogRecord;
import theider.log4jxmlview.LogViewerTableModel;

public class LogViewerFrame extends JFrame {

    private static final Logger log = LoggerFactory.getLogger(LogViewerFrame.class);

    private final JTable table;
    private final LogViewerTableModel tableModel;
    private final Log4jXmlReader xmlReader;

    public LogViewerFrame(Log4jXmlReader reader) {
        this.xmlReader = reader;
        setTitle("Log4j XML Viewer");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new LogViewerTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem open = new JMenuItem("Open XML File...");
        open.addActionListener(e -> openFile());
        fileMenu.add(open);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                List<LogRecord> records = xmlReader.parseRecords(fis);
                tableModel.setData(records);
            } catch (Exception ex) {
                ex.printStackTrace(); // For debugging, optional
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load XML file:\n" + ex.getMessage()
                        + "\n\nMake sure the XML file is well-formed and wrapped in a <records> root tag.",
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
