package rest;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import core.Cli;
import org.apache.commons.cli.CommandLine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Main class
 */
@SpringBootApplication
@EnableSwagger2
public class SpringBoot  extends  WebMvcConfigurerAdapter{

    public static void main(String[] args) {

        // Memory Storage
        SingeltonMemory sm = SingeltonMemory.getInstance();

        // Default DeepWalk and ParagraphVector parameters. dw_vectorSize, pw_layerSize = 3
        int dw_walkLength = 25;
        int dw_windowSize = 10;
        int pv_windowSize = 25; // = Wortzahl

        CommandLine commandLine = Cli.getCommandLine(args);

        String twitterDir = commandLine.getOptionValue("i") + "/";
        String outDir = commandLine.getOptionValue("o") + "/";
        new File(outDir+"csv").mkdirs();

        File edgesFile = new File(commandLine.getOptionValue("e"));
        if(commandLine.hasOption("deepwalk")) {
            String[] split = commandLine.getOptionValue("deepwalk").split(",");
            dw_windowSize = Integer.parseInt(split[1]);
            dw_walkLength = Integer.parseInt(split[0]);
        }

        if(commandLine.hasOption("pre-vec")) {
            pv_windowSize = Integer.parseInt(commandLine.getOptionValue("par-vec"));
        }

        if(!edgesFile.exists()) {
            System.err.println("Edges file not found");
            System.exit(0);
        }

        sm.init(twitterDir, outDir, edgesFile, dw_walkLength, dw_windowSize, pv_windowSize);

        SpringApplication.run(SpringBoot.class, args);
    }

    @Override
    public void addResourceHandlers (ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/vectors/**").
                addResourceLocations("file:"+SingeltonMemory.getInstance().outDir+"csv/");
    }

    @Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/deepWalk.*|/graph.*|/paragraphVectors.*"))
                .build();
    }

    /**
     * Information about provided ReST api.
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Twitter Graph DeepLearning").
                description("Rest Api für das Abfragen von DeepWalk and Paragraph-Vector basierten Graph Embeddings.\n" +
                        "Abfragen um Ähnlichkeiten zwischen Knoten A mit seinen TopK oder Knoten B zu analysieren.\n"+
                        "\nRobert Bielinski und Marvin Hofer")
                //.termsOfServiceUrl("http://www-03.ibm.com/software/sla/sladb.nsf/sla/bm?Open")
                //.contact("Marvin Hofer")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/IBM-Bluemix/news-aggregator/blob/master/LICENSE")
                .version("1.0")
                .build();
    }
}