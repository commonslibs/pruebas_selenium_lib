package es.juntadeandalucia.agapa.pruebasSelenium.webdriver;

import com.aventstack.extentreports.ExtentTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.WebElementWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.Collections;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;


/**
 * Factoria de Web drivers. Devuelve el driver del navegador especificado mediante obtenerInstancia().
 */
@Slf4j
public class WebDriverFactory {

   @Getter
   static @Setter private ExtentTest logger;

   // Almacén del WebDriver compartido por todos los servicios y clases de test del proyecto.
   @Getter
   @Setter
   private static WebDriver          driver;

   private static WebElementWrapper  webElementWrapper;

   public enum Navegador {
      CHROME, FIREFOX, MSEDGE
   }

   private static final String TX_FALSE                = "false";

   public static final boolean IS_REMOTE_SELENIUM_GRID =
         Boolean.parseBoolean(System.getProperty("remoteSG", WebDriverFactory.TX_FALSE).toLowerCase());

   public static final boolean IS_HEADLESS             =
         Boolean.parseBoolean(System.getProperty("java.awt.headless", WebDriverFactory.TX_FALSE).toLowerCase());

   public static final boolean IS_MODO_INCOGNITO       =
         Boolean.parseBoolean(System.getProperty("modoIncognito", WebDriverFactory.TX_FALSE).toLowerCase());

   private static final String HTTPS_PROXY             = VariablesGlobalesTest.getPropiedadOpcional(PropiedadesTest.HTTPS_PROXY, "");

   /**
    * No se implementa como singleton debido a una posible ejecución en paralelo.
    *
    * @param navegador
    *           navegador web que se usara para la prueba
    * @return wd el webdriver
    */
   public static WebDriver obtenerInstancia(Navegador navegador) {
      WebDriverFactory.log.info("Obteniendo una nueva instancia del navegador " + navegador.toString());
      WebDriver wd = null;
      switch (navegador) {
         case CHROME:
            wd = chrome();
            break;
         case FIREFOX:
            wd = firefox();
            break;
         case MSEDGE:
            wd = edge();
            break;
      }
      WebDriverFactory.log.info("Instancia obtenida: " + wd.toString());
      return wd;
   }

   public static WebElementWrapper getWebElementWrapper() {
      if (webElementWrapper == null) {
         webElementWrapper = new WebElementWrapper();
      }
      return webElementWrapper;
   }

   // FUENTES:
   // https://github.com/GoogleChrome/chrome-launcher/blob/main/docs/chrome-flags-for-tools.md
   // https://source.chromium.org/chromium/chromium/src/+/main:chrome/common/pref_names.h
   // https://peter.sh/experiments/chromium-command-line-switches/
   private static WebDriver chrome() {
      WebDriverManager webDriver = WebDriverManager.chromedriver();
      WebDriverManager webDriverConProxy = asignarProxy(webDriver);
      webDriverConProxy.setup();

      System.setProperty("webdriver.chrome.whitelistedIps", "");

      ChromeOptions options = new ChromeOptions();

      options.addArguments("enable-automation");

      // Parametros para evitar errores al ejecutar Google Chrome con usuario root en linux (se aconseja incluirla cuando se ejecuta chrome
      // en un entorno docker como es el caso
      options.addArguments("--no-sandbox");

      // IMPORTANTE: Para que sirve la siguiente propiedad?
      options.addArguments("--enable-npapi");

      // Incompatibilidad de selenium y chrome 111 al utilizar direcciones sin https
      // https: // stackoverflow.com/questions/75678572/java-io-ioexception-invalid-status-code-403-text-forbidden
      options.addArguments("--remote-allow-origins=*");

      // Para ignorar las advertencias al no utilizar https
      options.addArguments("ignore-certificate-errors");

      // Habilitar la opción "Ejecutar contenido Java"
      options.addArguments("--enable-internal-ehb");

      // Se aconseja incluirla cuando se ejecuta chrome en un entorno docker
      options.addArguments("--disable-dev-shm-usage");

      // https://stackoverflow.com/a/49123152/1689770
      options.addArguments("--disable-browser-side-navigation");

      // https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
      options.addArguments("--disable-gpu");

      // Para habilitar el registro detallado
      options.addArguments("--verbose");

      // Se deshabilitan las extensiones
      options.addArguments("--disable-extensions");

      // Se deshabiitan el sonido. Aconsejado en entorno docker
      options.addArguments("--mute-audio");

      // Desactiva la traducción de Chrome, tanto la opción manual como el mensaje emergente cuando se detecta una página con un idioma
      // diferente.
      options.addArguments("--disable-features=Translate");

      // Desactive la verificación del navegador predeterminado, no solicite configurarlo como tal
      options.addArguments("--no-default-browser-check");

      // No actualice los 'componentes' del navegador enumerados en chrome://components/
      options.addArguments("--disable-component-update");

      // Desactiva la comunicación del servidor de autocompletar. Esta función no se desactiva mediante otras banderas "principales".
      options.addArguments("--disable-features=AutofillServerCommunication");

      // Desactivar la sincronización con una cuenta de Google
      options.addArguments("--disable-sync");

      // Para lanzar en modo incognito
      if (WebDriverFactory.IS_MODO_INCOGNITO) {
         options.addArguments("--incognito");
      }

      // Parámetro para no abrir el modo gráfico del navegador.
      if (WebDriverFactory.IS_HEADLESS) {
         options.addArguments("--headless=new");
      }
      else {
         // Deshabilitamos el mensaje delnavegador: "Un Software automatizado de pruebas está controlando Chrome."
         options.setExperimentalOption("excludeSwitches", Collections.singletonList("--enable-automation"));
      }

      // Para no mostrar el dialogo de "donde guardar archivos". Bandera especial para el modo incognito a partir de chrome 119
      options.addArguments("disable-features=DownloadBubble,DownloadBubbleV2");

      // Experimental OPTIONS
      HashMap<String, Object> prefs = new HashMap<>();
      // Para descargar archivos XML
      prefs.put("download.extensions_to_open", "application/xml");
      // Para no mostrar el dialogo de "donde guardar archivos"
      prefs.put("safebrowsing.enabled", false);
      prefs.put("download.prompt_for_download", false);
      prefs.put("download.directory_upgrade", true);
      prefs.put("profile.default_content_settings.popups", 0);
      prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

      // Boolean that specifies if file selection dialogs are shown.
      prefs.put("select_file_dialogs.allowed", false);

      options.setExperimentalOption("prefs", prefs);

      // Para lanzar en modo incognito
      if (WebDriverFactory.IS_REMOTE_SELENIUM_GRID) {
         DesiredCapabilities capabilities = new DesiredCapabilities();

         capabilities.setCapability(ChromeOptions.CAPABILITY, options);

         return webDriver.capabilities(capabilities).create();
      }
      else {
         return new ChromeDriver(options);
      }
   }

   private static WebDriver firefox() {
      WebDriverManager webDriver = WebDriverManager.firefoxdriver();
      WebDriverManager webDriverConProxy = asignarProxy(webDriver);
      webDriverConProxy.setup();

      FirefoxOptions options = new FirefoxOptions();

      // Parámetro para no abrir el modo gráfico del navegador.
      if (WebDriverFactory.IS_HEADLESS) {
         options.addArguments("headless");
      }

      // Para lanzar en modo incognito
      if (WebDriverFactory.IS_MODO_INCOGNITO) {
         options.addArguments("-private");
      }

      if (WebDriverFactory.IS_REMOTE_SELENIUM_GRID) {
         DesiredCapabilities capabilities = new DesiredCapabilities();

         capabilities.setCapability(
               // FirefoxOptions.CAPABILITY, "moz:firefoxOptions"
               FirefoxOptions.FIREFOX_OPTIONS, options);

         return WebDriverManager.firefoxdriver().capabilities(capabilities).create();
      }
      else {
         return new FirefoxDriver(options);
      }
   }

   private static WebDriver edge() {
      WebDriverManager webDriver = WebDriverManager.edgedriver();
      WebDriverManager webDriverConProxy = asignarProxy(webDriver);
      webDriverConProxy.setup();

      EdgeOptions options = new EdgeOptions();

      // Para lanzar en modo incognito
      if (WebDriverFactory.IS_MODO_INCOGNITO) {
         options.addArguments("incognito");
      }

      // Parámetro para no abrir el modo gráfico del navegador.
      if (WebDriverFactory.IS_HEADLESS) {
         options.addArguments("headless");
      }

      if (WebDriverFactory.IS_REMOTE_SELENIUM_GRID) {
         DesiredCapabilities capabilities = new DesiredCapabilities();

         capabilities.setCapability(EdgeOptions.CAPABILITY, options);

         return WebDriverManager.edgedriver().capabilities(capabilities).create();
      }
      else {
         return new EdgeDriver(options);
      }
   }

   private static WebDriverManager asignarProxy(WebDriverManager webDriver) {
      if (StringUtils.isNotEmpty(WebDriverFactory.HTTPS_PROXY)) {
         return webDriver.proxy(WebDriverFactory.HTTPS_PROXY);
      }
      else {
         return webDriver;
      }
   }

}