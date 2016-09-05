package org.bricolages.streaming;
import org.bricolages.streaming.filter.ObjectFilterFactory;
import org.bricolages.streaming.event.EventQueue;
import org.bricolages.streaming.event.SQSQueue;
import org.bricolages.streaming.s3.S3Agent;
import org.bricolages.streaming.s3.ObjectMapper;
import org.bricolages.streaming.s3.S3ObjectLocation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Objects;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class Application {
    static public void main(String[] args) throws Exception {
        try (val ctx = SpringApplication.run(Application.class, args)) {
            ctx.getBean(Application.class).run(args);
        }
    }

    public void run(String[] args) throws Exception {
        boolean oneshot = false;
        S3ObjectLocation mapUrl = null;
        S3ObjectLocation procUrl = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--config=")) {
                val kv = args[i].split("=", 2);
                if (kv.length != 2) {
                    System.err.println("missing argument for --config");
                    System.exit(1);
                }
                this.configPath = kv[1];
            }
            else if (Objects.equals(args[i], "--oneshot")) {
                oneshot = true;
            }
            else if (args[i].startsWith("--map-url=")) {
                val kv = args[i].split("=", 2);
                if (kv.length != 2) {
                    System.err.println("missing argument for --map-url");
                    System.exit(1);
                }
                mapUrl = S3ObjectLocation.forUrl(kv[1]);
            }
            else if (args[i].startsWith("--process-url=")) {
                val kv = args[i].split("=", 2);
                if (kv.length != 2) {
                    System.err.println("missing argument for --process-url");
                    System.exit(1);
                }
                procUrl = S3ObjectLocation.forUrl(kv[1]);
            }
            else if (Objects.equals(args[i], "--help")) {
                printUsage(System.out);
                System.exit(0);
            }
            else if (args[i].startsWith("-")) {
                System.err.println("unknown option: " + args[i]);
                System.exit(1);
            }
            else {
                int argc = args.length - 1;
                if (argc > 1) {
                    System.err.println("too many arguments");
                    System.exit(1);
                }
                if (argc == 1) {
                    this.configPath = args[i];
                }
                break;
            }
        }

        if (mapUrl != null) {
            val result = mapper().map(mapUrl);
            System.out.println(result.getDestLocation());
            System.exit(0);
        }

        log.info("configPath=" + configPath);
        val preproc = preprocessor();
        if (procUrl != null) {
            val out = new BufferedWriter(new OutputStreamWriter(System.out));
            val success = preproc.processUrl(procUrl, out);
            out.flush();
            if (success) {
                System.err.println("SUCCEEDED");
                System.exit(0);
            }
            else {
                System.err.println("FAILED");
                System.exit(1);
            }
        }
        else if (oneshot) {
            preproc.runOneshot();
        }
        else {
            preproc.run();
        }
    }

    void printUsage(PrintStream s) {
        s.println("Usage: bricolage-streaming-preprocessor [options]");
        s.println("Options:");
        s.println("\t--config=PATH         Use PATH as a streaming preprocess config file.");
        s.println("\t--oneshot             Process one ReceiveMessage and quit.");
        s.println("\t--map-url=S3URL       Prints destination S3 URL for S3URL and quit.");
        s.println("\t--process-url=S3URL   Process the data file S3URL as configured and print to stdout.");
        s.println("\t--help                Prints this message and quit.");
    }

    String configPath = "config/streaming-preprocessor.yml";
    Config config;

    // FIXME: replace by Spring DI
    Config getConfig() {
        if (this.config == null) {
            this.config = Config.load(configPath);
        }
        return this.config;
    }

    @Bean
    public Preprocessor preprocessor() {
        return new Preprocessor(eventQueue(), logQueue(), s3(), mapper(), filterFactory());
    }

    @Bean
    public EventQueue eventQueue() {
        val config = getConfig().eventQueue;
        val sqs = new SQSQueue(new AmazonSQSClient(), config.url);
        if (config.visibilityTimeout > 0) sqs.setVisibilityTimeout(config.visibilityTimeout);
        if (config.maxNumberOfMessages > 0) sqs.setMaxNumberOfMessages(config.maxNumberOfMessages);
        if (config.waitTimeSeconds > 0) sqs.setWaitTimeSeconds(config.waitTimeSeconds);
        return new EventQueue(sqs);
    }

    @Bean
    public LogQueue logQueue() {
        val config = getConfig().logQueue;
        val sqs = new SQSQueue(new AmazonSQSClient(), config.url);
        return new LogQueue(sqs);
    }

    @Bean
    public S3Agent s3() {
        return new S3Agent(new AmazonS3Client());
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper(getConfig().mapping);
    }

    @Bean
    public ObjectFilterFactory filterFactory() {
        return new ObjectFilterFactory();
    }
}
