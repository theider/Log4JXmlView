package theider.log4jxmlview.app;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogViewerApp {

    public static void main(String[] args) {
        SpringApplication.run(LogViewerApp.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("Cannot launch UI: running in headless mode");
            } else {
                SwingUtilities.invokeLater(() -> new LogViewerAppFrame().setVisible(true));
            }

        };
    }

}
