package es.juntadeandalucia.agapa.pruebasSelenium.suite;

import static org.testng.Assert.assertNotNull;

import com.automation.remarks.testng.UniversalVideoListener;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.JsonFormatter;
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
import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;


/**
 * Test abstracto con funcionalidades comunes a los tests
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

   protected void beforeTest(String titulo, String nombre, String fichero) {
      String ficheroLargo = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + fichero + "/" + fichero + "-Extendido";
      this.spark = new ExtentSparkReporter(ficheroLargo + ".html");
      JsonFormatter json = new JsonFormatter(ficheroLargo + ".json");
      this.extent = new ExtentReports();
      this.extent.attachReporter(json, this.spark);
      this.spark.config().setDocumentTitle(titulo);
      this.spark.config().setReportName(nombre);
   }

   private String getScreenShot(WebDriver driver, String directorio, String screenshotName) throws IOException {
      String dateName = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
      TakesScreenshot ts = (TakesScreenshot) driver;
      File source = ts.getScreenshotAs(OutputType.FILE);
      String directorioLargo = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + directorio + "/";
      String rutaRelativa = VariablesGlobalesTest.DIRECTORIO_CAPTURAS + screenshotName + "_" + dateName + ".png";
      File finalDestination = new File(directorioLargo + rutaRelativa);
      FileUtils.copyFile(source, finalDestination);
      return rutaRelativa;
   }

   @AfterMethod
   public void getResult(ITestResult resultado) throws Exception {
      if (resultado.getStatus() == ITestResult.FAILURE) {
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(resultado.getName() + " - Test falló", ExtentColor.RED));
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(resultado.getThrowable() + " - Test falló", ExtentColor.RED));
         String rutaRelativa = this.getScreenShot(this.getDriver(), resultado.getTestContext().getName(), resultado.getName());
         this.getLogger().addScreenCaptureFromPath(rutaRelativa);
         this.getLogger().fail("Captura de pantalla del test que falló: " + rutaRelativa);
      }
      else if (resultado.getStatus() == ITestResult.SKIP) {
         this.getLogger().log(Status.SKIP, MarkupHelper.createLabel(resultado.getName() + " - Test saltado", ExtentColor.ORANGE));
      }
      else if (resultado.getStatus() == ITestResult.SUCCESS) {
         this.getLogger().log(Status.PASS, MarkupHelper.createLabel(resultado.getName() + " Test CORRECTO", ExtentColor.GREEN));
      }
      this.cerrarNavegador();
   }

   @AfterTest
   public void endReport(ITestContext contexto) {
      this.extent.flush();
   }

   /**
    * Metodo que se debe ejecutar antes de cualquier test.
    *
    * @throws PruebaAceptacionExcepcion
    */
   protected void beforeMethod(String contexto, String nombreTest) throws PruebaAceptacionExcepcion {
      // Level nivelLog = Level.INFO;
      //
      // Logger logger = Logger.getLogger("");
      // logger.setLevel(nivelLog);
      // Arrays.stream(logger.getHandlers()).forEach(handler -> {
      // // handler.setLevel(nivelLog);
      // // handler.setFormatter("%(asctime)s :%(levelname)s : %(name)s :%(message)s");
      // });
      // Logger.getLogger(HttpCommandExecutor.class.getName()).setLevel(Level.FINE);
      // Logger.getLogger(DriverCommandExecutor.class.getName()).setLevel(Level.FINE);
      // Logger.getLogger(RemoteWebDriver.class.getName()).setLevel(nivelLog);
      // Logger.getLogger(SeleniumManager.class.getName()).setLevel(nivelLog);

      try {
         System.setProperty("video.folder", VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + contexto);

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
      chrome.manage().timeouts().implicitlyWait(Duration.ofMillis(1));
      chrome.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
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

   protected void enviarCorreo(String asunto, String cuerpo, String destinatario) {
      assertNotNull(destinatario);
      try {
         Session session = Session.getInstance(TestSeleniumAbstracto.props);
         MimeMessage msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(REMITENTE));
         msg.setSentDate(new Date());
         msg.setSubject(asunto);
         msg.setRecipients(Message.RecipientType.TO, destinatario);
         msg.setContent(cuerpo, "text/html; charset=utf-8");
         Transport.send(msg);
         log.info("Correo de fallo enviado a " + destinatario);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }

}
