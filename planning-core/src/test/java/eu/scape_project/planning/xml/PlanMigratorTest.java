package eu.scape_project.planning.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.scape_project.planning.model.PlatoException;
import eu.scape_project.planning.utils.OS;

public class PlanMigratorTest {
    private static final Logger log = LoggerFactory.getLogger(PlanMigratorTest.class);
    
    private File tempDir;
    private String tempPath;
    
    @Before
    public void setUp(){
        tempPath = OS.getTmpPath() + "planmigrator_test" + System.currentTimeMillis() + File.separator;
        tempDir = new File(tempPath);
        tempDir.deleteOnExit();
        tempDir.mkdirs();
    }
    
    @After
    public void tearDown(){
        OS.deleteDirectory(tempDir);
    }

    @Test
    public void migrateBLTiffFromV300() throws PlatoException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
        PlanMigrator migrator = new PlanMigrator();
        
        List<String> appliedTransformations = new ArrayList<String>();
        String currentVersionData = migrator.getCurrentVersionData(getClass().getClassLoader().getResourceAsStream("plans/British_Library_Newspapers.xml"), tempPath, appliedTransformations);
        
        Assert.assertNotNull(currentVersionData);
        
        ValidatingParserFactory validatingParserFactory = new ValidatingParserFactory();
        SAXParser parser = validatingParserFactory.getValidatingParser();
        
        parser.setProperty(ValidatingParserFactory.JAXP_SCHEMA_SOURCE, PlanXMLConstants.PLATO_SCHEMA_URI);
        SchemaResolver schemaResolver = new SchemaResolver()
            .addSchemaLocation(PlanXMLConstants.PLATO_SCHEMA_URI, PlanXMLConstants.PLATO_SCHEMA_LOCATION)
            .addSchemaLocation(PlanXMLConstants.PAP_SCHEMA_URI, PlanXMLConstants.PAP_SCHEMA_LOCATION)
            .addSchemaLocation(PlanXMLConstants.TAVERNA_SCHEMA_URI, PlanXMLConstants.TAVERNA_SCHEMA_LOCATION);
        
        parser.parse(new FileInputStream(currentVersionData), new StrictDefaultHandler(schemaResolver));
    }
    
    @Test
    public void migrateFromV300WithoutSamples() throws PlatoException, ParserConfigurationException, SAXException, FileNotFoundException, IOException{
        PlanMigrator migrator = new PlanMigrator();
        
        
        List<String> appliedTransformations = new ArrayList<String>();
        String currentVersionData = migrator.getCurrentVersionData(getClass().getClassLoader().getResourceAsStream("plans/Archiving_Digital_Photographs.xml"), tempPath, appliedTransformations);
        
        Assert.assertNotNull(currentVersionData);
        
        ValidatingParserFactory validatingParserFactory = new ValidatingParserFactory();
        SAXParser parser = validatingParserFactory.getValidatingParser();
        
        parser.setProperty(ValidatingParserFactory.JAXP_SCHEMA_SOURCE, PlanXMLConstants.PLATO_SCHEMA_URI);
        SchemaResolver schemaResolver = new SchemaResolver()
            .addSchemaLocation(PlanXMLConstants.PLATO_SCHEMA_URI, PlanXMLConstants.PLATO_SCHEMA_LOCATION)
            .addSchemaLocation(PlanXMLConstants.PAP_SCHEMA_URI, PlanXMLConstants.PAP_SCHEMA_LOCATION)
            .addSchemaLocation(PlanXMLConstants.TAVERNA_SCHEMA_URI, PlanXMLConstants.TAVERNA_SCHEMA_LOCATION);
        
        parser.parse(new FileInputStream(currentVersionData), new StrictDefaultHandler(schemaResolver));
    }
    
    @Ignore
    @Test
    public void migrateDirectory() throws PlatoException, FileNotFoundException{
        PlanMigrator migrator = new PlanMigrator();
        
        File dir = new File("d://plans");
        
        File plans[] = dir.listFiles();
        for (File plan : plans) {
            if (plan.isFile() && plan.getName().endsWith(".xml")) {
                List<String> appliedTransformations = new ArrayList<String>();
                String currentVersionData = migrator.getCurrentVersionData(new FileInputStream(plan), tempPath, appliedTransformations);
                File migratedFile = new File(currentVersionData);
                File outFile = new File("d://plans//out//" + migratedFile.getName());
                if (!migratedFile.renameTo(outFile)) {
                    log.error("Failed to move file : " + currentVersionData);
                }
            }
        }
        
    }

}
