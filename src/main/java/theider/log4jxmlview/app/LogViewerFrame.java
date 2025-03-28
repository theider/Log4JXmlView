package theider.log4jxmlview.app;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import theider.log4jxmlview.logrecord.LogRecordReader;

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
            showLogWindow(file);
        }
    }

    private void showLogWindow(File file) {
        // Setup progress dialog
        JDialog progressDialog = new JDialog(this, "Indexing Log File", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 24));

        progressDialog.setLayout(new BorderLayout());
        progressDialog.add(new JLabel("Building index..."), BorderLayout.NORTH);
        progressDialog.add(progressBar, BorderLayout.CENTER);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(this);

        // Use a SwingWorker so the UI doesn't block
        SwingWorker<LogRecordReader, Integer> worker = new SwingWorker<>() {
            @Override
            protected LogRecordReader doInBackground() throws Exception {
                long fileSize = file.length();
                logger.debug("start record index in backgroup len=" + fileSize);
                return new LogRecordReader(file, (bytesRead, totalBytes) -> {
                    int percent = (int) ((bytesRead * 100) / totalBytes);
                    logger.debug("read update {} {} {}", percent, bytesRead, totalBytes);
                    publish(percent);
                }, fileSize);
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                progressBar.setValue(latest);
            }

            @Override
            protected void done() {
                progressDialog.dispose(); // close progress dialog
                try {
                    LogRecordReader reader = get();
                    LogViewerTableModel tableModel = new LogViewerTableModel(reader);
                    JTable table = new JTable(tableModel);
                    JScrollPane scrollPane = new JScrollPane(table);

                    LogViewListFrame internalFrame = new LogViewListFrame(reader);
                    internalFrame.setSize(900, 600);
                    internalFrame.setVisible(true);
                    internalFrame.setLayout(new BorderLayout());
                    internalFrame.add(scrollPane, BorderLayout.CENTER);

                    desktopPane.add(internalFrame);
                    internalFrame.setSelected(true);
                } catch (Exception ex) {
                    logger.error("Failed to load log file: {}", ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(
                            LogViewerFrame.this,
                            "Failed to load XML file:\n" + ex.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

}
