package es.juntadeandalucia.agapa.pruebasSelenium.webdriver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.GeckoDriverService;


/**
 * Factoria de Web drivers. Devuelve el driver del navegador especificado mediante obtenerInstancia().
 *
 * @author dmartinez
 */
@Slf4j
public class WebDriverFactory {

   public enum Navegador {
      CHROME, FIREFOX, MSEDGE
   }

   private static final Map<Navegador, Supplier<WebDriver>> navegadores;

   private static final Supplier<WebDriver>                 chromeDriverSupplier  = () -> {
                                                                                     String driverPath = "chromedriver";
                                                                                     if (System.getProperty("os.name").toLowerCase()
                                                                                           .contains("win")) {
                                                                                        driverPath += ".exe";
                                                                                     }
                                                                                     System.setProperty(
                                                                                           ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
                                                                                           buscarFichero(driverPath));
                                                                                     System.setProperty("webdriver.chrome.whitelistedIps",
                                                                                           "");
                                                                                     ChromeOptions options = new ChromeOptions();

                                                                                     options.addArguments("--no-sandbox");
                                                                                     options.addArguments("--disable-dev-shm-usage");
                                                                                     options.addArguments("--verbose");
                                                                                     return new ChromeDriver(options);
                                                                                  };

   private static final Supplier<WebDriver>                 firefoxDriverSupplier = () -> {
                                                                                     String driverPath = "geckodriver";
                                                                                     if (System.getProperty("os.name").toLowerCase()
                                                                                           .contains("win")) {
                                                                                        driverPath += ".exe";
                                                                                     }
                                                                                     System.setProperty(
                                                                                           GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY,
                                                                                           buscarFichero(driverPath));
                                                                                     return new FirefoxDriver();

                                                                                  };

   private static final Supplier<WebDriver>                 edgeDriverSupplier    = () -> {
                                                                                     System.setProperty(
                                                                                           EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY,
                                                                                           buscarFichero("msedgedriver.exe"));
                                                                                     return new EdgeDriver();
                                                                                  };

   static {
      navegadores = new HashMap<>();
      navegadores.put(Navegador.CHROME, chromeDriverSupplier);
      navegadores.put(Navegador.FIREFOX, firefoxDriverSupplier);
      navegadores.put(Navegador.MSEDGE, edgeDriverSupplier);
   }

   /**
    * No se implementa como singleton debido a una posible ejecución en paralelo.
    *
    * @param navegador
    * @return
    */
   public static WebDriver obtenerInstancia(Navegador navegador) {
      log.info("Obteniendo una nueva instancia del navegador " + navegador.toString());
      WebDriver wd = navegadores.get(navegador).get();
      log.info("Instancia obtenida: " + wd.toString());
      return wd;
   }

   /**
    * Lectura del driver en disco
    *
    * @param filename
    * @return @throws
    */
   private static String buscarFichero(String filename) {
      String paths[] = { "drivers" + File.separator };
      for (String path : paths) {
         if (new File(path + filename).exists()) {
            return path + filename;
         }
      }
      log.error("El fichero " + filename + " no se encuentra en disco...");

      return "";
   }

}