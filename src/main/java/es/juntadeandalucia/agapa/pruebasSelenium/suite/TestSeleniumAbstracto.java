package es.juntadeandalucia.agapa.pruebasSelenium.suite;

import static org.testng.Assert.assertNotNull;

import com.automation.remarks.testng.UniversalVideoListener;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.reports.InformeListener;
import es.juntadeandalucia.agapa.pruebasSelenium.reports.ResumenListener;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.Traza;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory.Navegador;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;


/**
 * Añade por defecto listeners
 */
@Listeners({ ResumenListener.class, InformeListener.class, UniversalVideoListener.class })
public abstract class TestSeleniumAbstracto extends AbstractTestNGSpringContextTests {

   protected ExtentReports     extent;
   private ExtentSparkReporter spark;
   protected ExtentTest        logger;

   public WebDriver getDriver() {
      return WebDriverFactory.getDriver();
   }

   @BeforeTest
   public void startReport() {
      // Create an object of Extent Reports
      this.extent = new ExtentReports();

      this.spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/test-output/STMExtentReport.html");
      this.extent.attachReporter(this.spark);
      // this.extent.setSystemInfo("Host Name", "SoftwareTestingMaterial");
      // this.extent.setSystemInfo("Environment", "Production");
      // this.extent.setSystemInfo("User Name", "Rajkumar SM");
      this.spark.config().setDocumentTitle("PROA");
      // Name of the report
      this.spark.config().setReportName("PROA");
      // Dark Theme
      // this.spark.config().setTheme(Theme.STANDARD);
   }

   // This method is to capture the screenshot and return the path of the screenshot.
   public static String getScreenShot(WebDriver driver, String screenshotName) throws IOException {
      String dateName = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
      TakesScreenshot ts = (TakesScreenshot) driver;
      File source = ts.getScreenshotAs(OutputType.FILE);
      // after execution, you could see a folder "FailedTestsScreenshots" under src folder
      String destination = System.getProperty("user.dir") + "/test-output/pantallazos/" + screenshotName + dateName + ".png";
      File finalDestination = new File(destination);
      FileUtils.copyFile(source, finalDestination);
      return destination;
   }

   @AfterMethod
   public void getResult(ITestResult result) throws Exception {
      if (result.getStatus() == ITestResult.FAILURE) {
         // MarkupHelper is used to display the output in different colors
         this.logger.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test falló", ExtentColor.RED));
         this.logger.log(Status.FAIL, MarkupHelper.createLabel(result.getThrowable() + " - Test falló", ExtentColor.RED));
         // To capture screenshot path and store the path of the screenshot in the string "screenshotPath"
         // We do pass the path captured by this method in to the extent reports using "logger.addScreenCapture" method.
         // String Scrnshot=TakeScreenshot.captuerScreenshot(driver,"TestCaseFailed");
         String screenshotPath = getScreenShot(this.getDriver(), result.getName());
         // To add it in the extent report
         this.logger.addScreenCaptureFromPath(screenshotPath);
         this.logger.fail("Test Case Failed Snapshot is below " + screenshotPath);
      }
      else if (result.getStatus() == ITestResult.SKIP) {
         this.logger.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " - Test saltado", ExtentColor.ORANGE));
      }
      else if (result.getStatus() == ITestResult.SUCCESS) {
         this.logger.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " Test CORRECTO", ExtentColor.GREEN));
      }
      this.cerrarNavegador();
   }

   @AfterTest
   public void endReport() {
      this.extent.flush();
   }

   /**
    * Metodo que se ejecuta antes de los test.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void beforeMethod() throws PruebaAceptacionExcepcion {
      try {
         // Indicara la carpeta donde se guardaran los videos.
         System.setProperty("video.folder", System.getProperty("user.dir") + "//test-output//video//" + this.getClass().getSimpleName());

         this.iniciar();
      }
      catch (Exception e) {
         Traza.error(e.getLocalizedMessage());
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
   }

   public void cerrarNavegador() throws PruebaAceptacionExcepcion {
      // ChromeDriver chrome = (ChromeDriver) WebDriverFactory.getDriver();
      // chrome.close();
      WebDriverFactory.getDriver().quit();
      WebDriverFactory.setDriver(null);
   }

   private void iniciar() throws PruebaAceptacionExcepcion {
      Navegador navegador = Navegador.valueOf(VariablesGlobalesTest.getPropiedad(PropiedadesTest.NAVEGADOR));
      WebDriverFactory.setDriver(WebDriverFactory.obtenerInstancia(navegador));
      ChromeDriver chrome = (ChromeDriver) WebDriverFactory.getDriver();
      Traza.info(chrome.toString());
      assertNotNull(WebDriverFactory.getDriver(), "Error al instanciar el driver de " + navegador);
      String propiedadMaximizar = null;
      try {
         propiedadMaximizar = VariablesGlobalesTest.getPropiedad(PropiedadesTest.MAXIMIZAR);
      }
      catch (PruebaAceptacionExcepcion e) {
         Traza.info("Propiedad MAXIMIZAR no definida. Se asume como TRUE");
      }
      boolean maximizar;
      if (StringUtils.isEmpty(propiedadMaximizar)) {
         maximizar = true;
      }
      else {
         maximizar = Boolean.parseBoolean(propiedadMaximizar);
      }
      if (maximizar) {
         WebDriverFactory.getDriver().manage().window().maximize();
      }
   }

}
