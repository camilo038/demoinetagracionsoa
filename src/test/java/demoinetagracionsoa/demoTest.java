package demoinetagracionsoa;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;



public class demoTest extends CamelSpringTestSupport {

    private static final String PROPERTIES_FILE_DIR = "src/test/resources/";
    private static Properties testProperties = new Properties();


    @Test
    	public void testTaxiTimex() throws Exception {
    	
	    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.mq.jms.MQConnectionFactory");
	    
	    File file = new File((PROPERTIES_FILE_DIR + "/request/pruebaconinfo.txt"));
	    file = file.getAbsoluteFile();

    	
//		byte[] bytes = Files.readAllBytes(Paths.get(PROPERTIES_FILE_DIR + "/request/prueba.txt"));
		
		
		RouteDefinition routeDefinition = context.getRouteDefinition("simple-route");
		routeDefinition.adviceWith(context, new AdviceWithRouteBuilder() {

			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:inputEndpoint");

				interceptSendToEndpoint("jms:queue:test").skipSendToOriginalEndpoint()
						.log("::: Message to test: ${body}");
//				
//				interceptSendToEndpoint("vm:loggingXML4OPSBatch").skipSendToOriginalEndpoint()
//				.log("::: Log Message: vm:loggingXML4OPSBatch").to("mock://endRoute");
				
			}
		});

		context.start();
		getMockEndpoint("mock://endRoute").expectedMinimumMessageCount(1);
		template.sendBody("direct:inputEndpoint", file);

		
	}

	@Override
	public boolean isUseAdviceWith() {
		return true;
	}


    /**
     * Carga del archivo de propiedades para los Test Unitarios
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void init() throws Exception {
	testProperties.load(demoTest.class.getResourceAsStream("/application.properties"));
    }

    @BeforeClass
    public static void setUpProperties() throws Exception {
	System.setProperty("karaf.home", PROPERTIES_FILE_DIR);
	System.setProperty("project.artifactId", "Test-Maven-Artifact");
    }


    @Override
    protected AbstractApplicationContext createApplicationContext() {
	return new ClassPathXmlApplicationContext("spring/camel-context.xml");
    }
    
    

    

}
