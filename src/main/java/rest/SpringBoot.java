package rest;
import java.io.File;
import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;


@SpringBootApplication
@EnableSwagger2
public class SpringBoot {

    public static void main(String[] args) {


        SingeltonMemory sm = SingeltonMemory.getInstance();


        String twitterDir = args[0]+"/";
        String outDir = args[1]+"/";
        File edgesFile = new File(args[2]);

        sm.init(twitterDir, outDir, edgesFile);
        SpringApplication.run(SpringBoot.class, args);
    }

    @Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/deepWalk.*|/graph.*|/paragraphVectors.*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("DeepWalk4J").
                description("description")
                //.termsOfServiceUrl("http://www-03.ibm.com/software/sla/sladb.nsf/sla/bm?Open")
                //.contact("Marvin Hofer")
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/IBM-Bluemix/news-aggregator/blob/master/LICENSE")
                .version("1.0")
                .build();
    }
}