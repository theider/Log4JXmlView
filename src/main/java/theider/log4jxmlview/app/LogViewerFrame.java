package theider.log4jxmlview.app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import theider.log4jxmlview.logrecord.LogFileRecordIndex;

public class LogViewerFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(LogViewerFrame.class);

    private final JDesktopPane desktopPane = new JDesktopPane();    
    
    public LogViewerFrame() {
        setTitle("Log4j XML Viewer");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        setContentPane(desktopPane);

        createMenuBar();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open XML File...");
        openItem.addActionListener(e -> openFile());

        fileMenu.add(openItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();            
            try {                
                showLogWindow(file);
            } catch (IOException ex) {
                logger.error("Failed to load XML file: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to load XML file:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void showLogWindow(File file) throws IOException {
        LogFileRecordIndex logFileRecordIndex = new LogFileRecordIndex(file);
        LogViewerTableModel tableModel = new LogViewerTableModel(logFileRecordIndex);
        String title = logFileRecordIndex.getFilename();        

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        LogViewListFrame internalFrame = new LogViewListFrame(logFileRecordIndex);
        internalFrame.setSize(900, 600);
        internalFrame.setVisible(true);
        internalFrame.setLayout(new BorderLayout());
        internalFrame.add(scrollPane, BorderLayout.CENTER);

        desktopPane.add(internalFrame);
        try {
            internalFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            logger.error(title + " internal frame could not be selected", e);
        }
    }
}
