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
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;


/**
 * Añade por defecto listeners
 */
@Listeners({ ResumenListener.class, InformeListener.class, UniversalVideoListener.class })
@Slf4j
public abstract class TestSeleniumAbstracto extends AbstractTestNGSpringContextTests {

   private static final String     REMITENTE = "noreply.agapa@juntadeandalucia.es";
   private static final String     HOST      = "mtaprod.dap.es";
   private static final String     PORT      = "25";
   private static final Properties props     = new Properties();

   static {
      props.put("mail.smtp.host", HOST);
      props.put("mail.smtp.starttls.enable", "false");
      props.put("mail.smtp.port", PORT);
      props.put("mail.smtp.mail.sender", REMITENTE);
      props.put("mail.smtp.auth", "false");
   }

   protected ExtentReports     extent;
   private ExtentSparkReporter spark;

   private WebDriver getDriver() {
      return WebDriverFactory.getDriver();
   }

   protected ExtentTest getLogger() {
      return WebDriverFactory.getLogger();
   }

   @BeforeTest
   public void startReport() {
      // Create an object of Extent Reports
      this.extent = new ExtentReports();

      this.spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/target/surefire-reports/STMExtentReport.html");
      this.extent.attachReporter(this.spark);
      // this.extent.setSystemInfo("Environment", "Production");
      this.spark.config().setDocumentTitle("PROA");
      // Name of the report
      this.spark.config().setReportName("PROA");
   }

   // This method is to capture the screenshot and return the path of the screenshot.
   public static String getScreenShot(WebDriver driver, String screenshotName) throws IOException {
      String dateName = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
      TakesScreenshot ts = (TakesScreenshot) driver;
      File source = ts.getScreenshotAs(OutputType.FILE);
      String destination =
            System.getProperty("user.dir") + "/target/surefire-reports/pantallazos/" + screenshotName + "_" + dateName + ".png";
      File finalDestination = new File(destination);
      FileUtils.copyFile(source, finalDestination);
      return destination;
   }

   @AfterMethod
   public void getResult(ITestResult result) throws Exception {
      if (result.getStatus() == ITestResult.FAILURE) {
         // MarkupHelper is used to display the output in different colors
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test falló", ExtentColor.RED));
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(result.getThrowable() + " - Test falló", ExtentColor.RED));
         // To capture screenshot path and store the path of the screenshot in the string "screenshotPath"
         // We do pass the path captured by this method in to the extent reports using "logger.addScreenCapture" method.
         // String Scrnshot=TakeScreenshot.captuerScreenshot(driver,"TestCaseFailed");
         String screenshotPath = getScreenShot(this.getDriver(), result.getName());
         // To add it in the extent report
         this.getLogger().addScreenCaptureFromPath(screenshotPath);
         this.getLogger().fail("Test Case Failed Snapshot is below " + screenshotPath);
         if ("SI".equalsIgnoreCase(VariablesGlobalesTest.getPropiedad(PropiedadesTest.ENVIAR_CORREO_FALLO))) {
            ITestNGMethod metodo = result.getMethod();
            String asunto = "PROA: Pruebas selenium. El test " + metodo.getInstance().getClass().getSimpleName() + "." + result.getName()
                  + " (" + metodo.getDescription() + ") ha fallado";
            // String cuerpo = "<h3>El test <b>" + result.getName() + " (" + result.getMethod().getDescription() + ")</b> ha fallado.</h3>";
            this.enviarCorreo(asunto, asunto);
         }
      }
      else if (result.getStatus() == ITestResult.SKIP) {
         this.getLogger().log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " - Test saltado", ExtentColor.ORANGE));
      }
      else if (result.getStatus() == ITestResult.SUCCESS) {
         this.getLogger().log(Status.PASS, MarkupHelper.createLabel(result.getName() + " Test CORRECTO", ExtentColor.GREEN));
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
   protected void beforeMethod() throws PruebaAceptacionExcepcion {
      Level nivelLog = Level.FINEST;

      Logger logger = Logger.getLogger("");
      logger.setLevel(nivelLog);
      Arrays.stream(logger.getHandlers()).forEach(handler -> {
         // handler.setLevel(nivelLog);
      });
      // Logger.getLogger(RemoteWebDriver.class.getName()).setLevel(nivelLog);
      // Logger.getLogger(SeleniumManager.class.getName()).setLevel(nivelLog);

      try {
         // Indicara la carpeta donde se guardaran los videos.
         System.setProperty("video.folder",
               System.getProperty("user.dir") + "/target/surefire-reports/video/" + this.getClass().getSimpleName());

         this.iniciar();
      }
      catch (Exception e) {
         Traza.error(e.getLocalizedMessage());
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
   }

   private void cerrarNavegador() throws PruebaAceptacionExcepcion {
      // ChromeDriver chrome = (ChromeDriver) WebDriverFactory.getDriver();
      // chrome.close();
      WebDriverFactory.getDriver().close();
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

   public void enviarCorreo(String asunto, String cuerpo) throws PruebaAceptacionExcepcion {
      String destinatario = VariablesGlobalesTest.getPropiedad(PropiedadesTest.DESTINATARIO_CORREO);
      assertNotNull(destinatario);
      this.enviarEmail(asunto, destinatario, cuerpo, new Date());
   }

   private void enviarEmail(String asunto, String destinatarios, String cuerpo, Date fecha) {
      try {
         Session session = Session.getInstance(TestSeleniumAbstracto.props);
         MimeMessage msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(REMITENTE));
         msg.setSentDate(fecha);
         msg.setSubject(asunto);
         msg.setRecipients(Message.RecipientType.TO, destinatarios);
         msg.setContent(cuerpo, "text/html; charset=utf-8");
         Transport.send(msg);
         log.info("Correo de fallo enviado a " + destinatarios);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }
}
