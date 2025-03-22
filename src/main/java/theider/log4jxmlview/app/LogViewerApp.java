package theider.log4jxmlview.app;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import theider.log4jxmlview.Log4jXmlReader;

@SpringBootApplication
public class LogViewerApp {

    public static void main(String[] args) {
        SpringApplication.run(LogViewerApp.class, args);
    }

    @Bean
    public CommandLineRunner run(Log4jXmlReader reader) {
        return args -> {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("Cannot launch UI: running in headless mode");
            } else {
                SwingUtilities.invokeLater(() -> new LogViewerFrame(reader).setVisible(true));
            }

        };
    }

    @Bean
    public Log4jXmlReader log4jXmlReader() {
        return new Log4jXmlReader();
    }
}
