package es.juntadeandalucia.agapa.pruebasSelenium.suite;

import com.automation.remarks.testng.utils.MethodUtils;
import com.automation.remarks.video.annotations.Video;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.JsonFormatter;
import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.listeners.InformeListener;
import es.juntadeandalucia.agapa.pruebasSelenium.listeners.ResumenListener;
import es.juntadeandalucia.agapa.pruebasSelenium.listeners.VideoListener;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.AutoSeleccionarCertificadoEnLogin;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.Traza;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.WindowsRegistry;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory.Navegador;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


/**
 * Test abstracto con funcionalidades comunes a los tests.
 *
 * @author AGAPA
 */
@Listeners({ ResumenListener.class, InformeListener.class, VideoListener.class })
@Slf4j
public abstract class TestSeleniumAbstracto extends AbstractTestNGSpringContextTests {

   /** La constante REMITENTE. */
   private static final String     REMITENTE = "noreply.agapa@juntadeandalucia.es";

   /** La constante HOST. */
   private static final String     HOST      = "mtaprod.dap.es";

   /** La constante PORT. */
   private static final String     PORT      = "25";

   /** La constante props. */
   private static final Properties props     = new Properties();

   static {
      TestSeleniumAbstracto.props.put("mail.smtp.host", TestSeleniumAbstracto.HOST);
      TestSeleniumAbstracto.props.put("mail.smtp.starttls.enable", "false");
      TestSeleniumAbstracto.props.put("mail.smtp.port", TestSeleniumAbstracto.PORT);
      TestSeleniumAbstracto.props.put("mail.smtp.mail.sender", TestSeleniumAbstracto.REMITENTE);
      TestSeleniumAbstracto.props.put("mail.smtp.auth", "false");
   }

   /** extent. */
   protected ExtentReports     extent;

   /** spark. */
   private ExtentSparkReporter spark;

   /**
    * Obtención del atributo: driver.
    *
    * @return atributo: driver
    */
   private WebDriver getDriver() {
      return WebDriverFactory.getDriver();
   }

   /**
    * Obtención del atributo: logger.
    *
    * @return atributo: logger
    */
   protected ExtentTest getLogger() {
      return WebDriverFactory.getLogger();
   }

   /**
    * Before test.
    *
    * @param titulo
    *           valor para: titulo
    * @param nombre
    *           valor para: nombre
    * @param fichero
    *           valor para: fichero
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   protected void beforeTest(String titulo, String nombre, String fichero) throws PruebaAceptacionExcepcion {
      String ficheroLargo = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + fichero + File.separator + fichero + "-Extendido";
      this.spark = new ExtentSparkReporter(ficheroLargo + ".html");
      JsonFormatter json = new JsonFormatter(ficheroLargo + ".json");
      this.extent = new ExtentReports();
      this.extent.attachReporter(json, this.spark);
      this.spark.config().setDocumentTitle(titulo);
      this.spark.config().setReportName(nombre);
   }

   /**
    * Obtención del atributo: screen shot.
    *
    * @param driver
    *           valor para: driver
    * @param directorio
    *           valor para: directorio
    * @param screenshotName
    *           valor para: screenshot name
    * @return atributo: screen shot
    * @throws IOException
    *            Signals that an I/O exception has occurred.
    */
   private String getScreenShot(WebDriver driver, String directorio, String screenshotName) throws IOException {
      String dateName = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
      TakesScreenshot ts = (TakesScreenshot) driver;
      File origen = ts.getScreenshotAs(OutputType.FILE);
      String directorioLargo = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + directorio + File.separator;
      String rutaRelativa = VariablesGlobalesTest.DIRECTORIO_CAPTURAS + screenshotName + "_" + dateName + ".png";
      File destinoFinal = new File(directorioLargo + rutaRelativa);
      FileUtils.copyFile(origen, destinoFinal);
      return rutaRelativa;
   }

   /**
    * Obtención del atributo: result.
    *
    * @param resultado
    *           valor para: resultado
    * @throws Exception
    *            la exception
    */
   @AfterMethod
   public void getResult(ITestResult resultado) throws Exception {
      String nombreVideo = "";
      if (resultado.getStatus() == ITestResult.FAILURE) {
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(resultado.getName() + " - Test falló", ExtentColor.RED));
         this.getLogger().log(Status.FAIL, MarkupHelper.createLabel(resultado.getThrowable() + " - Test falló", ExtentColor.RED));
         String rutaRelativa = this.getScreenShot(this.getDriver(), resultado.getTestContext().getName(), resultado.getName());
         this.getLogger().addScreenCaptureFromPath(rutaRelativa);
         this.getLogger().fail("Captura de pantalla del test que falló: " + rutaRelativa);
         this.cerrarNavegador();
         if (VariablesGlobalesTest.IS_DOCKER && VariablesGlobalesTest.IS_VIDEO_GRABAR) {
            nombreVideo = this.moverVideo(resultado);
         }
      }
      else if (resultado.getStatus() == ITestResult.SKIP) {
         this.getLogger().log(Status.SKIP, MarkupHelper.createLabel(resultado.getName() + " - Test saltado", ExtentColor.ORANGE));
         this.cerrarNavegador();
      }
      else if (resultado.getStatus() == ITestResult.SUCCESS) {
         this.getLogger().log(Status.PASS, MarkupHelper.createLabel(resultado.getName() + " Test CORRECTO", ExtentColor.GREEN));
         this.cerrarNavegador();
         if (VariablesGlobalesTest.IS_DOCKER && VariablesGlobalesTest.IS_VIDEO_GRABAR && VariablesGlobalesTest.IS_VIDEO_GRABAR_TODOS) {
            nombreVideo = this.moverVideo(resultado);
         }
      }

      // *** HTML para el video. OJO solo cuando es DOCKER=true ***
      // NO - Códec: TechSmith Camtasia Screen Capture (tscc)
      // SI - Códec: Google/On2's VP8 Video (VP80)
      // SI - Códec: H264 - MPEG-4 AVC (part 10) (avc1)
      if (!StringUtils.isBlank(nombreVideo)) {
         // Si nombreVideo, está rellena se ha grabado con DOCKER, el codec es compatible(H264) y se puede integrar en
         // el navegador
         String pathAqui = "./" + nombreVideo;
         String pathPPI = "./" + resultado.getTestContext().getName() + "/" + nombreVideo;
         String videoHtml = "<video width='600' controls>" + "<source src='" + pathAqui + "' type='video/mp4'>" + "<source src='" + pathPPI
               + "' type='video/mp4'>" + "Su navegador no soporta la reproducción de video." + "</video>";
         this.getLogger().log(Status.INFO, videoHtml);
      }
   }

   /**
    * End report.
    *
    * @param contexto
    *           valor para: contexto
    */
   @AfterTest
   public void endReport(ITestContext contexto) {
      this.extent.flush();
   }

   /**
    * Metodo que se debe ejecutar antes de cualquier test.
    *
    * @param contexto
    *           valor para: contexto
    * @param metodo
    *           valor para: metodo
    * @param filtroAutoseleccionCertificado
    *           valor para: filtro autoseleccion certificado
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   protected void beforeMethod(ITestContext contexto, Method metodo, String filtroAutoseleccionCertificado)
         throws PruebaAceptacionExcepcion {
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

      String nombreTest = metodo.getAnnotation(Test.class).description();
      WebDriverFactory.setLogger(this.extent.createTest(nombreTest));
      this.configurarVideo(contexto.getName());
      AutoSeleccionarCertificadoEnLogin[] anotaciones = metodo.getAnnotationsByType(AutoSeleccionarCertificadoEnLogin.class);
      boolean postConfiguracionCertificado = false;
      if (anotaciones.length > 0) {
         postConfiguracionCertificado = this.configurarCertificado(filtroAutoseleccionCertificado, anotaciones[0].value());
      }

      this.iniciar();

      if (postConfiguracionCertificado) {
         this.refrescarPoliticasChrome();
      }
   }

   /**
    * Configurar video.
    *
    * @param contexto
    *           valor para: contexto
    */
   private void configurarVideo(String contexto) {
      if (VariablesGlobalesTest.IS_DOCKER) {
         System.setProperty("video.enabled", Boolean.FALSE.toString());
      }
      else {
         System.setProperty("video.enable", Boolean.toString(VariablesGlobalesTest.IS_VIDEO_GRABAR));
         if (VariablesGlobalesTest.IS_VIDEO_GRABAR_TODOS) {
            System.setProperty("video.save.mode", "ALL");
         }
         System.setProperty("video.folder", VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + contexto);
      }
   }

   /**
    * Cerrar navegador.
    */
   private void cerrarNavegador() {
      try {
         this.getDriver().close();
         this.getDriver().quit();
      }
      catch (Exception e) {
         TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
      }
      WebDriverFactory.setDriver(null);
   }

   /**
    * Iniciar.
    */
   private void iniciar() {
      Navegador navegador = Navegador.valueOf(VariablesGlobalesTest.getPropiedad(PropiedadesTest.NAVEGADOR));
      WebDriverFactory.setDriver(WebDriverFactory.obtenerInstancia(navegador));
      Assert.assertNotNull(this.getDriver(), "Error al instanciar el driver de " + navegador);
      // WebDriver chrome = getDriver();
      // log.debug(chrome.manage().timeouts().getPageLoadTimeout().toString());
      // log.debug(chrome.manage().timeouts().getImplicitWaitTimeout().toString());
      // log.debug(chrome.manage().timeouts().getScriptTimeout().toString());
      // chrome.manage().timeouts().implicitlyWait(Duration.ofMillis(1));
      // chrome.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));

      // ------------------------------
      // Si estamos en modo GRAFICO:
      // Podemos mover ventana al segundo monitor
      // tambien podemos maximizar.
      if (!VariablesGlobalesTest.IS_HEADLESS) {
         // Para ejecutar en segundo monitor
         if (!VariablesGlobalesTest.IS_VIDEO_GRABAR) {
            GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            int posicion = 0;
            if (screens.length > 1) {
               for (GraphicsDevice screen : screens) {
                  int margen = screen.getDefaultConfiguration().getBounds().x;
                  if (margen != 0) {
                     posicion = margen;
                  }
               }
               this.getDriver().manage().window().setPosition(new Point(posicion, 0));
            }
         }

         // Para MAXIMIZAR
         String propiedadMaximizar = null;
         try {
            propiedadMaximizar = VariablesGlobalesTest.getPropiedad(PropiedadesTest.MAXIMIZAR);
         }
         catch (Exception e) {
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
            this.getDriver().manage().window().maximize();
         }
      }

      if (VariablesGlobalesTest.BORRAR_CACHE) {
         this.borrarCache();
      }
   }

   /**
    * Borrar cache.
    */
   private void borrarCache() {
      if (VariablesGlobalesTest.IS_MODO_INCOGNITO) {
         // Si el navegador está en modo incognito no es necesario limpiar la cache ya lo gestiona él.
         return;
      }
      // OJO!!!, La siguiente instruccion, si el navegador está en modo incognito cierra la ventana.
      this.getDriver().get("chrome://settings/clearBrowserData");
      WebElement shadowHostL1 = this.getDriver().findElement(By.cssSelector("settings-ui"));
      WebElement shadowElementL1 = this.getShadowElement(shadowHostL1, "settings-main");
      WebElement shadowElementL2 = this.getShadowElement(shadowElementL1, "settings-basic-page");
      WebElement shadowElementL3 = this.getShadowElement(shadowElementL2, "settings-section > settings-privacy-page");
      WebElement shadowElementL4 = this.getShadowElement(shadowElementL3, "settings-clear-browsing-data-dialog");
      WebElement shadowElementL5 = this.getShadowElement(shadowElementL4, "#clearBrowsingDataDialog");
      WebElement borrarCache = shadowElementL5.findElement(By.cssSelector("#clearBrowsingDataConfirm"));

      JavascriptExecutor js = (JavascriptExecutor) this.getDriver();
      js.executeScript("arguments[0].setAttribute('style', arguments[1]);", borrarCache, "background: yellow; border: 3px solid black;");
      borrarCache.click();
      WebDriverWait wait = new WebDriverWait(this.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
            Duration.ofMillis(100));
      wait.until(ExpectedConditions.invisibilityOf(borrarCache));
   }

   /**
    * Obtención del atributo: shadow element.
    *
    * @param shadowHost
    *           valor para: shadow host
    * @param cssOfShadowElement
    *           valor para: css of shadow element
    * @return atributo: shadow element
    */
   private WebElement getShadowElement(WebElement shadowHost, String cssOfShadowElement) {
      SearchContext shadowRoot = shadowHost.getShadowRoot();
      return shadowRoot.findElement(By.cssSelector(cssOfShadowElement));
   }

   /**
    * Enviar correo.
    *
    * @param asunto
    *           valor para: asunto
    * @param cuerpo
    *           valor para: cuerpo
    * @param destinatario
    *           valor para: destinatario
    */
   protected void enviarCorreo(String asunto, String cuerpo, String destinatario) {
      Assert.assertNotNull(destinatario);
      try {
         Session session = Session.getInstance(TestSeleniumAbstracto.props);
         MimeMessage msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(TestSeleniumAbstracto.REMITENTE));
         msg.setSentDate(new Date());
         msg.setSubject(asunto);
         msg.setRecipients(Message.RecipientType.TO, destinatario);
         msg.setContent(cuerpo, "text/html; charset=utf-8");
         Transport.send(msg);
         TestSeleniumAbstracto.log.info("Correo enviado a " + destinatario);
      }
      catch (Exception e) {
         TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
      }
   }

   /**
    * Configurar certificado.
    *
    * @param patron
    *           valor para: patron
    * @param filtro
    *           valor para: filtro
    * @return true, si es correcto
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   protected boolean configurarCertificado(String patron, String filtro) throws PruebaAceptacionExcepcion {
      boolean postConfiguracionCertificado = false;
      Navegador navegador = Navegador.valueOf(VariablesGlobalesTest.getPropiedad(PropiedadesTest.NAVEGADOR));
      if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
         switch (navegador) {
            case CHROME:
               this.configurarCertificadoLinuxChrome(patron, filtro);
               break;
            case FIREFOX:
               // TODO
               break;
            case MSEDGE:
               // TODO
               break;
         }
      }
      else if (SystemUtils.IS_OS_WINDOWS) {
         switch (navegador) {
            case CHROME:
               postConfiguracionCertificado = this.configurarCertificadoWindowsChrome(patron, filtro);
               break;
            case FIREFOX:
               // TODO
               break;
            case MSEDGE:
               // TODO
               break;
         }
      }
      return postConfiguracionCertificado;
   }

   /**
    * Configurar certificado linux chrome.
    *
    * @param patron
    *           valor para: patron
    * @param filtro
    *           valor para: filtro
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   private void configurarCertificadoLinuxChrome(String patron, String filtro) throws PruebaAceptacionExcepcion {
      FileWriter writer = null;
      try {
         File json = new File("/etc/opt/chrome/policies/managed/auto_seleccionar_certificado.json");
         FileUtils.touch(json);
         writer = new FileWriter(json);
         String contenido = "{\"AutoSelectCertificateForUrls\": [\"{\\\"pattern\\\":\\\"" + patron + "\\\",\\\"filter\\\":"
               + filtro.replace("\"", "\\\"") + "}\"]}";
         TestSeleniumAbstracto.log.info(contenido);
         writer.write(contenido);
      }
      catch (IOException e) {
         TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
      finally {
         if (writer != null) {
            try {
               writer.close();
            }
            catch (IOException e) {
               TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
               throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
            }
         }
      }
   }

   /**
    * Solo funcionará si se ha logado como administrador. Desde Eclipse probablemente no funcionará porque no es habitual iniciar Eclipse
    * como administrador.
    *
    * @param patron
    *           valor para: patron
    * @param filtro
    *           valor para: filtro
    * @return true, si es correcto
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   private boolean configurarCertificadoWindowsChrome(String patron, String filtro) throws PruebaAceptacionExcepcion {
      boolean exito = false;
      String key = "SOFTWARE\\Policies\\Google\\Chrome\\AutoSelectCertificateForUrls";
      String valueName = "1";
      String value = "{\"pattern\":\"" + patron + "\",\"filter\":" + filtro + "}";
      try {
         TestSeleniumAbstracto.log.info("key=" + WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, key, valueName));
         WindowsRegistry.writeStringValue(WindowsRegistry.HKEY_LOCAL_MACHINE, key, valueName, value);
         TestSeleniumAbstracto.log.info("key=" + WindowsRegistry.readString(WindowsRegistry.HKEY_LOCAL_MACHINE, key, valueName));
         exito = true;
      }
      catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
         TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
      }
      return exito;
   }

   /**
    * Refrescar politicas chrome.
    *
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   protected void refrescarPoliticasChrome() throws PruebaAceptacionExcepcion {
      this.getDriver().get("chrome://policy");
      WebDriverFactory.getWebElementWrapper().click(By.id("reload-policies"));

      By mensajesEmergentes = By.id("toast-container");

      int tiempo = Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO));

      WebDriverWait wait = new WebDriverWait(this.getDriver(), Duration.ofSeconds(tiempo), Duration.ofMillis(100));
      try {
         wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(mensajesEmergentes, 0));
      }
      catch (WebDriverException e) {
         String mensaje = "No se pueden refrescar las políticas de Chrome";
         TestSeleniumAbstracto.log.error(mensaje);
         throw e;
      }
      try {
         wait.until(ExpectedConditions.numberOfElementsToBeLessThan(mensajesEmergentes, 2));
      }
      catch (WebDriverException e) {
         String mensaje = "No se pueden refrescar las políticas de Chrome";
         TestSeleniumAbstracto.log.error(mensaje);
         throw e;
      }
   }

   /**
    * Formatear nombre fichero.
    *
    * @param ruta
    *           valor para: ruta
    * @return string
    */
   public static String formatearNombreFichero(String ruta) {
      TestSeleniumAbstracto.log.info("Formateando el nombre " + ruta);
      if (StringUtils.isBlank(ruta)) {
         return "";
      }
      ruta = ruta.replace(":", ""); // .replace(" ", "_");

      // Si es muy largo, lo recortamos
      if (ruta.length() > 180) {
         ruta = ruta.substring(0, 180 - 1);
      }

      // Si termina con punto, lo quitamos!!
      if (ruta.endsWith(".")) {
         ruta = ruta.substring(0, ruta.length() - 1);
      }

      return ruta;
   }

   /**
    * Validar fichero.
    *
    * @param ruta
    *           valor para: ruta
    * @return true, si es correcto
    */
   public static boolean validarFichero(String ruta) {
      try {
         Path path = Paths.get(ruta);
         return (Files.exists(path));
      }
      catch (Exception e) {
         return false;
      }

   }

   /**
    * Mover video.
    *
    * @param resultado
    *           valor para: resultado
    * @return string
    */
   private String moverVideo(ITestResult resultado) {
      // Solo se guarda si IS_DOCKER. Si no IS_DOCKER se guardará con video-recorder-testng.
      Video anotacionVideo = MethodUtils.getVideoAnnotation(resultado);
      String nombreDestino = "";
      if (anotacionVideo != null) {
         String directorio = resultado.getTestContext().getName();
         File origen = new File(System.getProperty("user.dir"));
         String directorioLargo = VariablesGlobalesTest.DIRECTORIO_TARGET_SUREFIRE_REPORTS + directorio + File.separator;
         File video = null;
         String destinoRuta = "Sin asignar";
         try {
            Iterator<File> it = FileUtils.iterateFiles(origen, new String[] { "mp4" }, false);
            while (it.hasNext()) {
               nombreDestino = anotacionVideo.name() + "_" + new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date()) + ".mp4";
               File destino = new File(directorioLargo + nombreDestino);
               destinoRuta = destino.getAbsolutePath();
               destino.delete();
               video = it.next();
               FileUtils.moveFile(video, destino, StandardCopyOption.ATOMIC_MOVE);
               TestSeleniumAbstracto.log.info("Mover video de " + video.getAbsolutePath() + " a " + destino.getAbsolutePath());
            }
         }
         catch (IOException e) {
            if (video != null) {
               TestSeleniumAbstracto.log
                     .warn("No se puede mover el video (" + video.getAbsolutePath() + ") a su ubicación definitiva (" + destinoRuta + ")");
            }
            TestSeleniumAbstracto.log.error(e.getLocalizedMessage());
         }
      }
      return nombreDestino;
   }

}
