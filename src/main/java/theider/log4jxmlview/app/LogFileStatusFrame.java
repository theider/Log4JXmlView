package theider.log4jxmlview.app;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import theider.log4jxmlview.logrecord.indexer.LogRecordIndex;
import theider.log4jxmlview.logrecord.model.LogRecord;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogFileStatusFrame extends JInternalFrame {

    private final JPanel infoPanel = new JPanel(new BorderLayout());
    private final JPanel centerPanel = new JPanel(new BorderLayout());
    private final JLabel loadingLabel = new JLabel("Scanning log file...", SwingConstants.CENTER);

    private static final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

    private static final DateTimeFormatter timestampFormatter =
            DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm:ss z")
                             .withZone(ZoneId.of("UTC"));

    public LogFileStatusFrame(String logFileName, LogRecordIndex logIndex) {
        super("Log File Status");

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Placeholders until scan is done
        JPanel filePanel = new JPanel(new GridLayout(0, 1));
        filePanel.setBorder(BorderFactory.createTitledBorder("Log File Info"));
        filePanel.add(new JLabel("File: " + logFileName));
        filePanel.add(new JLabel("Total Records: " + numberFormat.format(logIndex.size())));

        JPanel timePanel = new JPanel(new GridLayout(0, 1));
        timePanel.setBorder(BorderFactory.createTitledBorder("Time Range"));

        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(filePanel, BorderLayout.NORTH);
        infoPanel.add(timePanel, BorderLayout.SOUTH);

        centerPanel.add(loadingLabel, BorderLayout.CENTER);

        add(infoPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
        pack();

        new LogScanWorker(logIndex, timePanel).execute();
    }

    private class LogScanWorker extends SwingWorker<Void, Void> {
        private final LogRecordIndex logIndex;
        private final JPanel timePanel;
        private int errors = 0, warnings = 0, info = 0, debug = 0;
        private Instant start = null, end = null;

        public LogScanWorker(LogRecordIndex logIndex, JPanel timePanel) {
            this.logIndex = logIndex;
            this.timePanel = timePanel;
        }

        @Override
        protected Void doInBackground() {
            for (int i = 0; i < logIndex.size(); i++) {
                try {
                    LogRecord r = logIndex.readRecordAt(i);
                    switch (r.level().toUpperCase()) {
                        case "ERROR" -> errors++;
                        case "WARN", "WARNING" -> warnings++;
                        case "INFO" -> info++;
                        case "DEBUG" -> debug++;
                    }
                    Instant ts = r.timestamp();
                    if (start == null || ts.isBefore(start)) start = ts;
                    if (end == null || ts.isAfter(end)) end = ts;
                } catch (Exception ignored) {
                }
            }
            return null;
        }

        @Override
        protected void done() {
            timePanel.removeAll();

            if (start != null && end != null) {
                timePanel.add(new JLabel("Start Time: " + timestampFormatter.format(start)));
                timePanel.add(new JLabel("End Time: " + timestampFormatter.format(end)));
                timePanel.add(new JLabel("Duration: " + formatDuration(Duration.between(start, end))));
            }

            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            if (errors > 0) dataset.setValue("ERROR", errors);
            if (warnings > 0) dataset.setValue("WARN", warnings);
            if (info > 0) dataset.setValue("INFO", info);
            if (debug > 0) dataset.setValue("DEBUG", debug);

            JFreeChart chart = ChartFactory.createPieChart("Log Level Distribution", dataset, true, true, false);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setSectionPaint("ERROR", Color.RED);
            plot.setSectionPaint("WARN", Color.ORANGE);
            plot.setSectionPaint("INFO", Color.BLUE);
            plot.setSectionPaint("DEBUG", Color.GRAY);

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(300, 300));

            centerPanel.removeAll();
            centerPanel.add(chartPanel, BorderLayout.CENTER);

            infoPanel.revalidate();
            infoPanel.repaint();
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();
        return String.format("%02dh %02dm %02ds %03dms", hours, minutes, seconds, millis);
    }
}
