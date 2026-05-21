package es.juntadeandalucia.agapa.pruebasSelenium.webdriver;

import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.WebElementWrapperPrimeFace;


/**
 * Factoria de Web drivers específica para aplicaciones PrimeFaces.
 *
 * <p>Extiende {@link WebDriverFactory} activando <b>Selenium Manager</b> en lugar de
 * WebDriverManager (bonigarcia). El equipo GMA tomó esta decisión porque WebDriverManager
 * requiere acceso a {@code https://googlechromelabs.github.io} para descargar el chromedriver,
 * endpoint no disponible desde redes corporativas restringidas. Selenium Manager, incluido
 * en {@code selenium-java >= 4.6}, resuelve el driver de forma local sin necesidad de red.
 *
 * <p>La activación es <b>opt-in</b>: los proyectos que usen esta clase heredan automáticamente
 * el comportamiento. Los proyectos que usen {@link WebDriverFactory} directamente siguen
 * usando WebDriverManager sin ningún cambio.
 */
public class WebDriverFactoryPrimeFace extends WebDriverFactory {

   static {
      // El equipo GMA decidió no usar WebDriverManager (bonigarcia) porque su endpoint de
      // descarga de drivers (googlechromelabs.github.io) no es accesible desde la red corporativa.
      // Activamos el flag para que WebDriverFactory use Selenium Manager en su lugar.
      WebDriverFactory.usarSeleniumManager = true;
   }

   /**
    * Punto de entrada para que proyectos externos activen Selenium Manager antes de crear
    * el primer driver. Invocar este método desde un bloque {@code static} garantiza que el
    * bloque inicializador de esta clase se ejecute a tiempo (en Java, invocar un método
    * estático es un "active use" que desencadena la inicialización de la clase, a diferencia
    * de acceder al class literal con {@code .class}).
    */
   public static void activar() {
      // La activación real ocurre en el bloque static de esta clase
   }

   /** The web element wrapper. */
   private static WebElementWrapperPrimeFace webElementWrapper;

   /**
    * Gets the web element wrapper.
    *
    * @return the web element wrapper
    */
   public static WebElementWrapperPrimeFace getWebElementWrapper() {
      if (WebDriverFactoryPrimeFace.webElementWrapper == null) {
         WebDriverFactoryPrimeFace.webElementWrapper = new WebElementWrapperPrimeFace();
      }
      return WebDriverFactoryPrimeFace.webElementWrapper;
   }

}
